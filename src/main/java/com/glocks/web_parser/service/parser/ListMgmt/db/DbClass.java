package com.glocks.web_parser.service.parser.ListMgmt.db;

import com.glocks.web_parser.model.app.BlackList;
import com.glocks.web_parser.model.app.BlockedTacList;
import com.glocks.web_parser.model.app.ExceptionList;
import com.glocks.web_parser.model.app.GreyList;
import com.glocks.web_parser.repository.app.BlackListRepository;
import com.glocks.web_parser.repository.app.BlockedTacListRepository;
import com.glocks.web_parser.repository.app.ExceptionListRepository;
import com.glocks.web_parser.repository.app.GreyListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DbClass {

    @Autowired
    BlackListRepository blackListRepository;

    @Autowired
    ExceptionListRepository exceptionListRepository;

    @Autowired
    BlockedTacListRepository blockedTacListRepository;

    @Autowired
    GreyListRepository greyListRepository;

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
    public ExceptionList getExceptionListEntry(boolean imsiEmpty, boolean msisdnEmpty,boolean imeiEmpty, String imei,
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
    public BlockedTacList  getBlockedTacEntry(boolean tacEmpty, String tac) {
        BlockedTacList blockedTacList = null;
        if (!tacEmpty) {
            blockedTacList = blockedTacListRepository.findBlockedTacListByTac(tac);
        }
        return blockedTacList;
    }
}
