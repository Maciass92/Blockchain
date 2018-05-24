package com.blockchaininfo.backendData.services;

import com.blockchaininfo.backendData.model.PoolDef;
import com.blockchaininfo.backendData.model.PoolHashrate;
import com.blockchaininfo.backendData.pojos.*;
import com.blockchaininfo.backendData.repositories.NetworkHashrateRepository;
import com.blockchaininfo.backendData.repositories.PoolHashrateRepository;
import com.blockchaininfo.backendData.model.NetworkHashrate;
import com.blockchaininfo.backendData.repositories.PoolDefRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URLEncoder;
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

    @Value("${network.url}")
    private final String networkUrl;

    @Value("${pools.url}")
    private final String poolsUrl;

    public GetDataService(NetworkHashrateRepository networkHashrateRepository, PoolDefRepository poolDefRepository, PoolHashrateRepository poolHashrateRepository, ObjectMapper jsonMapper) {
        this.networkHashrateRepository = networkHashrateRepository;
        this.poolDefRepository = poolDefRepository;
        this.poolHashrateRepository = poolHashrateRepository;
        this.jsonMapper = jsonMapper;
        this.executorService = Executors.newCachedThreadPool();

        this.networkUrl = "";
        this.poolsUrl = "";

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
            String networkHashrate = this.getNetworkHashrateFromApi(networkUrl);
            OffsetDateTime date = OffsetDateTime.now();

            this.storePoolDataToDB(this.getPoolsListFromJson(), networkHashrate, date);

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


    private PoolList getPoolsListFromJson() {

        PoolList poolList = null;
        RestTemplate restTemplate = new RestTemplate();

        System.out.println(this.poolsUrl);
        String url = "https://raw.githubusercontent.com/turtlecoin/turtlecoin-pools-json/master/v2/turtlecoin-pools.json";

        try {
            poolList = this.jsonMapper.readValue(restTemplate.getForObject(url, String.class), PoolList.class);

        } catch (IOException e){
            log.info("" + e);
        }

        return poolList;
    }

    private void storePoolDataToDB(PoolList poolList, String networkHashrate, OffsetDateTime date) throws InterruptedException, IOException{

        List<Future<ReturnedPoolData>> dataFromApi = null;
        List<String> calledApis = this.createListOfPoolNames(poolList);
        List<String> nonRespondingPools = this.createListOfNonRespondingPools(poolList);

        try{
            dataFromApi = executorService.invokeAll(this.createListOfCallableTasks(poolList));
        } catch (ResourceAccessException | IllegalStateException | HttpServerErrorException e){
            log.info("" + e);
        }

        this.checkForPoolErrors(dataFromApi, calledApis, nonRespondingPools, networkHashrate, date);
    }

    private void checkForPoolErrors(List<Future<ReturnedPoolData>> dataFromApi, List<String> calledApis, List<String> nonRespondingPools, String networkHashrate, OffsetDateTime date) throws InterruptedException, IOException{

        ReturnedPoolData returnedPoolData = null;

        this.saveNetworkHashrateNewEntity(networkHashrate, date);
        Long id = this.retrieveNetworkIdForPoolDefinition(date);

        for (int i = 0; i < dataFromApi.size(); i++){

            boolean isErrorCase = false;

            try {
                returnedPoolData = dataFromApi.get(i).get();

                if(returnedPoolData == null || returnedPoolData.getJsonString().isEmpty())
                    isErrorCase = true;

            } catch (ExecutionException | ResourceAccessException e){
                log.info("" + e);
                isErrorCase = true;
            }

            this.processAndStoreData(returnedPoolData, i, calledApis, isErrorCase, id);
        }

        this.saveNonRespondingPools(nonRespondingPools, id);
    }

    private List<Callable<ReturnedPoolData>> createListOfCallableTasks(PoolList poolList){

        List<Callable<ReturnedPoolData>> callableList = new ArrayList<>();

        for (int i = 0; i < poolList.getPoolList().size(); i++){

            PoolDefinition poolDefinition = poolList.getPoolList().get(i);

            //todo implement Factory
            if (isTaskExecutable(poolDefinition.getName())) {

                if (poolDefinition.getApi() != null && (poolDefinition.getType().equals("forknote") || poolDefinition.getType().equals("node.js")))
                    callableList.add(new ApiConnectorCallable(this.appendPoolApiUrl(poolDefinition), poolDefinition.getName(), poolDefinition.getType()));
            }
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

    public void processAndStoreData(ReturnedPoolData returnedPoolData, Integer i, List<String> apis, boolean errorPresent, Long id) throws IOException{

        PoolDef poolDef = this.savePoolDefNewEntity(returnedPoolData, errorPresent, i, apis);
        this.savePoolHashrateNewEntity(returnedPoolData, poolDef, id, errorPresent);

        this.setPoolErrors(i, errorPresent, apis);
    }

    private void saveNonRespondingPools(List<String> nonRespondingPools, Long id) throws IOException{

        for (int i = 0; i < nonRespondingPools.size(); i++){

            PoolDef poolDef = this.savePoolDefNewEntity(null, true, i, nonRespondingPools);
            this.savePoolHashrateNewEntity(null, poolDef, id, true);
        }
    }

    private PoolDef savePoolDefNewEntity(ReturnedPoolData returnedPoolData, boolean errorPresent, Integer i, List<String> apis){

        PoolDef poolDef = new PoolDef();

        if (errorPresent){
            poolDef.setDateFrom(OffsetDateTime.now());
            poolDef.setName(apis.get(i));
        } else {
            poolDef.setDateFrom(returnedPoolData.getDateTime());
            poolDef.setName(returnedPoolData.getPoolName());
        }

        poolDefRepository.save(poolDef);

        return poolDef;
    }

    private void savePoolHashrateNewEntity(ReturnedPoolData returnedPoolData, PoolDef poolDef, Long id, boolean errorPresent) throws IOException{

        PoolHashrate poolHashrate = new PoolHashrate();

        if (errorPresent)
            poolHashrate.setHashrate(-0.001);
        else
            poolHashrate.setHashrate(this.retrieveHashrateFromJsonString(returnedPoolData));

            poolHashrate.setPoolDef(poolDef);
            poolHashrate.setNetworkHashrate(networkHashrateRepository.findById(id).get());

        poolHashrateRepository.save(poolHashrate);
    }

    private void setPoolErrors(Integer i, Boolean errorPresent, List<String> calledApis){

        if(errorPresent) {
            poolErrorMap.get(calledApis.get(i)).incrementErrorCount();
            poolErrorMap.get(calledApis.get(i)).setExecutionDate();
        } else
            poolErrorMap.get(calledApis.get(i)).setErrorCount(0);
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

        return hashrate < 0 ? hashrate*1000.0 : hashrate/1000.0;
    }
}