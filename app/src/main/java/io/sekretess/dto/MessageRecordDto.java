package io.sekretess.dto;

public class MessageRecordDto {
    private String sender;
    private String message;
    private long messageDate;

    public MessageRecordDto() {
    }

    public MessageRecordDto(String sender, String message, long messageDate) {
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

    public long getMessageDate() {
        return messageDate;
    }

    public void setMessageDate(long messageDate) {
        this.messageDate = messageDate;
    }
}
