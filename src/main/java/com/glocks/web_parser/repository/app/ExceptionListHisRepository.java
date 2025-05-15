package com.glocks.web_parser.repository.app;


import com.glocks.web_parser.model.app.ExceptionListHis;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

@Repository
public interface ExceptionListHisRepository extends JpaRepository<ExceptionListHis, Integer> {

}
