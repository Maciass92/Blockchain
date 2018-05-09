package com.example.blockchaininfo.pojos;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class PoolExecutionData {

    private Integer errorCount;
    private OffsetDateTime executionDate;

    public PoolExecutionData(OffsetDateTime executionDate) {
        this.executionDate = executionDate;
        this.errorCount = 0;
    }

    public void incrementErrorCount(){
        this.errorCount++;
    }

    public void setExecutionDate(){

        this.executionDate = OffsetDateTime.now().plusSeconds(this.errorCount*60);
    }
}
