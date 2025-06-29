package com.sekretess.utils;

import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.sekretess.dto.MessageDto;

public class RabbitMqConsumer extends DefaultConsumer {
    /**
     * Constructs a new instance and records its association to the passed-in channel.
     *
     * @param channel the channel to which this consumer is attached
     */
    private final ObjectMapper objectMapper = new ObjectMapper();
    public RabbitMqConsumer(Channel channel) {
        super(channel);
    }


    @Override
    public void handleDelivery(String consumerTag, Envelope envelope,
                               AMQP.BasicProperties properties, byte[] body) {
        try {
            String exchangeName = envelope.getExchange();
            Log.i("SekretessRabbitMqService", "Received payload:" + new String(body));
            MessageDto message = objectMapper.readValue(body, MessageDto.class);
            String encryptedText = message.getText();
            String messageType = message.getType();
            String sender = "";
            switch (messageType.toLowerCase()) {
                case "advert":
                    exchangeName = message.getBusinessExchange();
                    break;
                case "key_dist":
                    exchangeName = message.getConsumerExchange();
                    sender = message.getSender();
                    break;
                case "private":
                    exchangeName = message.getConsumerExchange();
                    break;
            }


            Log.i("SekretessRabbitMqService", "Encoded message received : " + message);
//            broadcastNewMessageReceived(encryptedText, sender, exchangeName, messageType);

        } catch (Exception e) {
//            Log.e(TAG, e.getMessage(), e);
        }
    }
}
