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

        /*
        File folder = new File("src/main/resources/static/json/networks/pools");
        File[] listOfFiles = folder.listFiles();

        for(File f : listOfFiles) {
            System.out.println(f.getPath());
        }*/

        List<Path> listOfFiles = Files.walk(Paths.get(ClassLoader.getSystemResource("static/json/networks/pools/").toURI()))
                .filter(Files::isRegularFile)
                .collect(toList());

        String myString = listOfFiles.get(0).toString();
        String newString = myString.substring(myString.indexOf("static") - 1, myString.length());

        System.out.println(newString);

        return listOfFiles;
    }

    //List<PoolList>
    public PoolList getPoolsListFromJson() throws IOException {

        ObjectMapper jsonMapper = new ObjectMapper();
        List<PoolList> poolListsList = new ArrayList<>();

        //for(File f : this.getAllFilePathsInFolder()) {

            InputStream jsonPoolsFile = new ClassPathResource("/static/json/networks/pools/turtlecoin-pools.json").getInputStream();
            PoolList poolList = jsonMapper.readValue(jsonPoolsFile, PoolList.class);

            //poolListsList.add(poolList);

            jsonPoolsFile.close();
        //}

        return poolList;
    }

    public void getPoolDataAndStoreToDB() throws IOException{

        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(
                HttpClientBuilder.create().build());
        clientHttpRequestFactory.setConnectTimeout(5000);
        clientHttpRequestFactory.setReadTimeout(5000);
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);

        OffsetDateTime date = OffsetDateTime.now();
        PoolList poolList = this.getPoolsListFromJson();

            for(int i = 0; i < this.getPoolsListFromJson().getPoolList().size(); i++) {

                    String jsonString = restTemplate.getForObject(this.appendPoolApiURL(poolList.getPoolList().get(i)), String.class);;

                /*try {
                    jsonString = restTemplate.getForObject(this.appendPoolApiURL(poolList.getPoolList().get(i)), String.class);
                } catch (ResourceAccessException e){
                    continue;
                }*/

                PoolDef poolDef = new PoolDef();
                poolDef.setDate_from(date);
                poolDef.setName(poolList.getPoolList().get(i).getName());
                poolDef.setId(new Long(i + 1));

                poolDefRepository.save(poolDef);

                PoolHashrate poolHashrate = new PoolHashrate();
                poolHashrate.setHashrate(this.processJsonString(jsonString, poolList.getPoolList().get(i)));
                poolHashrate.setNetworkHashrate(networkHashrateRepository.findById(new Long(1)).get());
                poolHashrate.setPoolDef(poolDef);
                poolHashrate.setId(new Long(i + 1));

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
