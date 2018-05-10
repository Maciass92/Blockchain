package com.example.blockchaininfo.services;

import com.example.blockchaininfo.model.NetworkHashrate;
import com.example.blockchaininfo.model.PoolDef;
import com.example.blockchaininfo.model.PoolHashrate;
import com.example.blockchaininfo.pojos.*;
import com.example.blockchaininfo.repositories.NetworkHashrateRepository;
import com.example.blockchaininfo.repositories.PoolDefRepository;
import com.example.blockchaininfo.repositories.PoolHashrateRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.HttpClientBuilder;
import org.hibernate.loader.custom.Return;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
public class GetDataService {

    private final NetworkHashrateRepository networkHashrateRepository;
    private final PoolDefRepository poolDefRepository;
    private final PoolHashrateRepository poolHashrateRepository;
    private final ObjectMapper jsonMapper;
    private final ExecutorService executorService;

    private final Map<String, PoolExecutionData> poolErrorMap;


    public GetDataService(NetworkHashrateRepository networkHashrateRepository, PoolDefRepository poolDefRepository, PoolHashrateRepository poolHashrateRepository, ObjectMapper jsonMapper) {
        this.networkHashrateRepository = networkHashrateRepository;
        this.poolDefRepository = poolDefRepository;
        this.poolHashrateRepository = poolHashrateRepository;
        this.jsonMapper = jsonMapper;
        this.executorService = Executors.newCachedThreadPool();

        this.poolErrorMap = this.initializePoolErrorMap(this.getPoolsListFromJson());
    }

    public Map<String, PoolExecutionData> initializePoolErrorMap(PoolList poolList){

        Map<String, PoolExecutionData> helperMap = new HashMap<>();

        for (PoolDefinition p : poolList.getPoolList())
            helperMap.put(p.getName(), new PoolExecutionData(OffsetDateTime.now().minusDays(1), p.getName()));

        return helperMap;
    }

    @Scheduled(fixedRate = 5000)
    public void storeData() throws IOException, InterruptedException{

            try {
                String hashrateFromApi = this.getJsonFromApi("http://public.turtlenode.io:11898/getinfo");
                OffsetDateTime date = OffsetDateTime.now();

                this.saveNetworkHashrateNewEntity(hashrateFromApi, date);
                this.storePoolDataToDB(this.getPoolsListFromJson(), retrieveNetworkIdForPoolDefinition(date));

            } catch (HttpServerErrorException e){
                log.info("Network Server error e1: " + e);
            } catch (ResourceAccessException e2){
                log.info("Network resource access exception: " + e2);
            }
    }

    public String getJsonFromApi(String url){

        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(HttpClientBuilder.create().build());
        clientHttpRequestFactory.setConnectTimeout(4500);
        clientHttpRequestFactory.setReadTimeout(4500);
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);

        ObjectNode objectNode = restTemplate.getForObject(url, ObjectNode.class);
        JsonNode jsonNode = objectNode.get("hashrate");

        return jsonNode.asText();
    }

    public Long retrieveNetworkIdForPoolDefinition(OffsetDateTime date){

        Optional<NetworkHashrate> networkHashrateOptional = networkHashrateRepository.findByDate(date);

        return networkHashrateOptional.isPresent() ? networkHashrateOptional.get().getId() : null;
    }


    public void saveNetworkHashrateNewEntity(String hashrate_as_string, OffsetDateTime date){

        NetworkHashrate networkHashrate = new NetworkHashrate();
        networkHashrate.setHashrate(Double.parseDouble(hashrate_as_string));
        networkHashrate.setRepDate(date);

        networkHashrateRepository.save(networkHashrate);
    }


    public PoolList getPoolsListFromJson(){

        PoolList poolList = new PoolList();
        try {
            InputStream jsonPoolListFile = new ClassPathResource("/static/json/networks/pools/turtlecoin-pools.json").getInputStream();
            poolList = this.jsonMapper.readValue(jsonPoolListFile, PoolList.class);
            jsonPoolListFile.close();
        } catch (IOException e){
            log.info("IOException!");
        }

        return poolList;
    }

    public void storePoolDataToDB(PoolList poolList, Long id) throws IOException, InterruptedException{

       // long started = System.currentTimeMillis();
        List<Future<ReturnedStringAndDate>> futureList = new ArrayList<>();
        List<String> listOfNames = this.createListOfNames(poolList);

        try{
            futureList = executorService.invokeAll(this.createListOfCallableTasks(poolList));
    System.out.println("Checking futures -------------->");

            System.out.println("Active threads: " + Thread.activeCount());
        } catch (ResourceAccessException e){
            log.info("Pool resource access exception: " + e);
        } catch (HttpServerErrorException e2){
            log.info("Pool server error: " + e2);
        } catch (IllegalStateException e3){
            log.info("Pool server error: " + e3);
        }

        for (Future<ReturnedStringAndDate> f : futureList)

        try {
            System.out.println(f.get());

        } catch (ExecutionException e){
            log.info("Execution exception.");
            continue;
        }

      //  System.out.println("Time: " + (System.currentTimeMillis() - started));

        for (int i = 0; i < futureList.size(); i++){

            try {
                PoolDef poolDef = this.savePoolDefNewEntity(futureList.get(i).get());
                this.savePoolHashrateNewEntity(futureList.get(i).get().getJsonString(), futureList.get(i).get(), poolDef, id);
                poolErrorMap.get(poolDef.getName()).setErrorCount(0);

            } catch (ExecutionException e){

                poolErrorMap.get(listOfNames.get(i)).incrementErrorCount();
                poolErrorMap.get(listOfNames.get(i)).setExecutionDate();
                continue;
            }
        }
        System.out.println("<--------- END OF SESSION --------->");

    }

    public List<Callable<ReturnedStringAndDate>> createListOfCallableTasks(PoolList poolList){

        List<Callable<ReturnedStringAndDate>> callableList = new ArrayList<>();

    System.out.println("Checking errorMap --------------->");
        for (String name : this.poolErrorMap.keySet())
    System.out.println("Pool Name: " + name + ", errors: " + poolErrorMap.get(name).getErrorCount() + ", execDate: " + poolErrorMap.get(name).getExecutionDate());

        for (int i = 0; i < poolList.getPoolList().size(); i++){

            if (this.poolErrorMap.get(poolList.getPoolList().get(i).getName()).getExecutionDate().isBefore(OffsetDateTime.now()))
                callableList.add(new ConnectToApiCallable(this.appendPoolApiUrl(poolList.getPoolList().get(i)), poolList.getPoolList().get(i).getName(), poolList.getPoolList().get(i).getType()));
            else
                continue;
        }

        return callableList;
    }

    public List<String> createListOfNames(PoolList poolList){

        List<String> namesOrdered = new ArrayList<>();

        for (int i = 0; i < poolList.getPoolList().size(); i++){

            if (this.poolErrorMap.get(poolList.getPoolList().get(i).getName()).getExecutionDate().isBefore(OffsetDateTime.now()))
                namesOrdered.add(poolList.getPoolList().get(i).getName());
            else
                continue;
        }

        return namesOrdered;
    }

    public PoolDef savePoolDefNewEntity(ReturnedStringAndDate object){

        PoolDef poolDef = new PoolDef();
        poolDef.setDateFrom(object.getDateTime());
        poolDef.setName(object.getPoolName());

        poolDefRepository.save(poolDef);

        return poolDef;
    }

    public void savePoolHashrateNewEntity(String jsonString, ReturnedStringAndDate object, PoolDef poolDef, Long id) throws IOException{

        PoolHashrate poolHashrate = new PoolHashrate();
        poolHashrate.setHashrate(this.retrieveHashrateFromJsonString(jsonString, object));
        poolHashrate.setNetworkHashrate(networkHashrateRepository.findById(Long.valueOf(id)).get());
        poolHashrate.setPoolDef(poolDef);

        poolHashrateRepository.save(poolHashrate);
    }

    public double retrieveHashrateFromJsonString(String string, ReturnedStringAndDate object) throws IOException{

        JsonNode jsonNode = jsonMapper.readTree(string);

        return object.getPoolType().equals("forknote") ? jsonNode.get("pool").get("hashrate").asDouble() : jsonNode.get("pool_statistics").get("hashRate").asDouble();
    }

    public String appendPoolApiUrl(PoolDefinition poolDefinition){

        StringBuilder appendedApi = new StringBuilder(poolDefinition.getApi());

        return poolDefinition.getType().equals("forknote") ? appendedApi.append("stats").toString() : appendedApi.append("pool/stats").toString();
    }
}
