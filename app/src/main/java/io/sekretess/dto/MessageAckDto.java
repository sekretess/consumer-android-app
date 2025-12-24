package io.sekretess.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MessageAckDto {

    private String messageId;
    private int resultCode;

    public MessageAckDto(String messageId) {
        this.messageId = messageId;
    }

    public MessageAckDto() {

    }

    public MessageAckDto(String messageId, int resultCode) {
        this.messageId = messageId;
        this.resultCode = resultCode;
    }
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public String jsonString() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "";
        }
    }
}
