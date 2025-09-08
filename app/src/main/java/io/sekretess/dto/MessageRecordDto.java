package io.sekretess.dto;

import io.sekretess.enums.ItemType;

public class MessageRecordDto {
    private Long messageId;
    private String sender;
    private String message;
    private long messageDate;
    private String dateText;
    private ItemType itemType;

    public MessageRecordDto() {
    }

    public MessageRecordDto(Long messageId, String sender, String message, long messageDate,
                            String dateText, ItemType itemType) {
        this.messageId = messageId;
        this.sender = sender;
        this.message = message;
        this.messageDate = messageDate;
        this.itemType = itemType;
        this.dateText = dateText;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getMessageDate() {
        return messageDate;
    }

    public String getDateText() {
        return dateText;
    }

    public void setMessageDate(long messageDate) {
        this.messageDate = messageDate;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public Long getMessageId() {
        return messageId;
    }
}
