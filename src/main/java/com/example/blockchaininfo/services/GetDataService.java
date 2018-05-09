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


        this.poolErrorMap = null;
    }

    @Scheduled(fixedRate = 5000)
    public void storeData() throws IOException, InterruptedException, ExecutionException{

            try {
                String hashrateFromApi = this.getJsonFromApi("http://public.turtlenode.io:11898/getinfo");
                OffsetDateTime date = OffsetDateTime.now();

                this.saveNetworkHashrateNewEntity(hashrateFromApi, date);
                this.connectToPoolApiAndStoreData(retrieveNetworkIdForPoolDefinition(date));
            } catch (HttpServerErrorException e){
                log.info("Network Server error e1: " + e);
            } catch (ResourceAccessException e2){
                log.info("Network resource access exception: " + e2);
            }
    }

    public Long retrieveNetworkIdForPoolDefinition(OffsetDateTime date){

        Optional<NetworkHashrate> networkHashrateOptional = networkHashrateRepository.findByDate(date);

        return networkHashrateOptional.isPresent() ? networkHashrateOptional.get().getId() : null;
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

    public void saveNetworkHashrateNewEntity(String hashrate_as_string, OffsetDateTime date){

        NetworkHashrate networkHashrate = new NetworkHashrate();
        networkHashrate.setHashrate(Double.parseDouble(hashrate_as_string));
        networkHashrate.setRepDate(date);

        networkHashrateRepository.save(networkHashrate);
    }

    public List<Path> getAllFilePathsInFolder(){

        List<Path> listOfFiles = new ArrayList<>();

        try {
            listOfFiles = Files.walk(Paths.get(ClassLoader.getSystemResource("static/json/networks/pools/").toURI()))
                    .filter(Files::isRegularFile)
                    .collect(toList());
        } catch (URISyntaxException e){
            log.info("" + e);
        } catch (IOException e2){
            log.info("" + e2);
        }

        return listOfFiles;
    }

    public PoolList getPoolsListFromJson() throws IOException{

        //List<PoolList> listOfPoolLists = new ArrayList<>();

      //  for(Path path : this.getAllFilePathsInFolder()) {

        //    InputStream jsonPoolsFile = new ClassPathResource(this.truncateFilePath(path)).getInputStream();
            PoolList poolList = this.jsonMapper.readValue(new File("/static/json/networks/pools/turtlecoin-pools.json"), PoolList.class);
          //  listOfPoolLists.add(poolList);

            //jsonPoolsFile.close();
        //}

        return poolList;
    }

    public String truncateFilePath(Path path){

        String helperString = path.toString();

        return helperString.substring(helperString.indexOf("static") - 1, helperString.length());
    }

    public void connectToPoolApiAndStoreData(Long id) throws IOException, InterruptedException, ExecutionException{

      //  List<PoolList> listOfPoolLists = this.getPoolsListFromJson();

        //for(PoolList poolList : listOfPoolLists)
            this.storePoolDataToDB(this.getPoolsListFromJson(), id);
    }

    public void storePoolDataToDB(PoolList poolList, Long id) throws IOException, InterruptedException{

        long started = System.currentTimeMillis();
        List<Future<ReturnedStringAndDate>> futureList = new ArrayList<>();


        try{
            futureList = executorService.invokeAll(this.createListOfCallableTasks(poolList, poolErrorMap));
        } catch (ResourceAccessException e){
            log.info("Pool resource access exception: " + e);
        } catch (HttpServerErrorException e2){
            log.info("Pool server error: " + e2);
        } catch (IllegalStateException e3){
            log.info("Pool server error: " + e3);
        }

        System.out.println("Time: " + (System.currentTimeMillis() - started));

        for (int i = 0; i < futureList.size(); i++){

            try {
                PoolDef poolDef = this.savePoolDefNewEntity(futureList.get(i).get().getDateTime(), poolList, i);
                this.savePoolHashrateNewEntity(futureList.get(i).get().getJsonString(), poolList, poolDef, id, i);
                poolErrorMap.get(poolDef.getName()).setErrorCount(0);

            } catch (ExecutionException e){
                poolErrorMap.get(poolList.getPoolList().get(i).getName()).incrementErrorCount();
                poolErrorMap.get(poolList.getPoolList().get(i).getName()).setExecutionDate();
                continue;
            }
        }
    }

    public List<Callable<ReturnedStringAndDate>> createListOfCallableTasks(PoolList poolList, Map<String, PoolExecutionData> executionDataMap){

        List<Callable<ReturnedStringAndDate>> callableList = new ArrayList<>();

        for (int i = 0; i < poolList.getPoolList().size(); i++){

            if (executionDataMap.get(poolList.getPoolList().get(i).getName()).getExecutionDate().isBefore(OffsetDateTime.now()))
                callableList.add(new ConnectToApiCallable(this.appendPoolApiUrl(poolList.getPoolList().get(i)), poolList.getPoolList().get(i).getName()));
            else
                continue;
        }

        return callableList;
    }

    //todo
    public Map<String, PoolExecutionData> initializePoolErrorMap(PoolList poolList){

        Map<String, PoolExecutionData> helperMap = new HashMap<>();

        for (PoolDefinition p : poolList.getPoolList())
            helperMap.put(p.getName(), new PoolExecutionData(OffsetDateTime.now().minusDays(1)));

        return helperMap;
    }

    public PoolDef savePoolDefNewEntity(OffsetDateTime date, PoolList poolList, int i){

        PoolDef poolDef = new PoolDef();
        poolDef.setDateFrom(date);
        poolDef.setName(poolList.getPoolList().get(i).getName());

        poolDefRepository.save(poolDef);

        return poolDef;
    }

    public void savePoolHashrateNewEntity(String jsonString, PoolList poolList, PoolDef poolDef, Long id, int i) throws IOException{

        PoolHashrate poolHashrate = new PoolHashrate();
        poolHashrate.setHashrate(this.retrieveHashrateFromJsonString(jsonString, poolList.getPoolList().get(i)));
        poolHashrate.setNetworkHashrate(networkHashrateRepository.findById(Long.valueOf(id)).get());
        poolHashrate.setPoolDef(poolDef);

        poolHashrateRepository.save(poolHashrate);
    }

    public double retrieveHashrateFromJsonString(String string, PoolDefinition poolDefinition) throws IOException{

        JsonNode jsonNode = jsonMapper.readTree(string);

        return checkIfPoolIsForknoteType(poolDefinition) ? jsonNode.get("pool").get("hashrate").asDouble() : jsonNode.get("pool_statistics").get("hashRate").asDouble();
    }

    public String appendPoolApiUrl(PoolDefinition poolDefinition){

        StringBuilder appendedApi = new StringBuilder(poolDefinition.getApi());

        return checkIfPoolIsForknoteType(poolDefinition) ? appendedApi.append("stats").toString() : appendedApi.append("pool/stats").toString();
    }

    public boolean checkIfPoolIsForknoteType(PoolDefinition poolDefinition){

        return poolDefinition.getType().equals("forknote") ? true : false;
    }
}
