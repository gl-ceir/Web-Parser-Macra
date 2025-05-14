package com.glocks.web_parser.repository.app;

import com.glocks.web_parser.model.app.BlackListHis;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

@Repository
public interface BlackListHisRepository extends JpaRepository<BlackListHis, Integer> {
    @Transactional(rollbackOn = {SQLException.class})
    @Modifying
    @Query("UPDATE BlackListHis x SET x.source =:source WHERE x.imei =:imei")
    public int updateSource(String source, String imei);
}
