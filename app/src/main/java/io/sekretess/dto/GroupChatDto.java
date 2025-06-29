package io.sekretess.dto;

public class GroupChatDto {
    private String sender;
    private String distributionKey;

    public GroupChatDto(String sender, String distributionKey) {
        this.sender = sender;
        this.distributionKey = distributionKey;
    }

    public String getDistributionKey() {
        return distributionKey;
    }

    public String getSender() {
        return sender;
    }
}
