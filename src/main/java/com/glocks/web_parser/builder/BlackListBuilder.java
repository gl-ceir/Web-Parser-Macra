package com.glocks.web_parser.builder;


import com.glocks.web_parser.dto.ListMgmtDto;
import com.glocks.web_parser.model.app.BlackList;
import com.glocks.web_parser.model.app.ExceptionList;
import com.glocks.web_parser.model.app.ListDataMgmt;
import lombok.Builder;
import org.springframework.stereotype.Component;

@Component
@Builder
public class BlackListBuilder {


    public static BlackList forInsert(ListDataMgmt listDataMgmt, String operatorName) {
        BlackList blackList = new BlackList();

        blackList.setImei(listDataMgmt.getImei() == null ? null : listDataMgmt.getImei().substring(0,14));
        blackList.setImsi(listDataMgmt.getImsi());
        blackList.setMsisdn(listDataMgmt.getMsisdn());
        blackList.setRemarks(listDataMgmt.getRemarks());
        blackList.setActualImei(listDataMgmt.getImei());
        blackList.setModeType(listDataMgmt.getRequestMode());
        blackList.setOperatorName(operatorName);
//        exceptionList.setOperatorId(exceptionList.getOperatorId());
        blackList.setRequestType(listDataMgmt.getCategory());
        blackList.setTxnId(listDataMgmt.getTransactionId());
        blackList.setUserId(listDataMgmt.getUserId());
//        exceptionList.setUserType(listDataMgmt.getUserType());
        blackList.setTac(listDataMgmt.getImei() == null ? null : listDataMgmt.getImei().substring(0,8));
        blackList.setSource("EIRSAdmin");
        return blackList;
    }
    public static BlackList forInsert(ListDataMgmt listDataMgmt, ListMgmtDto listMgmtDto, String operatorName) {
        BlackList blackList = new BlackList();
        blackList.setImei(listMgmtDto.getImei() == null ? null : listMgmtDto.getImei().substring(0,14));
        blackList.setImsi(listMgmtDto.getImsi());
        blackList.setMsisdn(listMgmtDto.getMsisdn());
        blackList.setRemarks(listDataMgmt.getRemarks());
        blackList.setActualImei(listMgmtDto.getImei());
        blackList.setModeType(listDataMgmt.getRequestMode());
        blackList.setOperatorName(operatorName);
//        exceptionList.setOperatorId(exceptionList.getOperatorId());
        blackList.setRequestType(listDataMgmt.getCategory());
        blackList.setTxnId(listDataMgmt.getTransactionId());
        blackList.setUserId(listDataMgmt.getUserId());
//        exceptionList.setUserType(listDataMgmt.getUserType());
        blackList.setTac(listMgmtDto.getImei() == null ? null : listMgmtDto.getImei().substring(0,8));
        blackList.setSource("EIRSAdmin");
        return blackList;
    }
}
