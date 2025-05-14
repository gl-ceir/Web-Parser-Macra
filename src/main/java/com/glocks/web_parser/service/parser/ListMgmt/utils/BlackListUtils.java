package com.glocks.web_parser.service.parser.ListMgmt.utils;


import com.glocks.web_parser.builder.BlackListBuilder;
import com.glocks.web_parser.builder.BlackListHisBuilder;
import com.glocks.web_parser.config.AppConfig;
import com.glocks.web_parser.config.DbConfigService;
import com.glocks.web_parser.constants.CategoryType;
import com.glocks.web_parser.dto.ListMgmtDto;
import com.glocks.web_parser.model.app.*;
import com.glocks.web_parser.repository.app.BlackListHisRepository;
import com.glocks.web_parser.repository.app.BlackListRepository;
import com.glocks.web_parser.repository.app.ListDataMgmtRepository;

import com.glocks.web_parser.repository.app.SysParamRepository;
import com.glocks.web_parser.service.operatorSeries.OperatorSeriesService;
import com.glocks.web_parser.service.parser.ListMgmt.CommonFunctions;
import com.glocks.web_parser.service.parser.ListMgmt.db.DbClass;
import com.glocks.web_parser.validator.Validation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BlackListUtils {

    @Autowired
    Validation validation;

    @Autowired
    ListDataMgmtRepository listDataMgmtRepository;
    @Autowired
    AppConfig appConfig;

    @Autowired
    SysParamRepository sysParamRepository;

    @Autowired
    BlackListRepository blackListRepository;
    @Autowired
    BlackListHisRepository blackListHisRepository;
    @Autowired
    DbConfigService dbConfigService;
    @Autowired
    OperatorSeriesService operatorSeriesService;
    @Autowired
    DbClass dbClass;
    @Autowired
    CommonFunctions commonFunctions;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public boolean processBlackSingleAddEntry(ListDataMgmt listDataMgmt, ListMgmtDto record, int type, PrintWriter writer) {
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
            BlackList blackList = dbClass.getBlackListEntry(imsiEmpty, msisdnEmpty, imeiEmpty, imei, imsi, msisdn);
            // if present write in file and exit.
            if (blackList != null) {
                logger.info("The entry already exists {}", blackList);
                if (Objects.nonNull(blackList.getSource())) {
                    if (!blackList.getSource().equalsIgnoreCase(listDataMgmt.getCategory())) {
                        String source = commonFunctions.isSourceExist.apply(blackList.getSource(), listDataMgmt.getCategory());
                        dbClass.updateSource(source, blackList, "BLACK_LIST");
                    }
                }
                writer.println((msisdnEmpty ? "" : msisdn) + "," + (imsiEmpty ? "" : imsi) + "," + (imeiEmpty ? "" : imei) + "," + dbConfigService.getValue("msgForAlreadyExistsInBlackList"));
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
                blackList = type == 1 ? BlackListBuilder.forInsert(listDataMgmt, operatorName) : BlackListBuilder.forInsert(listDataMgmt, record, operatorName);

                ExceptionList exceptionList = dbClass.getExceptionListEntry(imsiEmpty, msisdnEmpty, imeiEmpty, imei,
                        imsi, msisdn);

                if (exceptionList != null) {
                    logger.info("The entry already exists in exception list {}", exceptionList);
                    writer.println((msisdnEmpty ? "" : msisdn) + "," + (imsiEmpty ? "" : imsi) + "," + (imeiEmpty ? "" : imei) + "," + dbConfigService.getValue("msgForAlreadyExistsInExceptionList"));
                    return false;
                }
                GreyList greyList = dbClass.getGreyListEntry(imsiEmpty, msisdnEmpty, imeiEmpty, imei, imsi, msisdn);
                if (greyList != null) {
                    logger.info("The entry already exists in grey list {}", greyList);
                    writer.println((msisdnEmpty ? "" : msisdn) + "," + (imsiEmpty ? "" : imsi) + "," + (imeiEmpty ? "" : imei) + "," + dbConfigService.getValue("msgForAlreadyExistsInGreyList"));
                    return false;
                }
                logger.info("Entry going to save in black list {}", blackList);
                blackList.setReason(commonFunctions.getValue("blackListAddReasonCode"));
                blackList.setClarifyReason(commonFunctions.getValue("blackListAddClarifyReason"));
                try {
                    blackListRepository.save(blackList);
                } catch (DataIntegrityViolationException e) {
                    logger.info("record already exist for  blackList {}", blackList);
                }

                BlackListHis blackListHisEntity = BlackListHisBuilder.forInsert(blackList, 1, listDataMgmt);
                try {
                    blackListHisRepository.save(blackListHisEntity);
                    logger.info("Entry save in black list his {}", blackListHisEntity);
                } catch (DataIntegrityViolationException e) {
                    logger.info("record already exist for  blackListHisEntity {}", blackListHisEntity.getId());
                }
//                writer.println(msisdn+","+imsi+","+imei+","+"ADDED");
                writer.println((msisdnEmpty ? "" : msisdn) + "," + (imsiEmpty ? "" : imsi) + "," + (imeiEmpty ? "" : imei) + "," + dbConfigService.getValue("msgForAddedInExceptionList"));

            }
            return true;
        } catch (Exception ex) {
            logger.error("Error while processing the entry for black list, for request {} and action {}, message {}",
                    listDataMgmt.getRequestType(), listDataMgmt.getAction(), ex.getMessage());
//            writer.println(msisdn+","+imsi+","+imei+","+"ENTRY_FAILED");
            writer.println((msisdnEmpty ? "" : msisdn) + "," + (imsiEmpty ? "" : imsi) + "," + (imeiEmpty ? "" : imei) + "," + dbConfigService.getValue("msgForEntryFailedInBlackList"));

            return false;
        }
    }

    public boolean processBlackSingleDelEntry(ListDataMgmt listDataMgmt, ListMgmtDto record, int type, PrintWriter writer) {
        String imsi = type == 1 ? listDataMgmt.getImsi() : record.getImsi();
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
            BlackList blackList = dbClass.getBlackListEntry(imsiEmpty, msisdnEmpty, imeiEmpty, imei, imsi, msisdn);
            boolean isvalidRequest = true;
            // if present write in file and exit.
            if (blackList != null) {
                logger.info("The entry already exists {}", blackList);
                //    String operatorName = operatorSeriesService.getOperatorName(imsiEmpty, msisdnEmpty, imsi, msisdn);
                int val = (int) commonFunctions.sourceCount.apply(blackList.getSource(), listDataMgmt.getCategory()).longValue();

                switch (val) {
                    case 0 -> {
                        logger.info("request reject due to invalid category {}", listDataMgmt.getCategory());
                        writer.println((msisdnEmpty ? "" : msisdn) + "," + (imsiEmpty ? "" : imsi) + "," + (imeiEmpty ? "" : imei) + "," + dbConfigService.getValue("msgForNotExistsInBlackList"));
                        isvalidRequest = false;
                    }

                    case 1 -> {
                        logger.info("Entry deleted in black list {}", blackList);
                        blackList.setReason(commonFunctions.getValue("blackListDelReasonCode"));
                        blackList.setClarifyReason(commonFunctions.getValue("blackListDelClarifyReason"));
                        blackListRepository.delete(blackList);
                        BlackListHis blackListHisEntity = BlackListHisBuilder.forInsert(blackList, 0, listDataMgmt);
                        logger.info("Entry save in black list his {}", blackListHisEntity);
                        blackListHisRepository.save(blackListHisEntity);
                    }
                    case 2 -> {
                        String source = commonFunctions.removeSource.apply(blackList.getSource(), listDataMgmt.getCategory());
                        logger.info("black_list updated with source {}", source);
                        dbClass.updateSource(source, blackList, "BLACK_LIST");
                    }
                }
                if (isvalidRequest) {
                    writer.println((msisdnEmpty ? "" : msisdn) + "," + (imsiEmpty ? "" : imsi) + "," + (imeiEmpty ? "" : imei) + "," + dbConfigService.getValue("msgForDeletedInBlackList"));
                }

            }
            // if present write in file and exit
            else {
                logger.info("The entry for msisdn {}, imsi {} and imei {} does not exist.", msisdn, imsi, imei);
//                writer.println(msisdn + "," + imsi + "," + imei + "," + "NOT_EXIST");
                writer.println((msisdnEmpty ? "" : msisdn) + "," + (imsiEmpty ? "" : imsi) + "," + (imeiEmpty ? "" : imei) + "," + dbConfigService.getValue("msgForNotExistsInBlackList"));

            }

            return true;
            //  return isvalidRequest;
        } catch (Exception ex) {
            logger.error("Error while processing the entry for black list, for request {} and action {}, message {}",
                    listDataMgmt.getRequestType(), listDataMgmt.getAction(), ex.getMessage());
//            writer.println(msisdn+","+imsi+","+imei+","+"ENTRY_FAILED");
            writer.println((msisdnEmpty ? "" : msisdn) + "," + (imsiEmpty ? "" : imsi) + "," + (imeiEmpty ? "" : imei) + "," + dbConfigService.getValue("msgForEntryFailedInBlackList"));

            return false;

        }
    }
}
