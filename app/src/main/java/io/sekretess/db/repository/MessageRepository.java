package io.sekretess.db.repository;

import java.util.List;

import io.sekretess.db.SekretessDatabase;
import io.sekretess.db.dao.MessageDao;
import io.sekretess.db.model.MessageEntity;
import io.sekretess.dependency.SekretessDependencyProvider;

public class MessageRepository {
    private final MessageDao messageDao;
    private final String TAG = MessageRepository.class.getName();


    public MessageRepository() {
        SekretessDatabase db = SekretessDatabase.getInstance(SekretessDependencyProvider.applicationContext());
        this.messageDao = db.messageStoreDao();
    }

    public void storeDecryptedMessage(String sender, String message, String username) {
        messageDao.insert(new MessageEntity(username, sender, message, System.currentTimeMillis()));
    }

    public List<String> getTopSenders() {
        return messageDao.getTopSenders();
    }

    public List<MessageEntity> getMessages(String username, String sender){
        return messageDao.getMessages(username, sender);
    }

    public List<MessageEntity> getMessages(String username){
        return  messageDao.getMessages(username);
    }

    public void deleteMessage(Long messageId) {
        messageDao.deleteMessage(messageId);
    }
}
