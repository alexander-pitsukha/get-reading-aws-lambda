package com.app.aws.lambda;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.util.StringUtils;
import com.app.aws.lambda.dto.ReadingDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.app.aws.lambda.response.ResponseEvent;
import org.apache.http.HttpStatus;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReadingAwsLambda implements RequestHandler<Map<Object, Object>, ResponseEvent<Boolean>> {

    public ResponseEvent<Boolean> handleRequest(Map<Object, Object> input, Context context) {
        ResponseEvent<Boolean> response = new ResponseEvent<>();
        LambdaLogger logger = context.getLogger();
        logger.log("Input: " + input);

        Optional<Object> objectOptional = Optional.ofNullable(input.get("body"));

        if (objectOptional.isEmpty() || StringUtils.isNullOrEmpty(String.valueOf(objectOptional.get()))) {
            logger.log("Output: The params are empty.");
            return response.withStatusCode(HttpStatus.SC_BAD_REQUEST).withBody(Boolean.FALSE);
        } else {
            try {
                var properties = getProperties();
                var readingService = new ReadingService(Regions.fromName(
                        properties.getProperty(Constants.REGION)),
                        properties.getProperty(Constants.READING_TABLE_NAME),
                        properties.getProperty(Constants.SQS_QUEUE_URL));
                var objectMapper = new ObjectMapper();
                Map<String, String> readingParams = parseBody(String.valueOf(objectOptional.get()));

                var sourceRequestDto = ReadingUtil.createSourceRequestDto(input);
                var requestContextDto = ReadingUtil.createRequestContextDto(input);
                var readingDto = objectMapper.convertValue(readingParams, ReadingDto.class);
                var parsedRequestDto = ReadingUtil.createParsedRequestDto(requestContextDto, readingDto);

                var dbSavedSourceDto = ReadingUtil.createDbSavedSourceDto(sourceRequestDto,
                        parsedRequestDto, readingDto, Constants.PROTOCOL_FORA);
                readingService.persistData(dbSavedSourceDto);
                logger.log("Persist data in DynamoDB: " + objectMapper.writeValueAsString(dbSavedSourceDto));

                var sqsReadingEventDto = ReadingUtil.createSqsReadingEventDto(sourceRequestDto,
                        parsedRequestDto);
                var sqsReadingEventAsJson = objectMapper.writeValueAsString(sqsReadingEventDto);
                readingService.sendMessage(sqsReadingEventAsJson);
                logger.log("Send in SQS: " + sqsReadingEventAsJson);
            } catch (Exception e) {
                logger.log(e.getLocalizedMessage());
                return response.withStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR).withBody(Boolean.FALSE);
            }
            return response.withStatusCode(HttpStatus.SC_OK).withBody(Boolean.TRUE);
        }
    }

    private Properties getProperties() throws IOException {
        var properties = new Properties();
        var classLoader = getClass().getClassLoader();
        try (var inputStream = classLoader.getResourceAsStream("application.properties")) {
            properties.load(inputStream);
        }
        return properties;
    }

    private Map<String, String> parseBody(String params) {
        Map<String, String> parameters = params.lines()
                .map(entry -> entry.split("="))
                .collect(Collectors.toMap(entry -> entry[0], entry -> entry[1]));
        if (parameters.size() <= 1) {
            parameters = Stream.of(params.split("\\\\r\\\\n"))
                    .map(entry -> entry.split("="))
                    .collect(Collectors.toMap(entry -> entry[0], entry -> entry[1]));
        }
        return parameters;
    }

}
