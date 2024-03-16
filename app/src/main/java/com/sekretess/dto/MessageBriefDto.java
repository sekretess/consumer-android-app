package com.sekretess.dto;

public class MessageCountDto {
    private String sender;
    private int count;

    public MessageCountDto() {
    }

    public MessageCountDto(String sender, int count) {
        this.sender = sender;
        this.count = count;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
