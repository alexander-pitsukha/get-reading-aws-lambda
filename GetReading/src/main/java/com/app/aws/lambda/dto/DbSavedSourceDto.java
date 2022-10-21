package com.app.aws.lambda.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class DbSavedSourceDto {

    private UUID id;

    private SourceRequestDto sourceRequest;

    private ParsedRequestDto parsedRequest;

    private String deviceId;

    private String protocolCode;

    private String deviceType;

    private Integer dataType;

}
