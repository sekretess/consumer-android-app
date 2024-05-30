package com.sekretess.dto;

public class BusinessDto {
    private String businessName;

    public BusinessDto(String businessName) {
        this.businessName = businessName;
    }

    public BusinessDto() {
    }


    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }
}
