package io.sekretess.service;

import android.util.Log;

import androidx.lifecycle.LiveData;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.sekretess.dto.MessageDto;
import io.sekretess.enums.MessageType;
import io.sekretess.repository.MessageRepository;
import kotlinx.coroutines.flow.StateFlow;

public class SekretessMessageService {
    private final MessageRepository messageRepository;
    private final String TAG = SekretessMessageService.class.getName();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SekretessCryptographicService sekretessCryptographicService;

    public SekretessMessageService(MessageRepository messageRepository, SekretessCryptographicService sekretessCryptographicService) {
        this.messageRepository = messageRepository;
        this.sekretessCryptographicService = sekretessCryptographicService;
    }


    public void handleMessage(String messageText) {
        try {
            String exchangeName = "";
            Log.i(TAG, "Received payload:" + messageText + " ExchangeName :");
            MessageDto message = objectMapper.readValue(messageText, MessageDto.class);
            String encryptedText = message.getText();
            MessageType messageType = MessageType.getInstance(message.getType());
            String sender = "";
            switch (messageType) {
                case ADVERTISEMENT:
                    exchangeName = message.getBusinessExchange();
                    processAdvertisementMessage(encryptedText, exchangeName);
                    break;
                case KEY_DISTRIBUTION:
                case PRIVATE:
                    exchangeName = message.getConsumerExchange();
                    sender = message.getSender();
                    Log.i(TAG, "Private message received. Sender:" + sender + " Exchange:" + exchangeName);
                    processPrivateMessage(encryptedText, sender, messageType);
                    break;
            }
            Log.i(TAG, "Encoded message received : " + message);
        } catch (Throwable e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private void processAdvertisementMessage(String base64Message, String sender) {
        sekretessCryptographicService.decryptGroupChatMessage(sender, base64Message).ifPresent(decryptedMessage -> {
            messageRepository.storeDecryptedMessage(sender, decryptedMessage);
            broadcastNewMessageReceived();
            publishNotification(sender, decryptedMessage);
        });
    }

    private void processPrivateMessage(String base64Message, String sender, MessageType messageType) {
        sekretessCryptographicService.decryptPrivateMessage(sender, base64Message).ifPresent(decryptedMessage -> {
            if (messageType == MessageType.KEY_DISTRIBUTION) {
                sekretessCryptographicService.processKeyDistributionMessage(sender, decryptedMessage);
            } else {
                messageRepository.storeDecryptedMessage(sender, decryptedMessage);
                publishNotification(sender, decryptedMessage);
                broadcastNewMessageReceived();
            }
        });
    }
}
