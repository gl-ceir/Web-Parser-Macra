package com.glocks.web_parser.service.parser.ListMgmt;


import com.glocks.web_parser.constants.CategoryType;
import com.glocks.web_parser.constants.ConfigFlag;

import com.glocks.web_parser.model.app.WebActionDb;
import com.glocks.web_parser.model.app.ListDataMgmt;
import com.glocks.web_parser.repository.app.SysParamRepository;
import com.glocks.web_parser.repository.app.WebActionDbRepository;
import com.glocks.web_parser.repository.app.ListDataMgmtRepository;
import com.glocks.web_parser.service.operatorSeries.OperatorSeriesService;
import com.glocks.web_parser.validator.Validation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

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
    OperatorSeriesService operatorSeriesService;

    @Autowired
    SysParamRepository sysParamRepository;

    public void updateFailStatus(WebActionDb webActionDb, ListDataMgmt listDataMgmt) {
        webActionDbRepository.updateWebActionStatus(5, webActionDb.getId());
        listDataMgmtRepository.updateListDataMgmtStatus("FAIL", LocalDateTime.now(), listDataMgmt.getId());
//        alertService.raiseAnAlert(alertId, type, fileName, 0);
    }

    public void updateFailStatus(WebActionDb webActionDb, ListDataMgmt listDataMgmt, long totalCount,
                                 long successCount, long failureCount) {
        webActionDbRepository.updateWebActionStatus(5, webActionDb.getId());
        listDataMgmtRepository.updateListDataMgmtStatus("FAIL", LocalDateTime.now(), listDataMgmt.getId(),
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

        if (imsiEmpty && msisdnEmpty && imeiEmpty) {
            return ConfigFlag.msgForNullIMEIIMSIMSISDNInList.name();
        }
        if (!imeiEmpty) {
            if (validation.isLengthLess(imei, 14) || validation.isLengthMore(imei, 16)) {
                return ConfigFlag.msgForLengthValidationIMEIInList.name();
            }
            if (!validation.isNumeric(imei)) {
                return ConfigFlag.msgForNonNumericIMEIInList.name();
            }
        }
        if (!imsiEmpty) {
            for (int i = 0; i < imsiPrefix.length; i++) {

                if (!imsi.startsWith(imsiPrefix[i])) {
                    return ConfigFlag.msgForPrefixIMSIInList.name();
                }
//                if(imsi.isBlank() || !imsi.matches("\\d+")) {
//                    flagImsiNull = true;
//                }
            }
//            if(!validation.isPrefix(imsi, "456")) {
//                return "IMSI does not starts with prefix 456";
//            }
            if (!validation.isNumeric(imsi)) {
                return ConfigFlag.msgForNonNumericIMSIInList.name();
            }
        }
        if (!msisdnEmpty) {
            for (int i = 0; i < msisdnPrefix.length; i++) {

                if (!msisdn.startsWith(msisdnPrefix[i])) {
                    return ConfigFlag.msgForPrefixMSISDNInList.name();
                }
//                if(imsi.isBlank() || !imsi.matches("\\d+")) {
//                    flagImsiNull = true;
//                }
            }
//            if(!validation.isPrefix(msisdn, "855")) {
//                return "MSISDN does not starts with prefix 855";
//            }
            if (!validation.isNumeric(msisdn)) {
                return ConfigFlag.msgForNonNumericMSISDNInList.name();
            }
            if (!operatorSeriesService.validLengthMsisdn(msisdn)) {
                return ConfigFlag.msgForLengthValidationMSISDNInList.name();
            }

        }
        return "";
    }

    public String validateEntry(String tac) {
        boolean tacEmpty = validation.isEmptyAndNull(tac);
        if (tacEmpty) {
            return ConfigFlag.msgForNullTACInList.name();
        } else {

            if (!validation.isLengthEqual(tac, 8)) {
                return ConfigFlag.msgForLengthValidationTACInList.name();
            }
            if (!validation.isNumeric(tac)) {
                return ConfigFlag.msgForNonNumericTACInList.name();
            }
        }
        return "";
    }

    public BiFunction<String, String, Long> sourceCount = (source, categoryValue) -> {
        String category = categoryValue.equalsIgnoreCase("Other") ? CategoryType.getCategory("Other") : categoryValue;
        if (Objects.nonNull(source)) {
            String[] split = source.split(",");
            boolean exists = Arrays.asList(split).contains(category);
            logger.info("split {} and isExists {}", Arrays.toString(split), exists);
            if (exists) {
                logger.info("split length {}", split.length);
                return split.length == 1 ? 1L : 2L;
            }
        }
        logger.info("No source value {} matched", source);
        return 0L;
    };

    public BiFunction<String, String, String> isSourceExist = (source, categoryValue) -> {
        String category = categoryValue.equalsIgnoreCase("Other") ? CategoryType.getCategory("Other") : categoryValue;
        if (Objects.nonNull(source)) {
            String[] split = source.split(",");
            boolean exists = Arrays.asList(split).contains(category);

            if (!exists) {
                return source + "," + category;
            }
        }
        logger.info("No source value {} matched", categoryValue);
        return "";
    };

    public BiFunction<String, String, String> removeSource = (source, categoryValue) -> {
        String category = categoryValue.equalsIgnoreCase("Other") ? CategoryType.getCategory("Other") : categoryValue;

        if (Objects.nonNull(source)) {
            String result = Arrays.stream(source.split(","))
                    .map(String::trim)
                    .filter(element -> !element.equals(category))
                    .collect(Collectors.joining(","));

            logger.info("Updated source  {} after removing {}", result, category);
            return result;
        }

        logger.info("Source is null");
        return "";
    };

    public String getValue(String tag) {
        return sysParamRepository.getValueFromTag(tag);
    }
}
