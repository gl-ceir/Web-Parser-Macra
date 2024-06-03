package com.glocks.web_parser.dto;


import lombok.Data;
import org.springframework.stereotype.Service;

@Data
public class TrcTaFileDto {


    String no;
    String company;
    String trademark;
    String productName;
    String modelName;
    String country;
    String txFrequency;
    String rxFrequency;
    String trcIdentifier;
    String typeOfEquipment;
    String approvalDate;

    public TrcTaFileDto(String[] headers) {
        this.no = headers[0].trim();
        this.company = headers[1].trim();
        this.trademark = headers[2].trim();
        this.productName = headers[3].trim();
        this.modelName = headers[4].trim();
        this.country = headers[5].trim();
        this.txFrequency = headers[6].trim();
        this.rxFrequency = headers[7].trim();
        this.trcIdentifier = headers[8].trim();
        this.typeOfEquipment = headers[9].trim();
        this.approvalDate = headers[10].trim();
    }
}
