package com.app.aws.lambda;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.app.aws.lambda.dto.DbSavedSourceDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;
import java.util.UUID;

public class ReadingService {

    private static final String ID = "Id";

    private final String readingTable;
    private final String queueUrl;
    private final DynamoDB dynamoDB;
    private final AmazonSQS amazonSQS;

    public ReadingService(Regions region, String readingTable, String queueUrl) {
        var amazonDynamoDB = AmazonDynamoDBClientBuilder.standard().withRegion(region).build();
        this.readingTable = readingTable;
        this.dynamoDB = new DynamoDB(amazonDynamoDB);
        this.queueUrl = queueUrl;
        this.amazonSQS = AmazonSQSClientBuilder.standard().withRegion(region).build();
    }

    public PutItemOutcome persistData(DbSavedSourceDto dbSavedSourceDto) throws JsonProcessingException {
        var table = dynamoDB.getTable(readingTable);
        return table.putItem(convertToItem(dbSavedSourceDto));
    }

    public Item getData(UUID id) {
        var table = dynamoDB.getTable(readingTable);
        return table.getItem(ID, id.toString());
    }

    public void sendMessage(String messageBody) {
        SendMessageRequest sendMsgRequest = new SendMessageRequest().withQueueUrl(queueUrl)
                .withMessageBody(messageBody).withDelaySeconds(5);
        amazonSQS.sendMessage(sendMsgRequest);
    }

    private Item convertToItem(DbSavedSourceDto dbSavedSourceDto) throws JsonProcessingException {
        UUID id = generateItemId();
        var item = new Item().withPrimaryKey(ID, String.valueOf(id));
        var parsedRequestDto = dbSavedSourceDto.getParsedRequest();
        dbSavedSourceDto.setId(id);
        parsedRequestDto.setRequestId(id);
        var objectMapper = new ObjectMapper();
        if (dbSavedSourceDto.getSourceRequest() != null) {
            item.withJSON("SourceRequest", objectMapper.writeValueAsString(
                    dbSavedSourceDto.getSourceRequest()));
        }
        if (dbSavedSourceDto.getParsedRequest() != null) {
            item.withJSON("ParsedRequest", objectMapper.writeValueAsString(
                    dbSavedSourceDto.getParsedRequest()));
        }
        Optional.ofNullable(dbSavedSourceDto.getDeviceId())
                .ifPresent(value -> item.withString("DeviceId", value));
        Optional.ofNullable(dbSavedSourceDto.getProtocolCode())
                .ifPresent(value -> item.withString("ProtocolCode", value));
        Optional.ofNullable(dbSavedSourceDto.getDeviceType())
                .ifPresent(value -> item.withString("DeviceType", value));
        Optional.ofNullable(dbSavedSourceDto.getDataType())
                .ifPresent(value -> item.withInt("DataType", value));
        return item;
    }

    private UUID generateItemId() {
        var id = UUID.randomUUID();
        while (getData(id) != null) {
            id = UUID.randomUUID();
        }
        return id;
    }

}
