package com.glocks.web_parser.model.app;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name="check_imei_req_detail")
public class CheckImeiReqDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="created_on")
    LocalDateTime createdOn;

    @Column(name="language")
    String language;

    @Column(name="imei")
    String imei;

    @Column(name="compliance_status")
    String complianceStatus;

    @Column(name="channel")
    String channel;

    @Column(name="request_id")
    String requestId;

    @Column(name="fail_process_description")
    String failProcessDescription;


}
