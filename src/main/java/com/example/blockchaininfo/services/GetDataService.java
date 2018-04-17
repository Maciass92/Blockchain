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
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.lang.*;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Service
public class GetDataService {

    private final NetworkHashrateRepository networkHashrateRepository;
    private final PoolDefRepository poolDefRepository;
    private final PoolHashrateRepository poolHashrateRepository;

    public NetworkList getNetworkListFromJson() throws IOException {

        ObjectMapper jsonMapper = new ObjectMapper();

        InputStream jsonNetworkDetailsFile = new ClassPathResource("/static/json/networks/network-list.json").getInputStream();
        NetworkList networkList = jsonMapper.readValue(jsonNetworkDetailsFile, NetworkList.class);

        jsonNetworkDetailsFile.close();

        return networkList;
    }

    public void getNetworkDataAndStoreToDB() throws IOException{

        RestTemplate restTemplate = new RestTemplate();
        OffsetDateTime date = OffsetDateTime.now();

        for(NetworkDef networkDef : this.getNetworkListFromJson().getNetworkList()){

            String hashrate_as_string = restTemplate.getForObject(networkDef.getApi_url(), String.class);

            NetworkHashrate networkHashrate = new NetworkHashrate();
            networkHashrate.setId(networkDef.getId());
            networkHashrate.setHashrate(Double.parseDouble(hashrate_as_string));
            networkHashrate.setRep_date(date);

            networkHashrateRepository.save(networkHashrate);
        }
    }

    public List<Path> getAllFilePathsInFolder() throws IOException, URISyntaxException {

        List<Path> listOfFiles = Files.walk(Paths.get(ClassLoader.getSystemResource("static/json/networks/pools/").toURI()))
                .filter(Files::isRegularFile)
                .collect(toList());

        return listOfFiles;
    }

    public List<PoolList> getPoolsListFromJson() throws IOException, URISyntaxException {

        ObjectMapper jsonMapper = new ObjectMapper();
        List<PoolList> poolListsList = new ArrayList<>();

        for(Path path : this.getAllFilePathsInFolder()) {

            InputStream jsonPoolsFile = new ClassPathResource(this.pathToFormatedString(path)).getInputStream();
            PoolList poolList = jsonMapper.readValue(jsonPoolsFile, PoolList.class);

            poolListsList.add(poolList);

            jsonPoolsFile.close();
        }

        return poolListsList;
    }

    public String pathToFormatedString(Path path){

        String myString = path.toString();
        String newString = myString.substring(myString.indexOf("static") - 1, myString.length());

        return newString;
    }

    public void connectToPoolAPIs() throws IOException, URISyntaxException{

        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(
                HttpClientBuilder.create().build());
        clientHttpRequestFactory.setConnectTimeout(500);
        clientHttpRequestFactory.setReadTimeout(500);
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);

        List<PoolList> poolListList = this.getPoolsListFromJson();

        for(PoolList poolList : poolListList){

            this.storePoolDataToDB(poolList, restTemplate);
        }
    }

    public void storePoolDataToDB(PoolList poolList, RestTemplate restTemplate) throws IOException{

        OffsetDateTime date = OffsetDateTime.now();

        for (int i = 0; i < poolList.getPoolList().size(); i++) {

            //String jsonString = restTemplate.getForObject(this.appendPoolApiURL(poolList.getPoolList().get(i)), String.class);

            String jsonString;
            try {
                jsonString = restTemplate.getForObject(this.appendPoolApiURL(poolList.getPoolList().get(i)), String.class);
            } catch (ResourceAccessException e){
                continue;
            }


            PoolDef poolDef = new PoolDef();
            poolDef.setDate_from(date);
            poolDef.setName(poolList.getPoolList().get(i).getName());
//            poolDef.setId(poolDefRepository.findByName(poolList.getPoolList().get(i).getName()).get().getId());

            System.out.println("Iteracja: " + i);
            System.out.println("Id pool defa: " + poolDef.getId());

            poolDefRepository.save(poolDef);

            PoolHashrate poolHashrate = new PoolHashrate();
            poolHashrate.setHashrate(this.processJsonString(jsonString, poolList.getPoolList().get(i)));
            //todo how to assign id
            poolHashrate.setNetworkHashrate(networkHashrateRepository.findById(new Long(1)).get());
            poolHashrate.setPoolDef(poolDef);
       //     poolHashrate.setId(poolHashrateRepository.findByPoolId(poolDef.getId()).get().getId());

            System.out.println("Id pool hashrate'a: " + poolHashrate.getId());

            poolHashrateRepository.save(poolHashrate);
        }
    }

    public double processJsonString(String string, PoolDefinition poolDefinition) throws IOException{

        ObjectMapper jsonMapper = new ObjectMapper();
        double hashrate;

        Gett gett = jsonMapper.readValue(string, Gett.class);

        if(poolDefinition.getType().equals("forknote"))
            hashrate = gett.getPool().getHashrate();
        else
            hashrate = gett.getPool_statistics().getHashRate();

        return hashrate;
    }

    public String appendPoolApiURL(PoolDefinition poolDefinition){

        StringBuilder appended_api = new StringBuilder(poolDefinition.getApi());

        if(poolDefinition.getType().equals("forknote"))
            appended_api.append("stats");
        else
            appended_api.append("pool/stats");

        return appended_api.toString();
    }

}
