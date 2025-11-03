package io.sekretess;

import android.app.Application;

import io.sekretess.repository.DbHelper;
import io.sekretess.repository.IdentityKeyRepository;
import io.sekretess.repository.KyberPreKeyRepository;
import io.sekretess.repository.MessageRepository;
import io.sekretess.repository.PreKeyRepository;
import io.sekretess.repository.RegistrationRepository;
import io.sekretess.repository.SenderKeyRepository;
import io.sekretess.repository.SessionRepository;
import io.sekretess.service.SekretessCryptographicService;

public class SekretessApplication extends Application {

    private SekretessCryptographicService sekretessCryptographicService;
    private IdentityKeyRepository identityKeyRepository;
    private KyberPreKeyRepository kyberPreKeyRepository;
    private MessageRepository messageRepository;
    private PreKeyRepository preKeyRepository;
    private RegistrationRepository registrationRepository;
    private SenderKeyRepository senderKeyRepository;
    private SessionRepository sessionRepository;
    private DbHelper dbHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        this.sekretessCryptographicService = new SekretessCryptographicService(this);
        this.dbHelper = new DbHelper(getApplicationContext());
        this.identityKeyRepository = new IdentityKeyRepository(dbHelper);
        this.kyberPreKeyRepository = new KyberPreKeyRepository(dbHelper);
        this.messageRepository = new MessageRepository(dbHelper);
        this.preKeyRepository = new PreKeyRepository(dbHelper);
        this.registrationRepository = new RegistrationRepository(dbHelper);
        this.senderKeyRepository = new SenderKeyRepository(dbHelper);
        this.sessionRepository = new SessionRepository(dbHelper);
    }

    public DbHelper getDbHelper() {
        return dbHelper;
    }

    public SekretessCryptographicService getSekretessCryptographicService() {
        return sekretessCryptographicService;
    }

    public IdentityKeyRepository getIdentityKeyRepository() {
        return identityKeyRepository;
    }

    public KyberPreKeyRepository getKyberPreKeyRepository() {
        return kyberPreKeyRepository;
    }

    public MessageRepository getMessageRepository() {
        return messageRepository;
    }

    public PreKeyRepository getPreKeyRepository() {
        return preKeyRepository;
    }

    public RegistrationRepository getRegistrationRepository() {
        return registrationRepository;
    }

    public SenderKeyRepository getSenderKeyRepository() {
        return senderKeyRepository;
    }

    public SessionRepository getSessionRepository() {
        return sessionRepository;
    }
}
