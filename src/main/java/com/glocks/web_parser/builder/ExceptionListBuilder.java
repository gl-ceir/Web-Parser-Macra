package com.glocks.web_parser.builder;


import com.glocks.web_parser.dto.ListMgmtDto;
import com.glocks.web_parser.model.app.ExceptionList;
import com.glocks.web_parser.model.app.ListDataMgmt;
import lombok.Builder;
import org.springframework.stereotype.Component;

//import static com.gl.eirs.simchange.constants.Constants.remarks;

@Component
@Builder
public class ExceptionListBuilder {

    public static ExceptionList forInsert(ListDataMgmt listDataMgmt, String operatorName) {
        ExceptionList exceptionList = new ExceptionList();
        exceptionList.setImei(((listDataMgmt.getImei() == null) || (listDataMgmt.getImei().equalsIgnoreCase(""))) ? null : listDataMgmt.getImei().substring(0,14));
        exceptionList.setImsi(listDataMgmt.getImsi());
        exceptionList.setMsisdn(listDataMgmt.getMsisdn());
        exceptionList.setRemarks(listDataMgmt.getRemarks());
        exceptionList.setActualImei(listDataMgmt.getImei());
        exceptionList.setModeType(listDataMgmt.getRequestMode());
        exceptionList.setOperatorName(operatorName);
//        exceptionList.setOperatorId(exceptionList.getOperatorId());
        exceptionList.setRequestType(listDataMgmt.getCategory());
        exceptionList.setTxnId(listDataMgmt.getTransactionId());
        exceptionList.setUserId(listDataMgmt.getUserId());
//        exceptionList.setUserType(listDataMgmt.getUserType());
        exceptionList.setTac(((listDataMgmt.getImei() == null) || (listDataMgmt.getImei().equalsIgnoreCase(""))) ? null : listDataMgmt.getImei().substring(0,8));
        exceptionList.setSource("EIRSAdmin");
        return exceptionList;
    }

    public static ExceptionList forInsert(ListDataMgmt listDataMgmt, ListMgmtDto listMgmtDto, String operatorName) {
        ExceptionList exceptionList = new ExceptionList();
        exceptionList.setImei(((listMgmtDto.getImei() == null) || (listMgmtDto.getImei().equalsIgnoreCase(""))) ? null : listMgmtDto.getImei().substring(0,14));
        exceptionList.setImsi(listMgmtDto.getImsi());
        exceptionList.setMsisdn(listMgmtDto.getMsisdn());
        exceptionList.setRemarks(listDataMgmt.getRemarks());
        exceptionList.setActualImei(listMgmtDto.getImei());
        exceptionList.setModeType(listDataMgmt.getRequestMode());
        exceptionList.setOperatorName(operatorName);
//        exceptionList.setOperatorId(exceptionList.getOperatorId());
        exceptionList.setRequestType(listDataMgmt.getCategory());
        exceptionList.setTxnId(listDataMgmt.getTransactionId());
        exceptionList.setUserId(listDataMgmt.getUserId());
//        exceptionList.setUserType(listDataMgmt.getUserType());
        exceptionList.setTac(((listMgmtDto.getImei() == null) || (listMgmtDto.getImei().equalsIgnoreCase(""))) ? null : listMgmtDto.getImei().substring(0,8));
        exceptionList.setSource("EIRSAdmin");
        return exceptionList;
    }

}
