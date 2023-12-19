package com.zeliafinance.identitymanagement.documents.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DocumentService {

    @Autowired
    private AmazonS3 amazonS3Client;

    @Value("${aws.s3Bucket}")
    private String bucketName;

    @Value("${aws.baseUrl}")
    private String baseUrl;

    public ByteArrayOutputStream downloadFile(String keyName){
        log.info("Key: {}", keyName);
        try {
            S3Object s3Object = amazonS3Client.getObject(new GetObjectRequest(bucketName, keyName));
            InputStream inputStream = s3Object.getObjectContent();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            int length;
            byte[] buffer = new byte[4096];
            while ((length = inputStream.read(buffer, 0, buffer.length)) != -1){
                outputStream.write(buffer, 0, length);
            }
            return outputStream;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> listFiles(){
        log.info("Bucket Name: {}", bucketName);
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName);
        List<String> keys = new ArrayList<>();
        ObjectListing objects = amazonS3Client.listObjects(listObjectsRequest);
        while (true){
            List<S3ObjectSummary> objectSummaries = objects.getObjectSummaries();
            if (objectSummaries.isEmpty()){
                break;
            }
            for (S3ObjectSummary item : objectSummaries){
                if (!item.getKey().endsWith("/")){
                    keys.add(item.getKey());
                }
            }
            objects = amazonS3Client.listNextBatchOfObjects(objects);

        }
        return keys;
    }
}
