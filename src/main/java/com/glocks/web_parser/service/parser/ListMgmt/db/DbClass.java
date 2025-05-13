package com.glocks.web_parser.service.parser.ListMgmt.db;

import com.glocks.web_parser.model.app.BlackList;
import com.glocks.web_parser.model.app.BlockedTacList;
import com.glocks.web_parser.model.app.ExceptionList;
import com.glocks.web_parser.model.app.GreyList;
import com.glocks.web_parser.repository.app.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

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


    public GreyList getGreyListEntry(boolean imsiEmpty, boolean msisdnEmpty, boolean imeiEmpty, String imei,
                                     String imsi, String msisdn) {
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

    public BlackList getBlackListEntry(boolean imsiEmpty, boolean msisdnEmpty, boolean imeiEmpty, String imei,
                                       String imsi, String msisdn) {
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
        return blackList;

    }

    public ExceptionList getExceptionListEntry(boolean imsiEmpty, boolean msisdnEmpty, boolean imeiEmpty, String imei,
                                               String imsi, String msisdn) {

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

    public String remove(String source) {
        if (Objects.nonNull(source)) {
            return Arrays.stream(source.split(",")).filter(element -> !element.equals("CEIRAdmin")).collect(Collectors.joining(","));
        } else {
            logger.info("No source value {} found", source);
        }
        return null;
    }

    public void updateSource(String source, String imei, String repo) {
        logger.info("Updated {} with source {} for imei {}", repo, source, imei);
        int rowAffected = 0;
        switch (repo) {
            case "BLACK_LIST" -> rowAffected = blackListRepository.updateSource(source, imei);
            case "BLACK_LIST_HIS" -> rowAffected = blackListHisRepository.updateSource(source, imei);
            case "EXCEPTION_LIST" -> rowAffected = exceptionListRepository.updateSource(source, imei);
            case "EXCEPTION_LIST_HIS" -> rowAffected = exceptionListHisRepository.updateSource(source, imei);
        }
        if (rowAffected == 1) {
            logger.info("updated source value for {}", repo);
        } else {
            logger.info("Failed to update source value for {}", repo);
        }
    }
}
