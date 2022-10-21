package com.app.aws.lambda;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaRuntime;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.app.aws.lambda.ReadingAwsLambda;
import com.app.aws.lambda.ReadingService;
import com.app.aws.lambda.dto.DbSavedSourceDto;
import com.app.aws.lambda.response.ResponseEvent;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class ReadingAwsLambdaTest {

    private final ReadingAwsLambda readingAwsLambda = new ReadingAwsLambda();

    @Mock
    private Context mockContext;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockContext.getLogger()).thenReturn(LambdaRuntime.getLogger());
    }

    @Test
    void testHandleRequestSuccess() throws Exception {
        try (MockedConstruction<ReadingService> serviceMocked = mockConstruction(ReadingService.class)) {

            ReadingService readingService = new ReadingService(Regions.DEFAULT_REGION,
                    "readingTable", "readingUrl");
            when(readingService.getData(any(UUID.class))).thenReturn(spy(Item.class), null);
            when(readingService.persistData(any(DbSavedSourceDto.class))).thenReturn(null);
            doNothing().when(readingService).sendMessage(anyString());

            ResponseEvent<Boolean> result = readingAwsLambda.handleRequest(getInput(), mockContext);

            assertEquals(HttpStatus.SC_OK, result.getStatusCode().intValue());
            assertNotNull(result.getBody());
            assertTrue(result.getBody());
        }
    }

    @Test
    void testHandleRequestBadRequest() throws Exception {
        Map<Object, Object> input = getInput();
        input.put("body", "");

        ResponseEvent<Boolean> result = readingAwsLambda.handleRequest(input, mockContext);

        assertEquals(HttpStatus.SC_BAD_REQUEST, result.getStatusCode().intValue());
        assertNotNull(result.getBody());
        assertFalse(result.getBody());
    }

    @Test
    void testHandleRequestException() throws Exception {
        AmazonDynamoDBClientBuilder mockDynamoDBClientBuilder = mock(AmazonDynamoDBClientBuilder.class);
        try (MockedStatic<AmazonDynamoDBClientBuilder> dynamoDBMocked = mockStatic(AmazonDynamoDBClientBuilder.class)) {

            dynamoDBMocked.when(AmazonDynamoDBClientBuilder::standard).thenReturn(mockDynamoDBClientBuilder);
            when(mockDynamoDBClientBuilder.withRegion(any(Regions.class))).thenReturn(mockDynamoDBClientBuilder);
            when(mockDynamoDBClientBuilder.build()).thenAnswer(invocation -> {
                throw new Exception();
            });

            ResponseEvent<Boolean> result = readingAwsLambda.handleRequest(getInput(), mockContext);

            assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, result.getStatusCode().intValue());
            assertNotNull(result.getBody());
            assertFalse(result.getBody());
        }
    }

    private Map<Object, Object> getInput() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream("message.json")) {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(inputStream, Map.class);
        }
    }

}
