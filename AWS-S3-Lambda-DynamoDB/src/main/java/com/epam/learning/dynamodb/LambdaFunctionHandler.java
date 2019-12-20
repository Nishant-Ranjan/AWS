package com.epam.learning.dynamodb;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;

public class LambdaFunctionHandler implements RequestHandler<S3Event, String> {

	public LambdaFunctionHandler() {}

	@Override
	public String handleRequest(S3Event event, Context context) {
		AmazonDynamoDB dynamoDb = AmazonDynamoDBClientBuilder.standard()
				.withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
				.withRegion("ap-south-1").build();
		context.getLogger().log("Received event: " + event);
		String bucket = event.getRecords().get(0).getS3().getBucket().getName();
		String key = event.getRecords().get(0).getS3().getObject().getKey();
		try {
			CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(bucket+"_auditDetails")
					.withKeySchema(new KeySchemaElement().withAttributeName("file-name").withKeyType(KeyType.HASH))
					.withAttributeDefinitions(new AttributeDefinition().withAttributeName("file-name").withAttributeType(ScalarAttributeType.S))
					.withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(1L));
			TableUtils.createTableIfNotExists(dynamoDb, createTableRequest);

			SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
			item.put("file-name", new AttributeValue(key));
			item.put("bucket", new AttributeValue(bucket));
			item.put("Create DateTime", new AttributeValue(formatter.format(new Date(System.currentTimeMillis()))));

			PutItemRequest itemRequest = new PutItemRequest().withTableName(bucket+"_auditDetails").withItem(item);
			dynamoDb.putItem(itemRequest);

		} catch (Exception e) {
			e.printStackTrace();
			context.getLogger().log(String.format(
					"Error getting object %s from bucket %s. Make sure they exist and"
							+ " your bucket is in the same region as this function.", key, bucket));
			throw e;
		}
		return null;
	}
}