package com.glocks.web_parser.repository.app;

import com.glocks.web_parser.model.app.BlackList;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Repository
public interface BlackListRepository extends JpaRepository<BlackList, Integer> {


    BlackList findBlackListByImeiAndMsisdnAndImsi(String imei, String msisdn, String imsi);

    BlackList findBlackListByImeiAndImsi(String imei, String imsi);

    BlackList findBlackListByImeiAndMsisdn(String imei, String msisdn);

    BlackList findBlackListByImsiAndMsisdn(String imsi, String msisdn);

    BlackList findBlackListByImsi(String imsi);

    BlackList findBlackListByImei(String imei);

    BlackList findBlackListByMsisdn(String msisdn);

    @Transactional(rollbackOn = {SQLException.class})
    @Modifying
    @Query("UPDATE BlackList x SET x.source =:source WHERE x.imei =:imei AND x.imsi =:imsi AND x.msisdn=:msisdn")
    public int updateSource(String source, String imei, String imsi, String msisdn);

}
