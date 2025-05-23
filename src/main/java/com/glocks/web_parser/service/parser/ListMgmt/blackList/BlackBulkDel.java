package com.glocks.web_parser.service.parser.ListMgmt.blackList;

import com.glocks.web_parser.alert.AlertService;
import com.glocks.web_parser.config.AppConfig;
import com.glocks.web_parser.config.DbConfigService;
import com.glocks.web_parser.constants.FileType;
import com.glocks.web_parser.constants.ListType;
import com.glocks.web_parser.dto.FileDto;
import com.glocks.web_parser.dto.ListMgmtDto;
import com.glocks.web_parser.model.app.ListDataMgmt;
import com.glocks.web_parser.model.app.WebActionDb;
import com.glocks.web_parser.repository.app.WebActionDbRepository;
import com.glocks.web_parser.repository.app.SysParamRepository;
import com.glocks.web_parser.service.fileCopy.ListFileManagementService;
import com.glocks.web_parser.service.fileOperations.FileOperations;
import com.glocks.web_parser.service.operatorSeries.OperatorSeriesService;
import com.glocks.web_parser.service.parser.ListMgmt.CommonFunctions;
import com.glocks.web_parser.service.parser.ListMgmt.utils.BlackListUtils;
import com.glocks.web_parser.validator.Validation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;


@Service
public class BlackBulkDel implements IRequestTypeAction {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    WebActionDbRepository webActionDbRepository;
    @Autowired
    Validation validation;

    @Autowired
    AppConfig appConfig;

    @Autowired
    OperatorSeriesService operatorSeriesService;
    @Autowired
    FileOperations fileOperations;
    @Autowired
    SysParamRepository sysParamRepository;
    @Autowired
    ListFileManagementService listFileManagementService;
    @Autowired
    CommonFunctions commonFunctions;
    @Autowired
    DbConfigService dbConfigService;
    @Autowired
    AlertService alertService;
    @Autowired
    BlackListUtils blackListUtils;

    @Override
    public  void executeInitProcess(WebActionDb webActionDb, ListDataMgmt listDataMgmt) {
        logger.info("listDataMgmt request {}", listDataMgmt);

        webActionDbRepository.updateWebActionStatus(2, webActionDb.getId());
        executeValidateProcess(webActionDb, listDataMgmt);

    }
    public void executeValidateProcess(WebActionDb webActionDb, ListDataMgmt listDataMgmt) {

        try {

            String currentFileName = listDataMgmt.getFileName();
            String transactionId = listDataMgmt.getTransactionId();
            String filePath = appConfig.getListMgmtFilePath() + "/" + transactionId + "/" + currentFileName;
            FileDto currFile = new FileDto(currentFileName, appConfig.getListMgmtFilePath() + "/" + listDataMgmt.getTransactionId());

            logger.info("File path is {}", filePath);
            if(!fileOperations.checkFileExists(filePath)) {
                logger.error("File does not exists {}", filePath);
                alertService.raiseAnAlert(transactionId,"alert5701", "List Mgmt Black List", currentFileName + " with transaction id " + transactionId, 0);
//                commonFunctions.updateFailStatus(webActionDb, listDataMgmt);
                return ;
            }
            if(currFile.getTotalRecords() > Integer.parseInt(sysParamRepository.getValueFromTag("LIST_MGMT_FILE_COUNT"))) {
                commonFunctions.updateFailStatus(webActionDb, listDataMgmt,
                        currFile.getTotalRecords(), currFile.getSuccessRecords(), currFile.getFailedRecords());
                return ;
            }
            if(!fileValidation(filePath)) {
                commonFunctions.updateFailStatus(webActionDb, listDataMgmt,
                        currFile.getTotalRecords(), currFile.getSuccessRecords(), currFile.getFailedRecords());
                return ;
            }
            logger.info("File is ok will process it now");
            webActionDbRepository.updateWebActionStatus(3, webActionDb.getId());
            executeProcess(webActionDb, listDataMgmt);
        } catch (Exception ex) {
            logger.error(ex.getMessage());

        }

    }

    public void executeProcess(WebActionDb webActionDb, ListDataMgmt listDataMgmt) {
        int successCount = 0, failedCount = 0, validationFailedCount=0;
        String currentFileName = listDataMgmt.getFileName();
        String filePath = appConfig.getListMgmtFilePath() + "/" + listDataMgmt.getTransactionId() + "/" + currentFileName;
        FileDto currFile = new FileDto(currentFileName, appConfig.getListMgmtFilePath() + "/" + listDataMgmt.getTransactionId());
        String imsiPrefixValue = sysParamRepository.getValueFromTag("imsiPrefix");
        String msisdnPrefixValue = sysParamRepository.getValueFromTag("msisdnPrefix");
        try {
            operatorSeriesService.fillOperatorSeriesHash();
            File outFile = new File(appConfig.getListMgmtFilePath() + "/" + listDataMgmt.getTransactionId() + "/" + listDataMgmt.getTransactionId()+ ".csv");
            PrintWriter writer = new PrintWriter(outFile);

            try(BufferedReader reader = new BufferedReader( new FileReader(filePath))) {

                writer.println(reader.readLine()+",Reason"); // print header in file
                String record;

                while((record = reader.readLine()) != null) {
                    if(record.isEmpty()) {
                        continue;
                    }
                    ListMgmtDto listMgmtDto = new ListMgmtDto(record.split(appConfig.getListMgmtFileSeparator(), -1));
                    String validateEntry = commonFunctions.validateEntry(listMgmtDto.getImsi(), listMgmtDto.getImei(),
                            listMgmtDto.getMsisdn(), msisdnPrefixValue.split(",", -1),
                            imsiPrefixValue.split(",", -1));
                    if(validateEntry.equalsIgnoreCase("")) {
                        logger.info("The entry is valid, it will be processed");
                    }
                    else {
                        logger.info("The entry failed the validation, with reason {}", validateEntry);
                        writer.println((listMgmtDto.getMsisdn() == null ? "" : listMgmtDto.getMsisdn())+","+(listMgmtDto.getImsi() == null ? "" : listMgmtDto.getImsi())+","+(listMgmtDto.getImei() == null ? "":listMgmtDto.getImei())+","+dbConfigService.getValue(validateEntry));
                        failedCount++;
                        continue;
                    }
                    boolean status = blackListUtils.processBlackSingleDelEntry(listDataMgmt, listMgmtDto, 0, writer);
                    if(status) successCount++;
                    else failedCount++;
                }
                writer.close();
                listFileManagementService.saveListManagementEntity(listDataMgmt.getTransactionId(), ListType.OTHERS, FileType.BULK,
                        appConfig.getListMgmtFilePath() + "/" + listDataMgmt.getTransactionId() + "/",
                        listDataMgmt.getTransactionId() + ".csv", currFile.getTotalRecords());
                currFile.setSuccessRecords(successCount);
                currFile.setFailedRecords(failedCount);
            } catch (Exception ex) {
                logger.error("Error while processing the file {}, with error {}", filePath, ex.getMessage());
                commonFunctions.updateFailStatus(webActionDb, listDataMgmt,
                        currFile.getTotalRecords(), currFile.getSuccessRecords(), currFile.getFailedRecords());
            }
        } catch (Exception ex) {
            logger.error("Error while processing with error {}", ex.getMessage());
        }
        logger.info("File summary is {}", currFile);
        commonFunctions.updateSuccessStatus(webActionDb, listDataMgmt,
                currFile.getTotalRecords(), currFile.getSuccessRecords(), currFile.getFailedRecords());

    }






    boolean fileValidation(String fileName) {
        File file = new File(fileName);
        try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String headers = reader.readLine();
            String[] header = headers.split(appConfig.getListMgmtFileSeparator(), -1);
            if(header.length != 3) {
                return false;
            }
            ListMgmtDto listMgmtDto = new ListMgmtDto(header);
            if (listMgmtDto.getMsisdn().trim().equalsIgnoreCase("msisdn") &&
                    listMgmtDto.getImsi().trim().equalsIgnoreCase("imsi") &&
                    listMgmtDto.getImei().trim().equalsIgnoreCase("imei")
            ) {
                reader.close();
                return true;
            }
            reader.close();
            logger.error("The header of the file is not correct");
            return false;
        } catch (Exception ex) {
            logger.error("Exception while reading the file {} {}", fileName, ex.getMessage());
            return false;
        }
    }
}
