package com.example.blockchaininfo.services;

import com.example.blockchaininfo.model.NetworkHashrate;
import com.example.blockchaininfo.model.PoolDef;
import com.example.blockchaininfo.model.PoolHashrate;
import com.example.blockchaininfo.pojos.*;
import com.example.blockchaininfo.repositories.NetworkHashrateRepository;
import com.example.blockchaininfo.repositories.PoolDefRepository;
import com.example.blockchaininfo.repositories.PoolHashrateRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import javax.print.URIException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    private List<Future<PoolStatus>> futureList;
    private List<PoolStatus> poolStatusList;

    public GetDataService(NetworkHashrateRepository networkHashrateRepository, PoolDefRepository poolDefRepository, PoolHashrateRepository poolHashrateRepository, ObjectMapper jsonMapper) {
        this.networkHashrateRepository = networkHashrateRepository;
        this.poolDefRepository = poolDefRepository;
        this.poolHashrateRepository = poolHashrateRepository;
        this.jsonMapper = jsonMapper;
        this.executorService = Executors.newCachedThreadPool();

        this.futureList = new ArrayList<>();
        this.poolStatusList = this.poolStatusList();
    }

    public NetworkList getNetworkListFromJson() throws IOException {

        InputStream jsonNetworkDetailsFile = new ClassPathResource("/static/json/networks/network-list.json").getInputStream();
        NetworkList networkList = this.jsonMapper.readValue(jsonNetworkDetailsFile, NetworkList.class);
        jsonNetworkDetailsFile.close();

        return networkList;
    }

    @Scheduled(fixedRate = 5000)
    public void storeData() throws IOException, URISyntaxException, InterruptedException, ExecutionException{

        OffsetDateTime date = OffsetDateTime.now();

        for(NetworkDefinition networkDefinition : this.getNetworkListFromJson().getNetworkList()){

            String hashrateFromApi;

            try {
                hashrateFromApi = this.getJsonFromApi(networkDefinition.getApi_url());
            } catch (HttpServerErrorException e){
                log.info("Network Server error e1: " + e);
                break;
            } catch (ResourceAccessException e2){
                log.info("Network resource access exception: " + e2);
                break;
            }

            //this.saveNetworkHashrateNewEntity(hashrateFromApi, date);
            this.connectToPoolApiAndStoreData(retrieveNetworkIdForPoolDefinition(date));
        }
    }

    public Long retrieveNetworkIdForPoolDefinition(OffsetDateTime date){

        Optional<NetworkHashrate> networkHashrateOptional = networkHashrateRepository.findByDate(date);

        return networkHashrateOptional.isPresent() ? networkHashrateOptional.get().getId() : null;
    }

    public String getJsonFromApi(String url){

        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(HttpClientBuilder.create().build());
        clientHttpRequestFactory.setConnectTimeout(2000);
        clientHttpRequestFactory.setReadTimeout(2000);
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);

        String helperString = restTemplate.getForObject(url, String.class);

        return helperString;
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

    public List<PoolList> getPoolsListFromJson() throws IOException{

        List<PoolList> listOfPoolLists = new ArrayList<>();

        for(Path path : this.getAllFilePathsInFolder()) {

            InputStream jsonPoolsFile = new ClassPathResource(this.truncateFilePath(path)).getInputStream();
            PoolList poolList = this.jsonMapper.readValue(jsonPoolsFile, PoolList.class);

            listOfPoolLists.add(poolList);

            jsonPoolsFile.close();
        }

        return listOfPoolLists;
    }

    public String truncateFilePath(Path path){

        String helperString = path.toString();

        return helperString.substring(helperString.indexOf("static") - 1, helperString.length());
    }

    public void connectToPoolApiAndStoreData(Long id) throws IOException, URISyntaxException, InterruptedException, ExecutionException{

        List<PoolList> listOfPoolLists = this.getPoolsListFromJson();

        for(PoolList poolList : listOfPoolLists)
            this.storePoolDataToDB(poolList, id);
    }

    public void storePoolDataToDB(PoolList poolList, Long id) throws IOException, InterruptedException, ExecutionException{

        long started = System.currentTimeMillis();

        futureList = executorService.invokeAll(createCallableList(poolList, futureList));
        //System.out.println("Active threads: " + Thread.activeCount());

        for (Future<PoolStatus> f : futureList ){

            try {
                System.out.println(f.get());
            } catch (ExecutionException e) {
                log.info("Execution Exception: " + e);
            } catch (InterruptedException e2){
                log.info("Interrupted Exception: " + e2);
            }
        }

        System.out.println("Time: " + (System.currentTimeMillis() - started));
    }

    public List<PoolStatus> poolStatusList (){

        List<PoolStatus> poolStatusList = new ArrayList<>();
        PoolList poolList = new PoolList();

        try {
            poolList = this.getPoolsListFromJson().get(0);
        } catch (IOException e){
            log.info("" + e);
        }

        for (int i = 0; i < poolList.getPoolList().size(); i++){

            PoolStatus poolStatus = new PoolStatus();
            poolStatus.setName(poolList.getPoolList().get(i).getName());

            poolStatusList.add(poolStatus);
        }

        return poolStatusList;
    }

    public List<Callable<PoolStatus>> createCallableList (PoolList poolList, List<Future<PoolStatus>> futureList, List<PoolStatus> poolStatusList){

        List<Callable<PoolStatus>> callableList = new ArrayList<>();

        if(futureList.isEmpty())
            for (int i = 0; i < poolList.getPoolList().size(); i++)
                 callableList.add(new ConnectToApiCallable(this.appendPoolApiUrl(poolList.getPoolList().get(i)), poolList.getPoolList().get(i).getName()));
        else{
            for (int i = 0; i < futureList.size(); i++)
                futureList.get(i).get().getStatus() != Status.OK ?
        }
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

        PoolJson poolJson = this.jsonMapper.readValue(string, PoolJson.class);

        return checkIfPoolIsForknoteType(poolDefinition) ? poolJson.getPoolForknote().getHashrate() : poolJson.getPool_nodeJs().getHashRate();
    }

    public String appendPoolApiUrl(PoolDefinition poolDefinition){

        StringBuilder appendedApi = new StringBuilder(poolDefinition.getApi());

        return checkIfPoolIsForknoteType(poolDefinition) ? appendedApi.append("stats").toString() : appendedApi.append("pool/stats").toString();
    }

    public boolean checkIfPoolIsForknoteType(PoolDefinition poolDefinition){

        return poolDefinition.getType().equals("forknote") ? true : false;
    }
}
