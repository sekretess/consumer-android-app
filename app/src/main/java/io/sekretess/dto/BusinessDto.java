package io.sekretess.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.sekretess.enums.ItemType;

public class BusinessDto {
    @JsonProperty("business_name")
    private String businessName;
    @JsonProperty("icon")
    private String icon;
    private boolean subscribed;
    private ItemType itemType;

    public BusinessDto(String businessName, String icon, boolean subscribed) {
        this.businessName = businessName;
        this.icon = icon;
        this.subscribed = subscribed;
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

    public boolean isSubscribed() {
        return subscribed;
    }

    public void setSubscribed(boolean subscribed) {
        this.subscribed = subscribed;
    }

    public void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }

    public ItemType getItemType() {
        return itemType;
    }
}
