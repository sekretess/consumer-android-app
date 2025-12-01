package io.sekretess;

import android.app.Application;

import androidx.lifecycle.MutableLiveData;

import io.sekretess.cryptography.storage.SekretessSignalProtocolStore;
import io.sekretess.repository.AuthRepository;
import io.sekretess.repository.DbHelper;
import io.sekretess.repository.IdentityKeyRepository;
import io.sekretess.repository.KyberPreKeyRepository;
import io.sekretess.repository.MessageRepository;
import io.sekretess.repository.PreKeyRepository;
import io.sekretess.repository.RegistrationRepository;
import io.sekretess.repository.SenderKeyRepository;
import io.sekretess.repository.SessionRepository;
import io.sekretess.service.AuthService;
import io.sekretess.service.SekretessCryptographicService;
import io.sekretess.service.SekretessMessageService;
import io.sekretess.service.SekretessAuthenticatedWebSocket;
import io.sekretess.utils.ApiClient;

public class SekretessApplication extends Application {

    private SekretessCryptographicService sekretessCryptographicService;
    private SekretessMessageService sekretessMessageService;
    private final MutableLiveData<String> messageEventsLiveData = new MutableLiveData<>();
    private SekretessAuthenticatedWebSocket sekretessAuthenticatedWebSocket;
    private AuthService authService;
    private ApiClient apiClient;
    private DbHelper dbHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        this.dbHelper = new DbHelper(getApplicationContext());
        IdentityKeyRepository identityKeyRepository = new IdentityKeyRepository(dbHelper);
        KyberPreKeyRepository kyberPreKeyRepository = new KyberPreKeyRepository(dbHelper);
        MessageRepository messageRepository = new MessageRepository(dbHelper);
        PreKeyRepository preKeyRepository = new PreKeyRepository(dbHelper);
        RegistrationRepository registrationRepository = new RegistrationRepository(dbHelper);
        SenderKeyRepository senderKeyRepository = new SenderKeyRepository(dbHelper);
        SessionRepository sessionRepository = new SessionRepository(dbHelper);

        SekretessSignalProtocolStore sekretessSignalProtocolStore =
                new SekretessSignalProtocolStore(this, identityKeyRepository,
                        registrationRepository, preKeyRepository, sessionRepository,
                        senderKeyRepository, kyberPreKeyRepository);


        this.apiClient = new ApiClient(this);

        this.sekretessCryptographicService =
                new SekretessCryptographicService(this, sekretessSignalProtocolStore, apiClient);
        this.sekretessMessageService = new SekretessMessageService(messageRepository,
                sekretessCryptographicService, this);
        AuthRepository authRepository = new AuthRepository(dbHelper);
        this.authService = new AuthService(apiClient, authRepository);
        this.sekretessAuthenticatedWebSocket = new SekretessAuthenticatedWebSocket(sekretessMessageService, authService);

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        sekretessAuthenticatedWebSocket.destroy();
    }

    public DbHelper getDbHelper() {
        return dbHelper;
    }

    public SekretessCryptographicService getSekretessCryptographicService() {
        return sekretessCryptographicService;
    }

    public SekretessMessageService getSekretessMessageService() {
        return sekretessMessageService;
    }

    public MutableLiveData<String> getMessageEventsLiveData() {
        return messageEventsLiveData;
    }

    public SekretessAuthenticatedWebSocket getSekretessWebSocketClient() {
        return sekretessAuthenticatedWebSocket;
    }

    public AuthService getAuthService() {
        return authService;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }
}
