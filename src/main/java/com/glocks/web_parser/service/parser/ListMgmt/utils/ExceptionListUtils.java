package com.glocks.web_parser.service.parser.ListMgmt.utils;

import com.glocks.web_parser.builder.ExceptionListBuilder;
import com.glocks.web_parser.builder.ExceptionListHisBuilder;
import com.glocks.web_parser.config.AppConfig;
import com.glocks.web_parser.config.DbConfigService;
import com.glocks.web_parser.dto.ListMgmtDto;
import com.glocks.web_parser.model.app.*;
import com.glocks.web_parser.repository.app.ExceptionListHisRepository;
import com.glocks.web_parser.repository.app.ExceptionListRepository;
import com.glocks.web_parser.repository.app.ListDataMgmtRepository;
import com.glocks.web_parser.repository.app.SysParamRepository;
import com.glocks.web_parser.service.operatorSeries.OperatorSeriesService;
import com.glocks.web_parser.service.parser.ListMgmt.CommonFunctions;
import com.glocks.web_parser.service.parser.ListMgmt.db.DbClass;
import com.glocks.web_parser.validator.Validation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;

@Service
public class ExceptionListUtils {

    @Autowired
    Validation validation;

    @Autowired
    ListDataMgmtRepository listDataMgmtRepository;
    @Autowired
    AppConfig appConfig;

    @Autowired
    SysParamRepository sysParamRepository;


    @Autowired
    ExceptionListRepository exceptionListRepository;
    @Autowired
    ExceptionListHisRepository exceptionListHisRepository;
    @Autowired
    DbConfigService dbConfigService;
    @Autowired
    OperatorSeriesService operatorSeriesService;
    @Autowired
    DbClass dbClass;

    @Autowired
    CommonFunctions commonFunctions;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public boolean processExceptionSingleAddEntry(ListDataMgmt listDataMgmt, ListMgmtDto record, int type, PrintWriter writer) {
        String imsi = type == 1 ? listDataMgmt.getImsi() : record.getImsi().trim();
        String imei = type == 1 ? listDataMgmt.getImei() : record.getImei();
        String msisdn = type == 1 ? listDataMgmt.getMsisdn() : record.getMsisdn();
        boolean imsiEmpty = validation.isEmptyAndNull(imsi);
        boolean msisdnEmpty = validation.isEmptyAndNull(msisdn);
        boolean imeiEmpty = validation.isEmptyAndNull(imei);
        try {
            // search in list if already exists or not.

            if (!imsiEmpty) imsi = imsi.trim();
            if (!imeiEmpty) imei = imei.trim();
            if (!msisdnEmpty) msisdn = msisdn.trim();
            ExceptionList exceptionList = dbClass.getExceptionListEntry(imsiEmpty, msisdnEmpty, imeiEmpty, imei, imsi, msisdn);

            // if present write in file and exit.
            if (exceptionList != null) {
                logger.info("The entry already exists {}", exceptionList);
//                writer.println(msisdn+","+imsi+","+imei+","+"ALREADY_EXIST");
                writer.println((msisdnEmpty ? "" : msisdn) + "," + (imsiEmpty ? "" : imsi) + "," + (imeiEmpty ? "" : imei) + "," + dbConfigService.getValue("msgForAlreadyExistsInExceptionList"));
            }
            // if not present make entry in table
            else {
                logger.info("The entry for msisdn {}, imsi {} and imei {} does not exist.", msisdn, imsi, imei);
                String operatorName = operatorSeriesService.getOperatorName(imsiEmpty, msisdnEmpty, imsi, msisdn);
                if (validation.isEmptyAndNull(operatorName) && (!imsiEmpty || !msisdnEmpty)) { // operator name not found if imsi or msisdn is present.
                    logger.info("The operator name from operator series is not found.");
                    logger.error("The entry is failed.");
                    writer.println((msisdnEmpty ? "" : msisdn) + "," + (imsiEmpty ? "" : imsi) + "," + (imeiEmpty ? "" : imei) + "," + dbConfigService.getValue("msgForEntryFailedInBlackList"));
                    return false;
                }
                exceptionList = type == 1 ? ExceptionListBuilder.forInsert(listDataMgmt, operatorName) : ExceptionListBuilder.forInsert(listDataMgmt, record, operatorName);
                BlackList blackList = dbClass.getBlackListEntry(imsiEmpty, msisdnEmpty, imeiEmpty, imei,
                        imsi, msisdn);

                if (blackList != null) {
                    logger.info("The entry already exists in exception list {}", blackList);
                    writer.println((msisdnEmpty ? "" : msisdn) + "," + (imsiEmpty ? "" : imsi) + "," + (imeiEmpty ? "" : imei) + "," + dbConfigService.getValue("msgForAlreadyExistsInBlackList"));
                    return false;
                }
                GreyList greyList = dbClass.getGreyListEntry(imsiEmpty, msisdnEmpty, imeiEmpty, imei, imsi, msisdn);
                if (greyList != null) {
                    logger.info("The entry already exists in grey list {}", greyList);
                    writer.println((msisdnEmpty ? "" : msisdn) + "," + (imsiEmpty ? "" : imsi) + "," + (imeiEmpty ? "" : imei) + "," + dbConfigService.getValue("msgForAlreadyExistsInGreyList"));
                    return false;
                }
                logger.info("Entry save in exception list {}", exceptionList);
                exceptionList.setReason(commonFunctions.getValue("exceptionListAddReasonCode"));
                exceptionList.setClarifyReason(commonFunctions.getValue("exceptionListAddClarifyReason"));

                exceptionListRepository.save(exceptionList);
                ExceptionListHis exceptionListHisEntity = ExceptionListHisBuilder.forInsert(exceptionList, 1, listDataMgmt);
                logger.info("Entry save in exception list his {}", exceptionListHisEntity);
                exceptionListHisRepository.save(exceptionListHisEntity);
//                writer.println(msisdn+","+imsi+","+imei+","+"ADDED");
                writer.println((msisdnEmpty ? "" : msisdn) + "," + (imsiEmpty ? "" : imsi) + "," + (imeiEmpty ? "" : imei) + "," + dbConfigService.getValue("msgForAddedInExceptionList"));
            }
            return true;
        } catch (Exception ex) {
            logger.error("Error while processing the entry for exception list, for request {} and action {}, message {}",
                    listDataMgmt.getRequestType(), listDataMgmt.getAction(), ex.getMessage());
//            writer.println(msisdn+","+imsi+","+imei+","+"ENTRY_FAILED");
            writer.println((msisdnEmpty ? "" : msisdn) + "," + (imsiEmpty ? "" : imsi) + "," + (imeiEmpty ? "" : imei) + "," + dbConfigService.getValue("msgForEntryFailedInExceptionList"));

            return false;
        }
    }

    public boolean processExceptionSingleDelEntry(ListDataMgmt listDataMgmt, ListMgmtDto record, int type, PrintWriter writer) {
        String imsi = type == 1 ? listDataMgmt.getImsi() : record.getImsi().trim();
        String imei = type == 1 ? listDataMgmt.getImei() : record.getImei();
        String msisdn = type == 1 ? listDataMgmt.getMsisdn() : record.getMsisdn();
        boolean imsiEmpty = validation.isEmptyAndNull(imsi);
        boolean msisdnEmpty = validation.isEmptyAndNull(msisdn);
        boolean imeiEmpty = validation.isEmptyAndNull(imei);
        try {
            // search in list if already exists or not.

            if (!imsiEmpty) imsi = imsi.trim();
            if (!imeiEmpty) imei = imei.trim();
            if (!msisdnEmpty) msisdn = msisdn.trim();
            ExceptionList exceptionList = dbClass.getExceptionListEntry(imsiEmpty, msisdnEmpty, imeiEmpty, imei, imsi, msisdn);

            // if present write in file and exit.
            if (exceptionList != null) {
                logger.info("The entry exists {}", exceptionList);
                exceptionList.setReason(commonFunctions.getValue("exceptionListDelReasonCode"));
                exceptionList.setClarifyReason(commonFunctions.getValue("exceptionListDelClarifyReason"));
                exceptionListRepository.delete(exceptionList);
                logger.info("Entry deleted in exception list {}", exceptionList);
                ExceptionListHis exceptionListHisEntity = ExceptionListHisBuilder.forInsert(exceptionList, 0, listDataMgmt);
                logger.info("Entry save in exception list his {}", exceptionListHisEntity);
                exceptionListHisRepository.save(exceptionListHisEntity);
//                writer.println(msisdn + "," + imsi + "," + imei + "," + "DELETED");
                writer.println((msisdnEmpty ? "" : msisdn) + "," + (imsiEmpty ? "" : imsi) + "," + (imeiEmpty ? "" : imei) + "," + dbConfigService.getValue("msgForDeletedInExceptionList"));
            }
            // if present write in file and exit
            else {
                logger.info("The entry for msisdn {}, imsi {} and imei {} does not exist.", msisdn, imsi, imei);
//                writer.println(msisdn + "," + imsi + "," + imei + "," + "NOT_EXIST");
                writer.println((msisdnEmpty ? "" : msisdn) + "," + (imsiEmpty ? "" : imsi) + "," + (imeiEmpty ? "" : imei) + "," + dbConfigService.getValue("msgForNotExistsInExceptionList"));


            }
            return true;
        } catch (Exception ex) {
            logger.error("Error while processing the entry for exception list, for request {} and action {}, message {}",
                    listDataMgmt.getRequestType(), listDataMgmt.getAction(), ex.getMessage());
//            writer.println(msisdn+","+imsi+","+imei+","+"ENTRY_FAILED");
            writer.println((msisdnEmpty ? "" : msisdn) + "," + (imsiEmpty ? "" : imsi) + "," + (imeiEmpty ? "" : imei) + "," + dbConfigService.getValue("msgForEntryFailedInExceptionList"));

            return false;

        }
    }


}
