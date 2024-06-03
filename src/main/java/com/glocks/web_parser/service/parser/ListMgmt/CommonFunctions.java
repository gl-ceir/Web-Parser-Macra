package com.glocks.web_parser.service.parser.ListMgmt;

import com.glocks.web_parser.builder.*;
import com.glocks.web_parser.config.AppConfig;
import com.glocks.web_parser.config.DbConfigService;
import com.glocks.web_parser.constants.ConfigFlag;
import com.glocks.web_parser.dto.BlockedTacDto;
import com.glocks.web_parser.dto.ListMgmtDto;
import com.glocks.web_parser.model.app.*;
import com.glocks.web_parser.repository.app.*;
import com.glocks.web_parser.service.operatorSeries.OperatorSeriesService;
import com.glocks.web_parser.validator.Validation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.PrintWriter;
import java.time.LocalDateTime;

@Service
public class CommonFunctions {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    WebActionDbRepository webActionDbRepository;
    @Autowired
    Validation validation;
    @Autowired
    ListDataMgmtRepository listDataMgmtRepository;
    @Autowired
    AppConfig appConfig;
    @Autowired
    ExceptionListRepository exceptionListRepository;
    @Autowired
    ExceptionListHisRepository exceptionListHisRepository;

    @Autowired
    BlackListRepository blackListRepository;

    @Autowired
    BlackListHisRepository blackListHisRepository;
    @Autowired
    OperatorSeriesService operatorSeriesService;

    @Autowired
    BlockedTacListRepository blockedTacListRepository;
    @Autowired
    BlockedTacListHisRepository blockedTacListHisRepository;
    @Autowired
    DbConfigService dbConfigService;


    public boolean processExceptionSingleAddEntry(ListDataMgmt listDataMgmt, ListMgmtDto record, int type, PrintWriter writer) {
        String imsi = type == 1 ? listDataMgmt.getImsi() : record.getImsi().trim();
        String imei = type == 1 ?listDataMgmt.getImei() : record.getImei();
        String msisdn = type == 1 ?listDataMgmt.getMsisdn() : record.getMsisdn();
        boolean imsiEmpty = validation.isEmptyAndNull(imsi);
        boolean msisdnEmpty = validation.isEmptyAndNull(msisdn);
        boolean imeiEmpty = validation.isEmptyAndNull(imei);
        try {
            // search in list if already exists or not.

            if(!imsiEmpty) imsi = imsi.trim();
            if(!imeiEmpty) imei = imei.trim();
            if(!msisdnEmpty) msisdn = msisdn.trim();
            ExceptionList exceptionList = null;
            if (!imsiEmpty && !imeiEmpty && !msisdnEmpty) {
                exceptionList = exceptionListRepository.findExceptionListByImeiAndMsisdnAndImsi(imei.substring(0, 14), msisdn, imsi);
            } else if (!imeiEmpty && !imsiEmpty && msisdnEmpty) {
                exceptionList = exceptionListRepository.findExceptionListByImeiAndImsi(imei.substring(0, 14), imsi);
            } else if (!imeiEmpty && imsiEmpty && !msisdnEmpty) {
                exceptionList = exceptionListRepository.findExceptionListByImeiAndMsisdn(imei.substring(0, 14), msisdn);
            } else if (imeiEmpty && !imsiEmpty && !msisdnEmpty) {
                exceptionList = exceptionListRepository.findExceptionListByImsiAndMsisdn(imsi, msisdn);
            } else if (imeiEmpty && imsiEmpty && !msisdnEmpty) {
                exceptionList = exceptionListRepository.findExceptionListByMsisdn(msisdn);
            } else if (imeiEmpty && !imsiEmpty && msisdnEmpty) {
                exceptionList = exceptionListRepository.findExceptionListByImsi(imsi);
            } else if (!imeiEmpty && imsiEmpty && msisdnEmpty) {
                exceptionList = exceptionListRepository.findExceptionListByImei(imei.substring(0, 14));
            }
            // if present write in file and exit.
            if (exceptionList != null) {
                logger.info("The entry already exists {}", exceptionList);
//                writer.println(msisdn+","+imsi+","+imei+","+"ALREADY_EXIST");
                writer.println((msisdnEmpty ? "":msisdn)+","+(imsiEmpty ? "":imsi)+","+(imeiEmpty ? "":imei )+","+dbConfigService.getValue("msgForAlreadyExistsInExceptionList"));
            }
            // if not present make entry in table
            else {
                logger.info("The entry for msisdn {}, imsi {} and imei {} does not exist.", msisdn, imsi, imei);
                String operatorName = operatorSeriesService.getOperatorName(imsiEmpty, msisdnEmpty, imsi, msisdn);
                exceptionList = type == 1 ? ExceptionListBuilder.forInsert(listDataMgmt, operatorName) : ExceptionListBuilder.forInsert(listDataMgmt, record, operatorName);
                logger.info("Entry save in exception list {}",exceptionList);
                exceptionListRepository.save(exceptionList);
                ExceptionListHis exceptionListHisEntity = ExceptionListHisBuilder.forInsert(exceptionList, 1, listDataMgmt);
                logger.info("Entry save in exception list his {}", exceptionListHisEntity);
                exceptionListHisRepository.save(exceptionListHisEntity);
//                writer.println(msisdn+","+imsi+","+imei+","+"ADDED");
                writer.println((msisdnEmpty ? "":msisdn)+","+(imsiEmpty ? "":imsi)+","+(imeiEmpty ? "":imei )+","+dbConfigService.getValue("msgForAddedInExceptionList"));
            }
            return true;
        } catch (Exception ex) {
            logger.error("Error while processing the entry for exception list, for request {} and action {}, message {}",
                    listDataMgmt.getRequestType(), listDataMgmt.getAction(), ex.getMessage());
//            writer.println(msisdn+","+imsi+","+imei+","+"ENTRY_FAILED");
            writer.println((msisdnEmpty ? "":msisdn)+","+(imsiEmpty ? "":imsi)+","+(imeiEmpty ? "":imei )+","+dbConfigService.getValue("msgForEntryFailedInExceptionList"));

            return false;
        }
    }
    public boolean processExceptionSingleDelEntry(ListDataMgmt listDataMgmt, ListMgmtDto record, int type, PrintWriter writer) {
        String imsi = type == 1 ? listDataMgmt.getImsi() : record.getImsi().trim();
        String imei = type == 1 ?listDataMgmt.getImei() : record.getImei();
        String msisdn = type == 1 ?listDataMgmt.getMsisdn() : record.getMsisdn();
        boolean imsiEmpty = validation.isEmptyAndNull(imsi);
        boolean msisdnEmpty = validation.isEmptyAndNull(msisdn);
        boolean imeiEmpty = validation.isEmptyAndNull(imei);
        try {
            // search in list if already exists or not.

            if(!imsiEmpty) imsi = imsi.trim();
            if(!imeiEmpty) imei = imei.trim();
            if(!msisdnEmpty) msisdn = msisdn.trim();
            ExceptionList exceptionList = null;
            if (!imsiEmpty && !imeiEmpty && !msisdnEmpty) {
                exceptionList = exceptionListRepository.findExceptionListByImeiAndMsisdnAndImsi(imei.substring(0, 14), msisdn, imsi);
            } else if (!imeiEmpty && !imsiEmpty && msisdnEmpty) {
                exceptionList = exceptionListRepository.findExceptionListByImeiAndImsi(imei.substring(0, 14), imsi);
            } else if (!imeiEmpty && imsiEmpty && !msisdnEmpty) {
                exceptionList = exceptionListRepository.findExceptionListByImeiAndMsisdn(imei.substring(0, 14), msisdn);
            } else if (imeiEmpty && !imsiEmpty && !msisdnEmpty) {
                exceptionList = exceptionListRepository.findExceptionListByImsiAndMsisdn(imsi, msisdn);
            } else if (imeiEmpty && imsiEmpty && !msisdnEmpty) {
                exceptionList = exceptionListRepository.findExceptionListByMsisdn(msisdn);
            } else if (imeiEmpty && !imsiEmpty && msisdnEmpty) {
                exceptionList = exceptionListRepository.findExceptionListByImsi(imsi);
            } else if (!imeiEmpty && imsiEmpty && msisdnEmpty) {
                exceptionList = exceptionListRepository.findExceptionListByImei(imei.substring(0, 14));
            }
            // if present write in file and exit.
            if (exceptionList != null) {
                logger.info("The entry exists {}", exceptionList);
                exceptionListRepository.delete(exceptionList);
                logger.info("Entry deleted in exception list {}", exceptionList);
                ExceptionListHis exceptionListHisEntity = ExceptionListHisBuilder.forInsert(exceptionList, 0, listDataMgmt);
                logger.info("Entry save in exception list his {}", exceptionListHisEntity);
                exceptionListHisRepository.save(exceptionListHisEntity);
//                writer.println(msisdn + "," + imsi + "," + imei + "," + "DELETED");
                writer.println((msisdnEmpty ? "":msisdn)+","+(imsiEmpty ? "":imsi)+","+(imeiEmpty ? "":imei )+","+dbConfigService.getValue("msgForDeletedInExceptionList"));
            }
            // if present write in file and exit
            else {
                logger.info("The entry for msisdn {}, imsi {} and imei {} does not exist.", msisdn, imsi, imei);
//                writer.println(msisdn + "," + imsi + "," + imei + "," + "NOT_EXIST");
                writer.println((msisdnEmpty ? "":msisdn)+","+(imsiEmpty ? "":imsi)+","+(imeiEmpty ? "":imei )+","+dbConfigService.getValue("msgForNotExistsInExceptionList"));


            }
            return true;
        } catch (Exception ex) {
            logger.error("Error while processing the entry for exception list, for request {} and action {}, message {}",
                    listDataMgmt.getRequestType(), listDataMgmt.getAction(), ex.getMessage());
//            writer.println(msisdn+","+imsi+","+imei+","+"ENTRY_FAILED");
            writer.println((msisdnEmpty ? "":msisdn)+","+(imsiEmpty ? "":imsi)+","+(imeiEmpty ? "":imei )+","+dbConfigService.getValue("msgForEntryFailedInExceptionList"));

            return false;

        }
    }

    public boolean processBlackSingleAddEntry(ListDataMgmt listDataMgmt, ListMgmtDto record, int type, PrintWriter writer) {
        String imsi = type == 1 ? listDataMgmt.getImsi() : record.getImsi().trim();
        String imei = type == 1 ?listDataMgmt.getImei() : record.getImei();
        String msisdn = type == 1 ?listDataMgmt.getMsisdn() : record.getMsisdn();
        boolean imsiEmpty = validation.isEmptyAndNull(imsi);
        boolean msisdnEmpty = validation.isEmptyAndNull(msisdn);
        boolean imeiEmpty = validation.isEmptyAndNull(imei);
        try {
            // search in list if already exists or not.
            if(!imsiEmpty) imsi = imsi.trim();
            if(!imeiEmpty) imei = imei.trim();
            if(!msisdnEmpty) msisdn = msisdn.trim();
            BlackList blackList = null;
            if (!imsiEmpty && !imeiEmpty && !msisdnEmpty) {
                blackList = blackListRepository.findBlackListByImeiAndMsisdnAndImsi(imei.substring(0, 14), msisdn, imsi);
            } else if (!imeiEmpty && !imsiEmpty && msisdnEmpty) {
                blackList = blackListRepository.findBlackListByImeiAndImsi(imei.substring(0, 14), imsi);
            } else if (!imeiEmpty && imsiEmpty && !msisdnEmpty) {
                blackList = blackListRepository.findBlackListByImeiAndMsisdn(imei.substring(0, 14), msisdn);
            } else if (imeiEmpty && !imsiEmpty && !msisdnEmpty) {
                blackList = blackListRepository.findBlackListByImsiAndMsisdn(imsi, msisdn);
            } else if (imeiEmpty && imsiEmpty && !msisdnEmpty) {
                blackList = blackListRepository.findBlackListByMsisdn(msisdn);
            } else if (imeiEmpty && !imsiEmpty && msisdnEmpty) {
                blackList = blackListRepository.findBlackListByImsi(imsi);
            } else if (!imeiEmpty && imsiEmpty && msisdnEmpty) {
                blackList = blackListRepository.findBlackListByImei(imei.substring(0, 14));
            }
            // if present write in file and exit.
            if (blackList != null) {
                logger.info("The entry already exists {}", blackList);
//                writer.println(msisdn+","+imsi+","+imei+","+"ALREADY_EXIST");
                writer.println((msisdnEmpty ? "":msisdn)+","+(imsiEmpty ? "":imsi)+","+(imeiEmpty ? "":imei )+","+dbConfigService.getValue("msgForAlreadyExistsInBlackList"));


            }
            // if not present make entry in table
            else {
                logger.info("The entry for msisdn {}, imsi {} and imei {} does not exist.", msisdn, imsi, imei);
                String operatorName = operatorSeriesService.getOperatorName(imsiEmpty, msisdnEmpty, imsi, msisdn);
                blackList = type == 1 ? BlackListBuilder.forInsert(listDataMgmt, operatorName) : BlackListBuilder.forInsert(listDataMgmt, record, operatorName);

                logger.info("Entry save in black list {}",blackList);
                blackListRepository.save(blackList);
                BlackListHis blackListHisEntity = BlackListHisBuilder.forInsert(blackList, 1, listDataMgmt);
                logger.info("Entry save in black list his {}", blackListHisEntity);
                blackListHisRepository.save(blackListHisEntity);
//                writer.println(msisdn+","+imsi+","+imei+","+"ADDED");
                writer.println((msisdnEmpty ? "":msisdn)+","+(imsiEmpty ? "":imsi)+","+(imeiEmpty ? "":imei )+","+dbConfigService.getValue("msgForAddedInExceptionList"));

            }
            return true;
        } catch (Exception ex) {
            logger.error("Error while processing the entry for black list, for request {} and action {}, message {}",
                    listDataMgmt.getRequestType(), listDataMgmt.getAction(), ex.getMessage());
//            writer.println(msisdn+","+imsi+","+imei+","+"ENTRY_FAILED");
            writer.println((msisdnEmpty ? "":msisdn)+","+(imsiEmpty ? "":imsi)+","+(imeiEmpty ? "":imei )+","+ dbConfigService.getValue("msgForEntryFailedInBlackList"));

            return false;
        }
    }

    public boolean processBlackSingleDelEntry(ListDataMgmt listDataMgmt, ListMgmtDto record, int type, PrintWriter writer) {
        String imsi = type == 1 ? listDataMgmt.getImsi() : record.getImsi();
        String imei = type == 1 ?listDataMgmt.getImei() : record.getImei();
        String msisdn = type == 1 ?listDataMgmt.getMsisdn() : record.getMsisdn();
        boolean imsiEmpty = validation.isEmptyAndNull(imsi);
        boolean msisdnEmpty = validation.isEmptyAndNull(msisdn);
        boolean imeiEmpty = validation.isEmptyAndNull(imei);
        try {
            // search in list if already exists or not.
            if(!imsiEmpty) imsi = imsi.trim();
            if(!imeiEmpty) imei = imei.trim();
            if(!msisdnEmpty) msisdn = msisdn.trim();
            BlackList blackList = null;
            if (!imsiEmpty && !imeiEmpty && !msisdnEmpty) {
                blackList = blackListRepository.findBlackListByImeiAndMsisdnAndImsi(imei.substring(0, 14), msisdn, imsi);
            } else if (!imeiEmpty && !imsiEmpty && msisdnEmpty) {
                blackList = blackListRepository.findBlackListByImeiAndImsi(imei.substring(0, 14), imsi);
            } else if (!imeiEmpty && imsiEmpty && !msisdnEmpty) {
                blackList = blackListRepository.findBlackListByImeiAndMsisdn(imei.substring(0, 14), msisdn);
            } else if (imeiEmpty && !imsiEmpty && !msisdnEmpty) {
                blackList = blackListRepository.findBlackListByImsiAndMsisdn(imsi, msisdn);
            } else if (imeiEmpty && imsiEmpty && !msisdnEmpty) {
                blackList = blackListRepository.findBlackListByMsisdn(msisdn);
            } else if (imeiEmpty && !imsiEmpty && msisdnEmpty) {
                blackList = blackListRepository.findBlackListByImsi(imsi);
            } else if (!imeiEmpty && imsiEmpty && msisdnEmpty) {
                blackList = blackListRepository.findBlackListByImei(imei.substring(0, 14));
            }
            // if present write in file and exit.
            if (blackList != null) {
                logger.info("The entry already exists {}", blackList);
                String operatorName = operatorSeriesService.getOperatorName(imsiEmpty, msisdnEmpty, imsi, msisdn);
                logger.info("Entry deleted in black list {}", blackList);
                blackListRepository.delete(blackList);
                BlackListHis blackListHisEntity = BlackListHisBuilder.forInsert(blackList, 0, listDataMgmt);
                logger.info("Entry save in black list his {}", blackListHisEntity);
                blackListHisRepository.save(blackListHisEntity);
//                writer.println(msisdn + "," + imsi + "," + imei + "," + "DELETED");
                writer.println((msisdnEmpty ? "":msisdn)+","+(imsiEmpty ? "":imsi)+","+(imeiEmpty ? "":imei )+","+dbConfigService.getValue("msgForDeletedInBlackList"));

            }
            // if present write in file and exit
            else {
                logger.info("The entry for msisdn {}, imsi {} and imei {} does not exist.", msisdn, imsi, imei);
//                writer.println(msisdn + "," + imsi + "," + imei + "," + "NOT_EXIST");
                writer.println((msisdnEmpty ? "":msisdn)+","+(imsiEmpty ? "":imsi)+","+(imeiEmpty ? "":imei )+","+dbConfigService.getValue("msgForNotExistsInBlackList"));

            }
            return true;
        } catch (Exception ex) {
            logger.error("Error while processing the entry for black list, for request {} and action {}, message {}",
                    listDataMgmt.getRequestType(), listDataMgmt.getAction(), ex.getMessage());
//            writer.println(msisdn+","+imsi+","+imei+","+"ENTRY_FAILED");
            writer.println((msisdnEmpty ? "":msisdn)+","+(imsiEmpty ? "":imsi)+","+(imeiEmpty ? "":imei )+","+dbConfigService.getValue("msgForEntryFailedInBlackList"));

            return false;

        }
    }


    public boolean processBlockedTacDelEntry(ListDataMgmt listDataMgmt, BlockedTacDto record, int type, PrintWriter writer) {
        String tac = type == 1 ? listDataMgmt.getTac() : record.getTac();
        boolean tacEmpty = validation.isEmptyAndNull(tac);
        try {
            // search in list if already exists or not.
            if(!tacEmpty) tac = tac.trim();
            BlockedTacList blockedTacList = null;
            if (!tacEmpty) {
                blockedTacList = blockedTacListRepository.findBlockedTacListByTac(tac);
            }
            // if present write in file and exit.
            if (blockedTacList != null) {
                logger.info("The entry already exists {}", blockedTacList);
                blockedTacListRepository.delete(blockedTacList);
                logger.info("Entry deleted in blocked tac list {}", blockedTacList);
                BlockedTacListHis blockedTacListHisEntity = BlockedTacListHisBuilder.forInsert(blockedTacList, 0, listDataMgmt);
                blockedTacListHisRepository.save(blockedTacListHisEntity);
                logger.info("Entry save in blocked tac list his {}", blockedTacListHisEntity);
//                writer.println(tac + "," + "DELETED");
                writer.println((tacEmpty ? "":tac)+","+dbConfigService.getValue("msgForDeletedInBlockedTac"));

            }
            // if present write in file and exit
            else {
                logger.info("The entry for tac {} does not exist.", tac);
//                writer.println(tac + "," + "NOT_EXIST");
//                writer.println((msisdnEmpty ? "":msisdn)+","+(imsiEmpty ? "":imsi)+","+(imeiEmpty ? "":imei )+","+"NOT_EXIST");
                writer.println((tacEmpty ? "":tac)+","+dbConfigService.getValue("msgForNotExistsInBlockedTac"));
            }
            return true;
        } catch (Exception ex) {
            logger.error("Error while processing the entry for black list, for request {} and action {}, message {}",
                    listDataMgmt.getRequestType(), listDataMgmt.getAction(), ex.getMessage());
//            writer.println(tac +","+"ENTRY_FAILED");
//            writer.println((msisdnEmpty ? "":msisdn)+","+(imsiEmpty ? "":imsi)+","+(imeiEmpty ? "":imei )+","+"NOT_EXIST");
            writer.println((tacEmpty ? "":tac)+","+dbConfigService.getValue("msgForEntryFailedInBlockedTac"));
            return false;

        }
    }

    public boolean processBlockedTacAddEntry(ListDataMgmt listDataMgmt, BlockedTacDto record, int type, PrintWriter writer) {


        String tac = type == 1 ? listDataMgmt.getTac() : record.getTac();
        boolean tacEmpty = validation.isEmptyAndNull(tac);
        try {
            // search in list if already exists or not.
            if(!tacEmpty) tac = tac.trim();

            BlockedTacList blockedTacList = null;
            if (!tacEmpty) {
                blockedTacList = blockedTacListRepository.findBlockedTacListByTac(tac);
            }
            // if present write in file and exit.
            if (blockedTacList != null) {
                logger.info("The entry already exists {}", blockedTacList);
//                writer.println(tac+","+"ALREADY_EXIST");
//                writer.println((msisdnEmpty ? "":msisdn)+","+(imsiEmpty ? "":imsi)+","+(imeiEmpty ? "":imei )+","+"NOT_EXIST");
                writer.println((tacEmpty ? "":tac)+","+dbConfigService.getValue("msgForAlreadyExistsInBlockedTac"));
            }
            // if not present make entry in table
            else {
                logger.info("The entry for tac {}", tac);
//                String operatorName = operatorSeriesService.getOperatorName(imsi, msisdn);
                blockedTacList = type == 1 ? BlockedTacListBuilder.forInsert(listDataMgmt) : BlockedTacListBuilder.forInsert(listDataMgmt, record);

                logger.info("Entry save in blocked tac list {}",blockedTacList);
                blockedTacListRepository.save(blockedTacList);
                BlockedTacListHis blockedTacListHisEntity = BlockedTacListHisBuilder.forInsert(blockedTacList, 1, listDataMgmt);
                logger.info("Entry save in blocked tac list his {}", blockedTacListHisEntity);
                blockedTacListHisRepository.save(blockedTacListHisEntity);
//                writer.println(tac+","+"ADDED");
//                writer.println((msisdnEmpty ? "":msisdn)+","+(imsiEmpty ? "":imsi)+","+(imeiEmpty ? "":imei )+","+"NOT_EXIST");
                writer.println((tacEmpty ? "":tac)+","+dbConfigService.getValue("msgForAddedInBlockedTac"));
            }
            return true;
        } catch (Exception ex) {
            logger.error("Error while processing the entry for blocked tac list, for request {} and action {}, message {}",
                    listDataMgmt.getRequestType(), listDataMgmt.getAction(), ex.getMessage());
//            writer.println(tac+","+"ENTRY_FAILED");
//            writer.println((msisdnEmpty ? "":msisdn)+","+(imsiEmpty ? "":imsi)+","+(imeiEmpty ? "":imei )+","+"NOT_EXIST");
            writer.println((tacEmpty ? "":tac)+","+dbConfigService.getValue("msgForEntryFailedInBlockedTac"));
            return false;

        }
    }

    public void updateFailStatus(WebActionDb webActionDb, ListDataMgmt listDataMgmt) {
        webActionDbRepository.updateWebActionStatus(5, webActionDb.getId());
        listDataMgmtRepository.updateListDataMgmtStatus("FAIL", LocalDateTime.now(),listDataMgmt.getId());
//        alertService.raiseAnAlert(alertId, type, fileName, 0);
    }
    public void updateFailStatus(WebActionDb webActionDb, ListDataMgmt listDataMgmt, long totalCount,
                                 long successCount, long failureCount) {
        webActionDbRepository.updateWebActionStatus(5, webActionDb.getId());
        listDataMgmtRepository.updateListDataMgmtStatus("FAIL", LocalDateTime.now(),listDataMgmt.getId(),
                totalCount, successCount, failureCount);
//        alertService.raiseAnAlert(alertId, type, fileName, 0);
    }

    public void updateSuccessStatus(WebActionDb webActionDb, ListDataMgmt listDataMgmt) {
        webActionDbRepository.updateWebActionStatus(4, webActionDb.getId());
        listDataMgmtRepository.updateListDataMgmtStatus("DONE", LocalDateTime.now(), listDataMgmt.getId());
    }
    public void updateSuccessStatus(WebActionDb webActionDb, ListDataMgmt listDataMgmt, long totalCount,
                                    long successCount, long failureCount) {
        webActionDbRepository.updateWebActionStatus(4, webActionDb.getId());
        listDataMgmtRepository.updateListDataMgmtStatus("DONE", LocalDateTime.now(),
                listDataMgmt.getId(), totalCount, successCount, failureCount);
    }

    public String validateEntry(String imsi, String imei, String msisdn, String[] msisdnPrefix, String[] imsiPrefix) {

        boolean imsiEmpty = validation.isEmptyAndNull(imsi);
        boolean msisdnEmpty = validation.isEmptyAndNull(msisdn);
        boolean imeiEmpty = validation.isEmptyAndNull(imei);

        if(imsiEmpty && msisdnEmpty && imeiEmpty) {
            return ConfigFlag.msgForNullIMEIIMSIMSISDNInList.name();
        }
        if(!imeiEmpty) {
            if(validation.isLengthLess(imei, 14) || validation.isLengthMore(imei, 16))  {
                return ConfigFlag.msgForLengthValidationIMEIInList.name();
            }
            if(!validation.isNumeric(imei)) {
                return ConfigFlag.msgForNonNumericIMEIInList.name();
            }
        }
        if(!imsiEmpty) {
            for(int i=0;i<imsiPrefix.length;i++) {

                if(!imsi.startsWith(imsiPrefix[i])) {
                    return ConfigFlag.msgForPrefixIMSIInList.name();
                }
//                if(imsi.isBlank() || !imsi.matches("\\d+")) {
//                    flagImsiNull = true;
//                }
            }
//            if(!validation.isPrefix(imsi, "456")) {
//                return "IMSI does not starts with prefix 456";
//            }
            if(!validation.isNumeric(imsi)) {
                return ConfigFlag.msgForNonNumericIMSIInList.name();
            }
        }
        if(!msisdnEmpty) {
            for(int i=0;i<msisdnPrefix.length;i++) {

                if(!msisdn.startsWith(msisdnPrefix[i])) {
                    return ConfigFlag.msgForPrefixMSISDNInList.name();
                }
//                if(imsi.isBlank() || !imsi.matches("\\d+")) {
//                    flagImsiNull = true;
//                }
            }
//            if(!validation.isPrefix(msisdn, "855")) {
//                return "MSISDN does not starts with prefix 855";
//            }
            if(!validation.isNumeric(msisdn)) {
                return ConfigFlag.msgForNonNumericMSISDNInList.name();
            }
            if(!operatorSeriesService.validLengthMsisdn(msisdn)) {
                return ConfigFlag.msgForLengthValidationMSISDNInList.name();
            }

        }
        return "";
    }

    public String validateEntry(String tac) {
        boolean tacEmpty = validation.isEmptyAndNull(tac);
        if(tacEmpty) {
            return ConfigFlag.msgForNullTACInList.name();
        } else {

            if(!validation.isLengthEqual(tac, 8))  {
                return ConfigFlag.msgForLengthValidationTACInList.name();
            }
            if(!validation.isNumeric(tac)) {
                return ConfigFlag.msgForNonNumericTACInList.name();
            }
        }
        return "";
    }

}
