package com.glocks.web_parser.model.app;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name="grey_list")
public class GreyList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @Column(name="imei")
    String imei;
    @Column(name="imsi")
    String imsi;

    @Column(name="msisdn")
    String msisdn;


}
