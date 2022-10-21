package com.app.aws.lambda.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SourceRequestDto {

    private Integer contentLength;

    private String contentType;

    private String host;

    private String userAgent;

    private String method;

    private String path;

    private String protocol;

    private String body;

}
