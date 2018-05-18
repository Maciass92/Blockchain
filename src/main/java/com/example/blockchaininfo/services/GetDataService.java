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
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

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

    private Map<String, PoolExecutionData> initializePoolErrorMap(PoolList poolList){

        Map<String, PoolExecutionData> helperMap = new HashMap<>();

        for (PoolDefinition p : poolList.getPoolList())
            helperMap.put(p.getName(), new PoolExecutionData(OffsetDateTime.now().minusDays(1), p.getName()));

        return helperMap;
    }

    public void storeData(){

        try {
            String hashrateFromApi = this.getNetworkHashrateFromApi("http://public.turtlenode.io:11898/getinfo");
            OffsetDateTime date = OffsetDateTime.now();

            this.saveNetworkHashrateNewEntity(hashrateFromApi, date);
            this.storePoolDataToDB(this.getPoolsListFromJson(), retrieveNetworkIdForPoolDefinition(date));

        } catch (HttpServerErrorException | InterruptedException | IOException | ResourceAccessException e){
            log.info("" + e);
        }
    }

    private String getNetworkHashrateFromApi(String url){

        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(HttpClientBuilder.create().build());
        clientHttpRequestFactory.setConnectTimeout(7000);
        clientHttpRequestFactory.setReadTimeout(7000);
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);

        return getHashrateFromJson(restTemplate, url);
    }

    private String getHashrateFromJson(RestTemplate restTemplate, String url){

        ObjectNode objectNode = restTemplate.getForObject(url, ObjectNode.class);
        JsonNode jsonNode = objectNode.get("hashrate");

        return jsonNode.asText();
    }

    private Long retrieveNetworkIdForPoolDefinition(OffsetDateTime date){

        Optional<NetworkHashrate> networkHashrateOptional = networkHashrateRepository.findByDate(date);

        return networkHashrateOptional.map(NetworkHashrate::getId).orElse(null);
    }

    private void saveNetworkHashrateNewEntity(String hashrateAsString, OffsetDateTime date){

        NetworkHashrate networkHashrate = new NetworkHashrate();
        networkHashrate.setHashrate(Double.parseDouble(hashrateAsString));
        networkHashrate.setRepDate(date);

        networkHashrateRepository.save(networkHashrate);
    }


    private PoolList getPoolsListFromJson(){

        PoolList poolList = new PoolList();
        InputStream jsonPoolListFile = null;

        try {
            jsonPoolListFile = new ClassPathResource("/static/json/turtlecoin-pools.json").getInputStream();
            poolList = this.jsonMapper.readValue(jsonPoolListFile, PoolList.class);
        } catch (IOException e){
            log.info("" + e);
        } finally{
            try {
                jsonPoolListFile.close();
            } catch (IOException e){log.info("" + e);
            }
        }

        return poolList;
    }

    private void storePoolDataToDB(PoolList poolList, Long id) throws InterruptedException, IOException{

        List<Future<ReturnedPoolData>> futureList = new ArrayList<>();
        List<String> listOfNames = this.createListOfPoolNames(poolList);
        List<String> listOfNonRespondingPools = this.createListOfNonRespondingPools(poolList);

        try{
            futureList = executorService.invokeAll(this.createListOfCallableTasks(poolList));
        } catch (ResourceAccessException | IllegalStateException | HttpServerErrorException e){
            log.info("" + e);
        }

        this.checkForPoolErrors(futureList, listOfNames, id, listOfNonRespondingPools);
    }

    private void checkForPoolErrors(List<Future<ReturnedPoolData>> futureList, List<String> listOfNames, Long id, List<String> nonRespondingPools) throws InterruptedException, IOException{

        ReturnedPoolData returnedPoolData = null;

        for (int i = 0; i < futureList.size(); i++){

            try {
                returnedPoolData = futureList.get(i).get();

                if(returnedPoolData == null || returnedPoolData.getJsonString().isEmpty())
                    this.processAndStoreData(returnedPoolData, i, listOfNames, id, true);
                else
                    this.processAndStoreData(returnedPoolData, i, listOfNames, id, false);

            } catch (ExecutionException e){
                log.info("" + e);
                this.processAndStoreData(returnedPoolData, i, listOfNames, id, true);
            }
        }

        this.processAndStoreData(returnedPoolData);
    }

    private List<Callable<ReturnedPoolData>> createListOfCallableTasks(PoolList poolList){

        List<Callable<ReturnedPoolData>> callableList = new ArrayList<>();

        for (int i = 0; i < poolList.getPoolList().size(); i++){

            PoolDefinition poolDefinition = poolList.getPoolList().get(i);

            if (isTaskExecutable(poolDefinition.getName()))
                callableList.add(new ConnectToApiCallable(this.appendPoolApiUrl(poolDefinition), poolDefinition.getName(), poolDefinition.getType()));
        }

        return callableList;
    }

    private List<String> createListOfNonRespondingPools(PoolList poolList){

        return poolList.getPoolList().stream()
                .filter(q -> !isTaskExecutable(q.getName()))
                .map(q -> q.getName())
                .collect(Collectors.toList());
    }

    private List<String> createListOfPoolNames(PoolList poolList){

        return poolList.getPoolList().stream()
                .filter(q -> isTaskExecutable(q.getName()))
                .map(PoolDefinition::getName)
                .collect(Collectors.toList());
    }

    private boolean isTaskExecutable(String name){

        return this.poolErrorMap.get(name).getExecutionDate().isBefore(OffsetDateTime.now());
    }

    public void processAndStoreData(ReturnedPoolData returnedPoolData, Integer i, List<String> listOfNames, Long id, boolean errorPresent) throws IOException{

        PoolDef poolDef = this.savePoolDefNewEntity(returnedPoolData, errorPresent, i, listOfNames);
        this.savePoolHashrateNewEntity(returnedPoolData, poolDef, id, errorPresent, listOfNames, i);
        this.setPoolErrors(i, errorPresent, listOfNames);
    }

    private void saveNonRespondingPools(List<String> nonRespondingPools, Long id){

        //this.processAndStoreData(null, null, nonRespondingPools, id, true);
    }

    private PoolDef savePoolDefNewEntity(ReturnedPoolData returnedPoolData, boolean errorPresent, Integer i, List<String> listOfNames){

        PoolDef poolDef = new PoolDef();

        if (errorPresent){
            poolDef.setDateFrom(OffsetDateTime.now());
            poolDef.setName(listOfNames.get(i));
        } else {
            poolDef.setDateFrom(returnedPoolData.getDateTime());
            poolDef.setName(returnedPoolData.getPoolName());
        }

        poolDefRepository.save(poolDef);

        return poolDef;
    }

    private void savePoolHashrateNewEntity(ReturnedPoolData returnedPoolData, PoolDef poolDef, Long id, boolean errorPresent, List<String> listOfNames, Integer i) throws IOException{

        PoolHashrate poolHashrate = new PoolHashrate();

        if (errorPresent){
            poolHashrate.setHashrate(0);
            poolHashrate.setPoolDef(poolDef);
            poolHashrate.setNetworkHashrate(networkHashrateRepository.findById(id).get());
        } else {
            poolHashrate.setHashrate(this.retrieveHashrateFromJsonString(returnedPoolData));
            poolHashrate.setNetworkHashrate(networkHashrateRepository.findById(id).get());
            poolHashrate.setPoolDef(poolDef);
        }

        poolHashrateRepository.save(poolHashrate);
    }

    private void setPoolErrors(Integer i, Boolean errorPresent, List<String> listOfNames){

        if(errorPresent) {
            poolErrorMap.get(listOfNames.get(i)).incrementErrorCount();
            poolErrorMap.get(listOfNames.get(i)).setExecutionDate();
        } else
            poolErrorMap.get(listOfNames.get(i)).setErrorCount(0);
    }

    private double retrieveHashrateFromJsonString(ReturnedPoolData returnedPoolData) throws IOException{

        JsonNode jsonNode = jsonMapper.readTree(returnedPoolData.getJsonString());

        return returnedPoolData.getPoolType().equals("forknote") ? jsonNode.get("pool").get("hashrate").asDouble() : jsonNode.get("pool_statistics").get("hashRate").asDouble();
    }

    private String appendPoolApiUrl(PoolDefinition poolDefinition){

        StringBuilder appendedApi = new StringBuilder(poolDefinition.getApi());

        return poolDefinition.getType().equals("forknote") ? appendedApi.append("stats").toString() : appendedApi.append("pool/stats").toString();
    }

    public String formatDate(OffsetDateTime dateTime){

        return DateTimeFormatter.ofPattern("yyyy-MM-dd / HH:mm:ss").format(dateTime);
    }

    public double formatHashrate(double hashrate){

        return hashrate/1000.0;
    }
}