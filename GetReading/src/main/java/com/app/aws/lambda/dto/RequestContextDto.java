package com.app.aws.lambda.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class RequestContextDto {

    private String host;

    private String userAgent;

    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    private OffsetDateTime receivedDateTime;

}
