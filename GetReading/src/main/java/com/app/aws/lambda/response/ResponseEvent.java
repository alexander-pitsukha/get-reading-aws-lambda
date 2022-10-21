package com.app.aws.lambda.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseEvent<T> {

    private Integer statusCode;

    private T body;

    public ResponseEvent<T> withStatusCode(Integer statusCode) {
        setStatusCode(statusCode);
        return this;
    }

    public ResponseEvent<T> withBody(T body) {
        setBody(body);
        return this;
    }

}
