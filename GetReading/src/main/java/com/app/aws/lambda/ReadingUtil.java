package com.app.aws.lambda;

import com.app.aws.lambda.dto.DbSavedSourceDto;
import com.app.aws.lambda.dto.ParsedRequestDto;
import com.app.aws.lambda.dto.ReadingDto;
import com.app.aws.lambda.dto.RequestContextDto;
import com.app.aws.lambda.dto.SourceRequestDto;
import com.app.aws.lambda.dto.SqsReadingEventDto;
import lombok.experimental.UtilityClass;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@UtilityClass
public class ReadingUtil {

    String HOST = "host";
    String USER_AGENT = "user-agent";

    SourceRequestDto createSourceRequestDto(Map<Object, Object> input) {
        var sourceRequestDto = new SourceRequestDto();
        Optional.ofNullable(getValue(input, "content-length")).ifPresent(o ->
                sourceRequestDto.setContentLength(Integer.parseInt(o)));
        Optional.ofNullable(getValue(input, "content-type")).ifPresent(sourceRequestDto::setContentType);
        Optional.ofNullable(getValue(input, HOST)).ifPresent(sourceRequestDto::setHost);
        Optional.ofNullable(getValue(input, USER_AGENT)).ifPresent(sourceRequestDto::setUserAgent);
        Optional.ofNullable(getValue(input, "httpMethod")).ifPresent(sourceRequestDto::setMethod);
        if (sourceRequestDto.getMethod() == null) {
            Optional.ofNullable(getValue(input, "method")).ifPresent(sourceRequestDto::setMethod);
        }
        Optional.ofNullable(getValue(input, "path")).ifPresent(sourceRequestDto::setPath);
        Optional.ofNullable(getValue(input, "protocol")).ifPresent(sourceRequestDto::setProtocol);
        Optional.ofNullable(getValue(input, "body")).ifPresent(sourceRequestDto::setBody);
        return sourceRequestDto;
    }

    RequestContextDto createRequestContextDto(Map<Object, Object> input) {
        var requestContextDto = new RequestContextDto();
        Optional.ofNullable(getValue(input, HOST)).ifPresent(requestContextDto::setHost);
        Optional.ofNullable(getValue(input, USER_AGENT)).ifPresent(requestContextDto::setUserAgent);
        requestContextDto.setReceivedDateTime(OffsetDateTime.now());
        return requestContextDto;
    }

    ParsedRequestDto createParsedRequestDto(RequestContextDto requestContextDto, ReadingDto readingDto) {
        var parsedRequestDto = new ParsedRequestDto();
        parsedRequestDto.setRequestContext(requestContextDto);
        parsedRequestDto.setReading(readingDto);
        return parsedRequestDto;
    }

    DbSavedSourceDto createDbSavedSourceDto(SourceRequestDto sourceRequestDto,
                                            ParsedRequestDto parsedRequestDto, ReadingDto readingDto,
                                            String protocolCode) {
        var dbSavedSourceDto = new DbSavedSourceDto();
        dbSavedSourceDto.setSourceRequest(sourceRequestDto);
        dbSavedSourceDto.setParsedRequest(parsedRequestDto);
        dbSavedSourceDto.setDeviceId(readingDto.getDeviceId());
        dbSavedSourceDto.setDeviceType(readingDto.getDeviceType());
        dbSavedSourceDto.setDataType(readingDto.getDataType());
        dbSavedSourceDto.setProtocolCode(protocolCode);
        return dbSavedSourceDto;
    }

    SqsReadingEventDto createSqsReadingEventDto(SourceRequestDto sourceRequestDto,
                                                ParsedRequestDto parsedRequest) {
        var sqsReadingEventDto = new SqsReadingEventDto();
        sqsReadingEventDto.setSourceRequest(sourceRequestDto);
        sqsReadingEventDto.setParsedRequest(parsedRequest);
        return sqsReadingEventDto;
    }

    String getValue(Map<Object, Object> input, String key) {
        String value = null;
        if (input.containsKey(key)) {
            return String.valueOf(input.get(key));
        }
        for (Map.Entry<Object, Object> entry : input.entrySet()) {
            if (key.equalsIgnoreCase(String.valueOf(entry.getKey()))) {
                if (entry.getValue() instanceof List) {
                    List<?> objects = (List<?>) entry.getValue();
                    if (!objects.isEmpty()) {
                        value = String.valueOf(objects.get(0));
                    }
                } else {
                    value = String.valueOf(entry.getValue());
                }
            } else if (entry.getValue() instanceof Map) {
                value = getValue((Map<Object, Object>) entry.getValue(), key);
            }
            if (value != null) {
                break;
            }
        }
        return value;
    }

}
