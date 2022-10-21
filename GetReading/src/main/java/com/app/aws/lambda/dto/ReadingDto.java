package com.app.aws.lambda.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReadingDto {

    @JsonProperty("GatewayType")
    private String gatewayType;

    @JsonProperty("GatewayID")
    private String gatewayId;

    @JsonProperty("GatewayVer")
    private String gatewayVer;

    @JsonProperty("DeviceType")
    private String deviceType;

    @JsonProperty("DeviceID")
    private String deviceId;

    @JsonProperty("DeviceVer")
    private String deviceVer;

    @JsonProperty("ExtensionID")
    private String extensionId;

    @JsonProperty("Year")
    private String year;

    @JsonProperty("Month")
    private String month;

    @JsonProperty("Day")
    private String day;

    @JsonProperty("Hour")
    private String hour;

    @JsonProperty("Minute")
    private String minute;

    @JsonProperty("Second")
    private String second;

    @JsonProperty("DataType")
    private Integer dataType;

    @JsonProperty("Value1")
    private String value1;

    @JsonProperty("Value2")
    private String value2;

    @JsonProperty("Value3")
    private String value3;

    @JsonProperty("Value4")
    private String value4;

    @JsonProperty("Value5")
    private String value5;

    @JsonProperty("Value6")
    private String value6;

    @JsonProperty("Value7")
    private String value7;

    @JsonProperty("IMEI")
    private String imei;

    @JsonProperty("SIMID")
    private String simid;

    @JsonProperty("Slot")
    private String slot;

    @JsonProperty("GroupID")
    private String groupId;

    @JsonProperty("PatientID")
    private String patientId;

    @JsonProperty("OperatorID")
    private String operatorId;

    @JsonProperty("Note")
    private String note;

    @JsonProperty("Height")
    private String height;

    @JsonProperty("Weight")
    private String weight;

    @JsonProperty("Gender")
    private String gender;

    @JsonProperty("Age")
    private String age;

}
