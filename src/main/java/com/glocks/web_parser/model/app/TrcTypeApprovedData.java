package com.glocks.web_parser.model.app;


import com.glocks.web_parser.config.AppConfig;
import com.glocks.web_parser.dto.TrcTaFileDto;
import jakarta.persistence.*;
import lombok.CustomLog;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name="trc_type_approved_data")
public class TrcTypeApprovedData {



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="created_on")
    LocalDateTime createdOn;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)

    @Column(name="modified_on")
    LocalDateTime modifiedOn;

    @Column(name="no")
    int no;

    @Column(name="company")
    String company;

    @Column(name="trademark")
    String trademark;

    @Column(name="product_name")
    String productName;

    @Column(name="model")
    String model;

    @Column(name="country")
    String country;

    @Column(name="tx_frequency")
    String txFrequency;

    @Column(name="rx_frequency")
    String rxFrequency;

    @Column(name="trc_identifier")
    String trcIdentifier;

    @Column(name="type_of_equipment")
    String typeOfEquipment;

    @Column(name="approval_date")
    String approvalDate;

    public TrcTypeApprovedData(String[] taDataRecord) {
        this.no= Integer.parseInt(taDataRecord[0].trim());
        this.company=taDataRecord[1].trim();
        this.trademark=taDataRecord[2].trim();
        this.productName=taDataRecord[3].trim();
        this.model=taDataRecord[4].trim();
        this.country=taDataRecord[5].trim();
        this.txFrequency=taDataRecord[6].trim();
        this.rxFrequency=taDataRecord[7].trim();
        this.trcIdentifier=taDataRecord[8].trim();
        this.typeOfEquipment=taDataRecord[9].trim();
        this.approvalDate=taDataRecord[10].trim();
    }

    public TrcTypeApprovedData() {

    }
}
