package io.sekretess.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.sekretess.enums.ItemType;

public class BusinessDto {
    @JsonProperty("displayName")
    private String displayName;

    @JsonProperty("name")
    private String name;

    @JsonProperty("email")
    private String email;
    @JsonProperty("icon")
    private String icon;
    private boolean subscribed;
    private ItemType itemType;

    public BusinessDto(String displayName, String name, String email, String icon, boolean subscribed) {
        this.name = name;
        this.email = email;
        this.displayName = displayName;
        this.icon = icon;
        this.subscribed = subscribed;
    }

    public BusinessDto() {
    }


    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
