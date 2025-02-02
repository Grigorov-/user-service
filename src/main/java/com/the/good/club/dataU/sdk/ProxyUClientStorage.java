package com.the.good.club.dataU.sdk;

import com.google.protobuf.ByteString;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

public interface ProxyUClientStorage {

    Logger logger = Logger.getLogger(ProxyUClientStorage.class.getName());

    /**
     * Method used to store or update bulk data for a data subject i.e a company which owns user data
     * @param subject public key of the subject that shared the data
     * @param dataUUID uuid of the data field to store
     * @param process uuid of the process; for one DataIdentificationGraphHelper.BULK_PROCESS
     * @param filename name of transferred file
     * @param mime type of data e.g. text/plain; charset=UTF-8
     * @param dataValue value of the data received field
     */
    default void saveOrUpdateBulkUserData(
            ByteString subject, ByteString dataUUID, ByteString process, ByteString filename, String mime,
            ByteString dataValue
    ) {
        String fileName = filename.toStringUtf8();

        File outputFile = new File(fileName);

        try {
            Files.write(outputFile.toPath(), dataValue.toByteArray());
            logger.log(Level.INFO, "Received file {0}!", fileName);
        } catch (IOException e) {
            logger.info("Error writing file!");
            e.printStackTrace();
        }
    }

    /**
     * Method used to store or update individual data for a data subject
     * @param subject public key of the subject that shared the data
     * @param dataUUID uuid of the data field to store
     * @param process uuid of the process; for one of BULK | INDIVIDUAL
     * @param mime type of data e.g. text/plain; charset=UTF-8
     * @param dataValue value of the data received field
     */
    void saveOrUpdateUserData(
        ByteString subject, ByteString dataUUID, ByteString process, String mime, ByteString dataValue
    );


    /**
     * Method used to read data from DB for the INDIVIDUAL flow
     * @param subject public key of the subject that shared the data
     * @param dataUUID uuid of the data field to retrieve
     * @param process uuid of the process; for one of BULK | INDIVIDUAL
     */
    UserData extractUserData(ByteString subject, ByteString dataUUID, ByteString process);


    /**
     * Method used to delete data from DB for the INDIVIDUAL flow
     * @param subject public key of the subject that shared the data
     * @param dataUUID uuid of the data field to delete
     * @param process uuid of the process; for one of BULK | INDIVIDUAL
     */
    void deleteData(ByteString subject, ByteString dataUUID, ByteString process);
}
