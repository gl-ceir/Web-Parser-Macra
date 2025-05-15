package com.glocks.web_parser.service.parser.ListMgmt.db;

import com.glocks.web_parser.model.app.BlackList;
import com.glocks.web_parser.model.app.BlockedTacList;
import com.glocks.web_parser.model.app.ExceptionList;
import com.glocks.web_parser.model.app.GreyList;
import com.glocks.web_parser.repository.app.*;
import com.glocks.web_parser.validator.Validation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DbClass {
    private final Logger logger = LogManager.getLogger(this.getClass());
    @Autowired
    BlackListRepository blackListRepository;
    @Autowired
    GreyListRepository greyListRepository;
    @Autowired
    ExceptionListRepository exceptionListRepository;

    @Autowired
    BlockedTacListRepository blockedTacListRepository;

    @Autowired
    ExceptionListHisRepository exceptionListHisRepository;

    @Autowired
    BlackListHisRepository blackListHisRepository;

    @Autowired
    Validation validation;

    public GreyList getGreyListEntry(boolean imsiEmpty, boolean msisdnEmpty, boolean imeiEmpty, String imei, String imsi, String msisdn) {
        GreyList greyList = null;
        if (!imsiEmpty && !imeiEmpty && !msisdnEmpty) {
            greyList = greyListRepository.findGreyListByImeiAndMsisdnAndImsi(imei.substring(0, 14), msisdn, imsi);
        } else if (!imeiEmpty && !imsiEmpty && msisdnEmpty) {
            greyList = greyListRepository.findGreyListByImeiAndImsi(imei.substring(0, 14), imsi);
        } else if (!imeiEmpty && imsiEmpty && !msisdnEmpty) {
            greyList = greyListRepository.findGreyListByImeiAndMsisdn(imei.substring(0, 14), msisdn);
        } else if (imeiEmpty && !imsiEmpty && !msisdnEmpty) {
            greyList = greyListRepository.findGreyListByImsiAndMsisdn(imsi, msisdn);
        } else if (imeiEmpty && imsiEmpty && !msisdnEmpty) {
            greyList = greyListRepository.findGreyListByMsisdn(msisdn);
        } else if (imeiEmpty && !imsiEmpty && msisdnEmpty) {
            greyList = greyListRepository.findGreyListByImsi(imsi);
        } else if (!imeiEmpty && imsiEmpty && msisdnEmpty) {
            greyList = greyListRepository.findGreyListByImei(imei.substring(0, 14));
        }
        return greyList;

    }

    public BlackList getBlackListEntry(boolean imsiEmpty, boolean msisdnEmpty, boolean imeiEmpty, String imei, String imsi, String msisdn) {
        BlackList blackList = null;
        if (!imsiEmpty && !imeiEmpty && !msisdnEmpty) {
            blackList = blackListRepository.findBlackListByImeiAndMsisdnAndImsi(imei.substring(0, 14), msisdn, imsi);
        } else if (!imeiEmpty && !imsiEmpty && msisdnEmpty) {
            blackList = blackListRepository.findBlackListByImeiAndImsiAndMsisdnIsNull(imei.substring(0, 14), imsi);
        } else if (!imeiEmpty && imsiEmpty && !msisdnEmpty) {
            blackList = blackListRepository.findBlackListByImeiAndMsisdnAndImsiIsNull(imei.substring(0, 14), msisdn);
        } else if (imeiEmpty && !imsiEmpty && !msisdnEmpty) {
            blackList = blackListRepository.findBlackListByImsiAndMsisdnAndImeiIsNull(imsi, msisdn);
        } else if (imeiEmpty && imsiEmpty && !msisdnEmpty) {
            blackList = blackListRepository.findBlackListByMsisdnAndImsiIsNullAndImeiIsNull(msisdn);
        } else if (imeiEmpty && !imsiEmpty && msisdnEmpty) {
            blackList = blackListRepository.findBlackListByImsiAndImeiIsNullAndMsisdnIsNull(imsi);
        } else if (!imeiEmpty && imsiEmpty && msisdnEmpty) {
            blackList = blackListRepository.findBlackListByImeiAndImsiIsNullAndMsisdnIsNull(imei.substring(0, 14));
        }
        return blackList;

    }

    public ExceptionList getExceptionListEntry(boolean imsiEmpty, boolean msisdnEmpty, boolean imeiEmpty, String imei, String imsi, String msisdn) {

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
        return exceptionList;
    }

    public BlockedTacList getBlockedTacEntry(boolean tacEmpty, String tac) {
        BlockedTacList blockedTacList = null;
        if (!tacEmpty) {
            blockedTacList = blockedTacListRepository.findBlockedTacListByTac(tac);
        }
        return blockedTacList;
    }


    public void updateSource(String source, BlackList blackList, String repo) {
        logger.info("Updated {} with source {} for imei {}", repo, source, blackList.getImei());

        String imsi = validation.isEmptyAndNull(blackList.getImsi()) ? null : blackList.getImsi();
        String msisdn = validation.isEmptyAndNull(blackList.getMsisdn()) ? null : blackList.getMsisdn();
        String imei = validation.isEmptyAndNull(blackList.getImei()) ? null : blackList.getImei();
        logger.info("The corresponding row in the black_list  will be updated based on the imsi {} , msisdn {} and imei {}", imsi, msisdn, imei);

        int rowAffected = 0;
        try {
            switch (repo) {
                case "BLACK_LIST" -> rowAffected = blackListRepository.updateSource(source, imei, imsi, msisdn);
            }
            if (rowAffected > 0) {
                logger.info("updated source value for {}", repo);
            }
        } catch (Exception e) {
            logger.error("Exception occured during update the entry for imei {} with message {}", imei, e.getCause());
        }
    }
}
