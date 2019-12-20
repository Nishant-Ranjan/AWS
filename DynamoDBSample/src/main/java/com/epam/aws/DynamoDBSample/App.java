package com.epam.aws.DynamoDBSample;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import com.amazonaws.services.dynamodbv2.util.TableUtils.TableNeverTransitionedToStateException;

public class App 
{
	public static void main( String[] args )
	{
		App app = new App();
		AmazonDynamoDB dynamoDB = app.init();

		/* create Table */
		//app.createTable("Employee", dynamoDB);
		
		
		/* Insert Record */
		//app.createEmployee("102", "Nishant123", dynamoDB);
		
		
		/* Read Request */
		//		List<Map<String, AttributeValue>> employees = app.getAllEmployees(dynamoDB);
		//		for(Map<String, AttributeValue> emp: employees) {
		//			System.out.print(emp.get("id")+">>>>");
		//			System.out.print(emp.get("name"));
		//			System.out.println();
		//		}

		/* Get Record By Partition key */

//		Map<String, AttributeValue> emp =  app.getEmployee("101", dynamoDB);
//		System.out.print(emp.get("id")+">>>>");
//		System.out.print(emp.get("name"));
//		System.out.println();
		
		/* delete record */
		app.deleteEmployee("101", dynamoDB);


	}

	public void createTable(String tableName, AmazonDynamoDB dynamoDB) {
		try {
			CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(tableName)
					.withKeySchema(new KeySchemaElement().withAttributeName("id").withKeyType(KeyType.HASH))
					.withAttributeDefinitions(new AttributeDefinition().withAttributeName("id").withAttributeType(ScalarAttributeType.S))
					.withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(1L));
			TableUtils.createTableIfNotExists(dynamoDB, createTableRequest);
			TableUtils.waitUntilActive(dynamoDB, tableName);
			System.out.println("Table Created Successfully.Nmae: "+ tableName);
		} catch (TableNeverTransitionedToStateException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void createEmployee(String employeeId, String employeeName, AmazonDynamoDB dynamoDB) {
		try {
			Map<String, AttributeValue> map= new HashMap<String, AttributeValue>();
			map.put("id", new AttributeValue(employeeId));
			map.put("name", new AttributeValue(employeeName));
			PutItemRequest putItemRequest = new PutItemRequest("Employee", map);
			dynamoDB.putItem(putItemRequest);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	public List<Map<String, AttributeValue>> getAllEmployees(AmazonDynamoDB dynamoDB){
		List<Map<String, AttributeValue>> employees = null;
		try {
			ScanRequest scanRequest = new ScanRequest().withTableName("Employee");
			ScanResult result = dynamoDB.scan(scanRequest);
			employees = result.getItems();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return employees;
	}

	public Map<String, AttributeValue> getEmployee(String employeeId, AmazonDynamoDB dynamoDB){
		Map<String, AttributeValue> employees = null;
		try {
			Map<String, AttributeValue> map= new HashMap<String, AttributeValue>();
			map.put("id", new AttributeValue(employeeId));
			GetItemRequest getItemRequest = new GetItemRequest().withTableName("Employee")
					.withKey(map);
			GetItemResult result = dynamoDB.getItem(getItemRequest);
			employees = result.getItem();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return employees;
	}
	
	public void deleteEmployee(String employeeId, AmazonDynamoDB dynamoDB) {
		try {
			Map<String, AttributeValue> map= new HashMap<String, AttributeValue>();
			map.put("id", new AttributeValue(employeeId));
			DeleteItemRequest deleteRequest = new DeleteItemRequest().withTableName("Employee")
					.withKey(map);
			dynamoDB.deleteItem(deleteRequest);
			System.out.println("Employee deleted successfully.");
		}catch(Exception e) {
			e.printStackTrace();
		}
	}


	public AmazonDynamoDB init() {
		ProfileCredentialsProvider provider = new ProfileCredentialsProvider();
		try {
			provider.getCredentials();
			return AmazonDynamoDBClientBuilder.standard()
					.withCredentials(provider)
					.withRegion(Regions.US_EAST_1)
					.build();

		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
