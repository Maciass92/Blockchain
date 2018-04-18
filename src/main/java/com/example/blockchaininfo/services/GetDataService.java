package com.example.blockchaininfo.services;

import com.example.blockchaininfo.POJOs.*;
import com.example.blockchaininfo.model.NetworkHashrate;
import com.example.blockchaininfo.model.PoolDef;
import com.example.blockchaininfo.model.PoolHashrate;
import com.example.blockchaininfo.repositories.NetworkHashrateRepository;
import com.example.blockchaininfo.repositories.PoolDefRepository;
import com.example.blockchaininfo.repositories.PoolHashrateRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.lang.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Service
public class GetDataService {

    private final NetworkHashrateRepository networkHashrateRepository;
    private final PoolDefRepository poolDefRepository;
    private final PoolHashrateRepository poolHashrateRepository;
    private final ObjectMapper jsonMapper;

    public NetworkList getNetworkListFromJson() throws IOException {

        InputStream jsonNetworkDetailsFile = new ClassPathResource("/static/json/networks/network-list.json").getInputStream();
        NetworkList networkList = this.jsonMapper.readValue(jsonNetworkDetailsFile, NetworkList.class);
        jsonNetworkDetailsFile.close();

        return networkList;
    }

    @Scheduled(fixedRate = 5000)
    public void storeData() throws IOException, URISyntaxException{

        OffsetDateTime date = OffsetDateTime.now();

        for(NetworkDefinition networkDefinition : this.getNetworkListFromJson().getNetworkList()){

            String hashrateFromAPI;

            try {
                hashrateFromAPI = this.getJsonFromAPI(networkDefinition.getApi_url());
            } catch (HttpServerErrorException e){
                break;
            }

            this.saveNetworkHashrateNewEntity(hashrateFromAPI, date);

            Optional<NetworkHashrate> networkHashrateOptional = networkHashrateRepository.findByDate(date);
            if(networkHashrateOptional.isPresent()){
                Long networkId = networkHashrateOptional.get().getId();
                this.connectToPoolAPIAndStoreData(networkId);
            }
        }
    }

    public String getJsonFromAPI(String url){

        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(HttpClientBuilder.create().build());
        clientHttpRequestFactory.setConnectTimeout(1000);
        clientHttpRequestFactory.setReadTimeout(1000);
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);

        String helperString = restTemplate.getForObject(url, String.class);

        return helperString;
    }

    public void saveNetworkHashrateNewEntity(String hashrate_as_string, OffsetDateTime date){

        NetworkHashrate networkHashrate = new NetworkHashrate();
        networkHashrate.setHashrate(Double.parseDouble(hashrate_as_string));
        networkHashrate.setRep_date(date);

        networkHashrateRepository.save(networkHashrate);
    }

    public List<Path> getAllFilePathsInFolder() throws IOException, URISyntaxException {

        List<Path> listOfFiles = Files.walk(Paths.get(ClassLoader.getSystemResource("static/json/networks/pools/").toURI()))
                .filter(Files::isRegularFile)
                .collect(toList());

        return listOfFiles;
    }

    public List<PoolList> getPoolsListFromJson() throws IOException, URISyntaxException {

        List<PoolList> poolListsList = new ArrayList<>();

        for(Path path : this.getAllFilePathsInFolder()) {

            InputStream jsonPoolsFile = new ClassPathResource(this.truncateFilePath(path)).getInputStream();
            PoolList poolList = this.jsonMapper.readValue(jsonPoolsFile, PoolList.class);

            poolListsList.add(poolList);

            jsonPoolsFile.close();
        }

        return poolListsList;
    }

    public String truncateFilePath(Path path){

        String helperString = path.toString();
        String truncatedFilePath = helperString.substring(helperString.indexOf("static") - 1, helperString.length());

        return truncatedFilePath;
    }

    public void connectToPoolAPIAndStoreData(Long id) throws IOException, URISyntaxException{

        List<PoolList> poolListList = this.getPoolsListFromJson();

        for(PoolList poolList : poolListList)
            this.storePoolDataToDB(poolList, id);
    }

    public void storePoolDataToDB(PoolList poolList, Long id) throws IOException{

        for (int i = 0; i < poolList.getPoolList().size(); i++) {

            OffsetDateTime date = OffsetDateTime.now();

            String jsonString;
            try {
                jsonString = this.getJsonFromAPI(this.appendPoolApiUrl(poolList.getPoolList().get(i)));
            } catch (ResourceAccessException e){
                continue;
            } catch (HttpServerErrorException e2){
                continue;
            }/* catch (HttpClientErrorException e3){
                continue;
            }*/

            PoolDef poolDef = this.savePoolDefNewEntity(date, poolList, i);

            this.savePoolHashrateNewEntity(jsonString, poolList, poolDef, id, i);
        }
    }

    public PoolDef savePoolDefNewEntity(OffsetDateTime date, PoolList poolList, int i){

        PoolDef poolDef = new PoolDef();
        poolDef.setDate_from(date);
        poolDef.setName(poolList.getPoolList().get(i).getName());

        poolDefRepository.save(poolDef);

        return poolDef;
    }

    public void savePoolHashrateNewEntity(String jsonString, PoolList poolList, PoolDef poolDef, Long id, int i) throws IOException{

        PoolHashrate poolHashrate = new PoolHashrate();
        poolHashrate.setHashrate(this.retrieveHashrateFromJsonString(jsonString, poolList.getPoolList().get(i)));
        poolHashrate.setNetworkHashrate(networkHashrateRepository.findById(new Long(id)).get());
        poolHashrate.setPoolDef(poolDef);

        poolHashrateRepository.save(poolHashrate);
    }

    public double retrieveHashrateFromJsonString(String string, PoolDefinition poolDefinition) throws IOException{

        double hashrate;

        PoolJson poolJson = this.jsonMapper.readValue(string, PoolJson.class);

        if(checkIfPoolIsForknoteType(poolDefinition))
            hashrate = poolJson.getPoolForknote().getHashrate();
        else
            hashrate = poolJson.getPool_nodeJs().getHashRate();

        return hashrate;
    }

    public String appendPoolApiUrl(PoolDefinition poolDefinition){

        StringBuilder appendedApi = new StringBuilder(poolDefinition.getApi());

        if(checkIfPoolIsForknoteType(poolDefinition))
            appendedApi.append("stats");
        else
            appendedApi.append("pool/stats");

        return appendedApi.toString();
    }

    public boolean checkIfPoolIsForknoteType(PoolDefinition poolDefinition){

        if(poolDefinition.getType().equals("forknote"))
            return true;
        else
            return false;
    }
}
