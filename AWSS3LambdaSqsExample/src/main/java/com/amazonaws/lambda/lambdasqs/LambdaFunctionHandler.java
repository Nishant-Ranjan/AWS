package com.amazonaws.lambda.lambdasqs;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LambdaFunctionHandler implements RequestHandler<SQSEvent, String> {

	private AmazonS3 s3 = AmazonS3ClientBuilder.standard().build();
	public LambdaFunctionHandler() {}
	LambdaFunctionHandler(AmazonS3 s3) {
		this.s3 = s3;
	}
	@Override
	public String handleRequest(SQSEvent event, Context context) {
		context.getLogger().log("Total Message in Queue: " + event.getRecords().size());

		for(SQSMessage message: event.getRecords()) {
			String body = message.getBody();
			ObjectMapper mapper = new ObjectMapper();
			try {
				JsonNode rootNode = mapper.readTree(body).path("Records").path(0);
				JsonNode s3Node = rootNode.path("s3");
				String bucketName = s3Node.path("bucket").path("name").textValue();
				String key = s3Node.path("object").path("key").textValue();
				context.getLogger().log("Bucket: "+ bucketName +"; Key:"+key);
				String folderName = key.split("\\.")[1] + "/";
				s3.copyObject(bucketName, key, "s3contentstorage-infrastuctureplatform-nishant", folderName+key);
				context.getLogger().log("File copied successfully.");

			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}

		}
		return null;
	}

}
