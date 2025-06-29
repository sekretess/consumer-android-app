package io.sekretess.dto;

public class MessageDto {
    private String text;
    private String sender;
    private String consumerExchange;
    private String businessExchange;
    private String type;
    public String getBusinessExchange() {
        return businessExchange;
    }

    public void setBusinessExchange(String businessExchange) {
        this.businessExchange = businessExchange;
    }



    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getConsumerExchange() {
        return consumerExchange;
    }

    public void setConsumerExchange(String consumerExchange) {
        this.consumerExchange = consumerExchange;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
