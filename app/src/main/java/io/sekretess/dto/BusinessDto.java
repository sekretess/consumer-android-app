package io.sekretess.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BusinessDto {
    @JsonProperty("business_name")
    private String businessName;
    @JsonProperty("icon")
    private String icon;

    public BusinessDto(String businessName, String icon) {
        this.businessName = businessName;
        this.icon = icon;
    }

    public BusinessDto() {
    }


    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
