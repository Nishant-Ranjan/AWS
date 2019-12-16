package com.amazonaws.lambda.s3;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

public class LambdaFunctionHandler implements RequestHandler<S3Event, String> {

    private AmazonS3 s3 = AmazonS3ClientBuilder.standard().build();

    public LambdaFunctionHandler() {}

    // Test purpose only.
    LambdaFunctionHandler(AmazonS3 s3) {
        this.s3 = s3;
    }

    @Override
    public String handleRequest(S3Event event, Context context) {
        context.getLogger().log("Received event: " + event);
        try {
        	context.getLogger().log(event.toJson());
            String bucketName = event.getRecords().get(0).getS3().getBucket().getName();
            String key = event.getRecords().get(0).getS3().getObject().getKey();
            String folderName = key.split("\\.")[1] + "/";
            context.getLogger().log("Key: "+ key);
            s3.copyObject(bucketName, key, "s3contentstorage-infrastuctureplatform-nishant", folderName+key);
            context.getLogger().log("File copied successfully.");
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    

}