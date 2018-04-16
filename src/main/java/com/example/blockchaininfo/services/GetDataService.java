package com.example.blockchaininfo.services;

import com.example.blockchaininfo.POJOs.NetworkDef;
import com.example.blockchaininfo.POJOs.NetworkList;
import com.example.blockchaininfo.POJOs.PoolList;
import com.example.blockchaininfo.model.NetworkHashrate;
import com.example.blockchaininfo.repositories.NetworkHashrateRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.lang.*;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class GetDataService {

    private final NetworkHashrateRepository networkHashrateRepository;

    public NetworkList getNetworkListFromJson() throws IOException {

        ObjectMapper jsonMapper = new ObjectMapper();

        InputStream jsonNetworkDetailsFile = new ClassPathResource("/static/json/networks/network-list.json").getInputStream();

        NetworkList networkList = jsonMapper.readValue(jsonNetworkDetailsFile, NetworkList.class);

        jsonNetworkDetailsFile.close();

        return networkList;
    }

    public void getDataAndStoreToDB() throws IOException{

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

    public File[] getAllFilePathsInFolder(){

        File folder = new File("src/main/resources/static/json/networks/pools");
        File[] listOfFiles = folder.listFiles();

        return listOfFiles;
    }

    public List<PoolList> getPoolsListFromJson() throws IOException {

        ObjectMapper jsonMapper = new ObjectMapper();
        List<PoolList> poolListsList = new ArrayList<>();

        for(File f : this.getAllFilePathsInFolder()) {

            InputStream jsonPoolsFile = new ClassPathResource(f.getPath()).getInputStream();
            PoolList poolList = jsonMapper.readValue(jsonPoolsFile, PoolList.class);

            poolListsList.add(poolList);

            jsonPoolsFile.close();
        }

        return poolListsList;
    }

    public void getPoolDataAndStoreToDB(){

        RestTemplate restTemplate = new RestTemplate();
        OffsetDateTime date = OffsetDateTime.now();

    }

}
