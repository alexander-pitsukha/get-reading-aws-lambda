package com.app.aws.lambda.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ParsedRequestDto {

    private UUID requestId;

    private RequestContextDto requestContext;

    private ReadingDto reading;

}
