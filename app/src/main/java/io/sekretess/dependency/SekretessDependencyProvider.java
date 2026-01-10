package io.sekretess.dependency;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import io.sekretess.cryptography.storage.SekretessSignalProtocolStore;
import io.sekretess.db.repository.AuthRepository;
import io.sekretess.db.repository.IdentityKeyRepository;
import io.sekretess.db.repository.KyberPreKeyRepository;
import io.sekretess.db.repository.MessageRepository;
import io.sekretess.db.repository.PreKeyRepository;
import io.sekretess.db.repository.RegistrationRepository;
import io.sekretess.db.SekretessDatabase;
import io.sekretess.db.repository.SenderKeyRepository;
import io.sekretess.db.repository.SessionRepository;
import io.sekretess.db.repository.SignedPreKeyRepository;
import io.sekretess.enums.SekretessEvent;
import io.sekretess.service.AuthService;
import io.sekretess.websocket.SekretessAuthenticatedWebSocket;
import io.sekretess.service.SekretessCryptographicService;
import io.sekretess.service.SekretessMessageService;
import io.sekretess.utils.ApiClient;

public class SekretessDependencyProvider {
    private static SekretessCryptographicService sekretessCryptographicService;
    private static SekretessMessageService sekretessMessageService;
    private static SekretessAuthenticatedWebSocket sekretessAuthenticatedWebSocket;
    private static AuthService authService;
    private static ApiClient apiClient;
    private static Context rootContext;
    private static MutableLiveData<String> messageEventStream = new MutableLiveData<>();

    private static MutableLiveData<SekretessEvent> sekretessEventMutableLiveData = new MutableLiveData<>();


    public SekretessDependencyProvider(Context context) {
        rootContext = context;

        SekretessSignalProtocolStore sekretessSignalProtocolStore = getSekretessSignalProtocolStore();
        sekretessCryptographicService = new SekretessCryptographicService(sekretessSignalProtocolStore);

        MessageRepository messageRepository = new MessageRepository();
        sekretessMessageService = new SekretessMessageService(messageRepository);

        apiClient = new ApiClient();

        AuthRepository authRepository = new AuthRepository();
        authService = new AuthService(authRepository, apiClient());



        sekretessAuthenticatedWebSocket = new SekretessAuthenticatedWebSocket();
    }

    @NonNull
    private static SekretessSignalProtocolStore getSekretessSignalProtocolStore() {
        IdentityKeyRepository identityKeyRepository = new IdentityKeyRepository();
        RegistrationRepository registrationRepository = new RegistrationRepository();
        PreKeyRepository preKeyRepository = new PreKeyRepository();
        SignedPreKeyRepository signedPreKeyRepository = new SignedPreKeyRepository();
        SessionRepository sessionRepository = new SessionRepository();
        SenderKeyRepository senderKeyRepository = new SenderKeyRepository();
        KyberPreKeyRepository kyberPreKeyRepository = new KyberPreKeyRepository();

        return new SekretessSignalProtocolStore(identityKeyRepository, registrationRepository,
                preKeyRepository, signedPreKeyRepository, sessionRepository, senderKeyRepository,
                kyberPreKeyRepository);
    }

    public static SekretessMessageService messageService() {
        return sekretessMessageService;
    }

    public static AuthService authService() {
        return authService;
    }

    public static SekretessCryptographicService cryptographicService() {
        return sekretessCryptographicService;
    }

    public static Context applicationContext() {
        return rootContext;
    }

    public static MutableLiveData<String> messageEventStream() {
        return messageEventStream;
    }

    public static MutableLiveData<SekretessEvent> getSekretessEventMutableLiveData() {
        return sekretessEventMutableLiveData;
    }

    public static ApiClient apiClient() {
        return apiClient;
    }

    public static SekretessAuthenticatedWebSocket authenticatedWebSocket() {
        return sekretessAuthenticatedWebSocket;
    }
}
