package com.app.aws.lambda.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SqsReadingEventDto {

    private SourceRequestDto sourceRequest;

    private ParsedRequestDto parsedRequest;

}
