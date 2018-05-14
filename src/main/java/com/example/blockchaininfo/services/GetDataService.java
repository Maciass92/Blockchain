package com.example.blockchaininfo.services;

import com.example.blockchaininfo.model.NetworkHashrate;
import com.example.blockchaininfo.model.PoolDef;
import com.example.blockchaininfo.model.PoolHashrate;
import com.example.blockchaininfo.pojos.*;
import com.example.blockchaininfo.repositories.NetworkHashrateRepository;
import com.example.blockchaininfo.repositories.PoolDefRepository;
import com.example.blockchaininfo.repositories.PoolHashrateRepository;
import com.example.blockchaininfo.services.ConnectToApiCallable;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.HttpClientBuilder;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

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

            } catch (HttpServerErrorException e){
                log.info("Network Server error e1: " + e);
            } catch (ResourceAccessException e2){
                log.info("Network resource access exception: " + e2);
            } catch (IOException e3) {
                log.info("IOException thrown");
            } catch (InterruptedException e4){
                log.info("Interrupted Exception thrown");
            }
    }

    private String getNetworkHashrateFromApi(String url){

        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(HttpClientBuilder.create().build());
        clientHttpRequestFactory.setConnectTimeout(7000);
        clientHttpRequestFactory.setReadTimeout(7000);
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);

        ObjectNode objectNode = restTemplate.getForObject(url, ObjectNode.class);
        JsonNode jsonNode = objectNode.get("hashrate");

        return jsonNode.asText();
    }

    private Long retrieveNetworkIdForPoolDefinition(OffsetDateTime date){

        Optional<NetworkHashrate> networkHashrateOptional = networkHashrateRepository.findByDate(date);

        return networkHashrateOptional.isPresent() ? networkHashrateOptional.get().getId() : null;
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

        try{
            futureList = executorService.invokeAll(this.createListOfCallableTasks(poolList));
            System.out.println("Active threads: " + Thread.activeCount());
        } catch (ResourceAccessException e){
            log.info("" + e);
        } catch (HttpServerErrorException e2){
            log.info("" + e2);
        } catch (IllegalStateException e3){
            log.info("" + e3);
        }

        this.processDataAndResolvePoolErrors(futureList, listOfNames, id);
    }

    private void processDataAndResolvePoolErrors(List<Future<ReturnedPoolData>> futureList, List<String> listOfNames, Long id) throws InterruptedException, IOException{

        for (int i = 0; i < futureList.size(); i++){
            try {
                PoolDef poolDef = this.savePoolDefNewEntity(futureList.get(i).get());
                this.savePoolHashrateNewEntity(futureList.get(i).get(), poolDef, id);
                poolErrorMap.get(poolDef.getName()).setErrorCount(0);

            } catch (ExecutionException e){
                log.info("" + e);
                poolErrorMap.get(listOfNames.get(i)).incrementErrorCount();
                poolErrorMap.get(listOfNames.get(i)).setExecutionDate();
                continue;
            }
        }
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

    private List<String> createListOfPoolNames(PoolList poolList){

        List<String> namesOrdered = new ArrayList<>();

        for (int i = 0; i < poolList.getPoolList().size(); i++){
            if (isTaskExecutable(poolList.getPoolList().get(i).getName()))
                namesOrdered.add(poolList.getPoolList().get(i).getName());
        }

        return namesOrdered;
    }

    private boolean isTaskExecutable(String name){

        return this.poolErrorMap.get(name).getExecutionDate().isBefore(OffsetDateTime.now());
    }

    private PoolDef savePoolDefNewEntity(ReturnedPoolData returnedPoolData){

        PoolDef poolDef = new PoolDef();
        poolDef.setDateFrom(returnedPoolData.getDateTime());
        poolDef.setName(returnedPoolData.getPoolName());

        poolDefRepository.save(poolDef);

        return poolDef;
    }

    private void savePoolHashrateNewEntity(ReturnedPoolData returnedPoolData, PoolDef poolDef, Long id) throws IOException{

        PoolHashrate poolHashrate = new PoolHashrate();
        poolHashrate.setHashrate(this.retrieveHashrateFromJsonString(returnedPoolData));
        poolHashrate.setNetworkHashrate(networkHashrateRepository.findById(id).get());
        poolHashrate.setPoolDef(poolDef);

        poolHashrateRepository.save(poolHashrate);
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
