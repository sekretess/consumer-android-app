package io.sekretess.dto;

public class TrustedSender {
    private String businessName;
    private String icon;
    private Integer resourceId;

    public TrustedSender(String businessName, String icon) {
        this.businessName = businessName;
        this.icon = icon;
    }

    public TrustedSender(String businessName, int resourceId) {
        this.businessName = businessName;
        this.resourceId = resourceId;
    }

    public String getBusinessName() {
        return businessName;
    }

    public String getIcon() {
        return icon;
    }

    public Integer getResourceId() {
        return resourceId;
    }
}
