package com.example.blockchaininfo.pojos;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class PoolExecutionData {

    private String name;
    private Integer errorCount;
    private OffsetDateTime executionDate;

    public PoolExecutionData(OffsetDateTime executionDate, String name) {
        this.name = name;
        this.executionDate = executionDate;
        this.errorCount = 0;
    }

    public void incrementErrorCount(){

        if(this.errorCount > 7)
            this.errorCount = 7;
        else
            this.errorCount++;
    }

    public void setExecutionDate(){

        int multiplier = 0;

        switch (this.errorCount){

            case 0: multiplier = 0; break;
            case 1: multiplier = 0; break;
            case 2: multiplier = 5; break;
            case 3: multiplier = 10; break;
            case 4: multiplier = 15; break;
            case 5: multiplier = 60; break;
            case 6: multiplier = 150; break;
            case 7: multiplier = 514; break;
        }

        this.executionDate = OffsetDateTime.now().plusSeconds(this.errorCount*multiplier);
    }


}
