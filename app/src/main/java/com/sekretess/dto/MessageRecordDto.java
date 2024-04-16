package com.sekretess.dto;

public class MessageRecordDto {
    private String sender;
    private String message;
    private String messageDate;

    public MessageRecordDto() {
    }

    public MessageRecordDto(String sender, String message, String messageDate) {
        this.sender = sender;
        this.message = message;
        this.messageDate = messageDate;
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

    public String getMessageDate() {
        return messageDate;
    }

    public void setMessageDate(String messageDate) {
        this.messageDate = messageDate;
    }
}
