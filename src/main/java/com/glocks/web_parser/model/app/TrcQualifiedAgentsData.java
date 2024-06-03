package com.glocks.web_parser.model.app;


import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name="trc_qualified_agent_data")
public class TrcQualifiedAgentsData {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
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

    @Column(name="company_name")
    String companyName;

    @Column(name="phone_number")
    String phoneNumber;

    @Column(name="email")
    String email;


    public TrcQualifiedAgentsData(String[] taDataRecord) {
        this.no= Integer.parseInt(taDataRecord[0].trim());
        this.companyName=taDataRecord[1].trim();
        this.phoneNumber=taDataRecord[2].trim();
        this.email=taDataRecord[3].trim();
    }

    public TrcQualifiedAgentsData() {

    }
}
