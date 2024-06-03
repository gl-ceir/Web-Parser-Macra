package com.glocks.web_parser.dto;

import lombok.Data;

@Data
public class TrcQaFileDto {

    String no;
    String companyName;
    String phoneNumber;
    String email;

    public TrcQaFileDto(String[] headers) {
        this.no = headers[0].trim();
        this.companyName = headers[1].trim();
        this.phoneNumber = headers[2].trim();
        this.email = headers[3].trim();
    }
}
