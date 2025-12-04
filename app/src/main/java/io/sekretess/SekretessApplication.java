package io.sekretess;

import android.app.Application;

import io.sekretess.cryptography.storage.SekretessSignalProtocolStore;
import io.sekretess.dependency.SekretessDependencyProvider;
import io.sekretess.repository.AuthRepository;
import io.sekretess.repository.SekretessDatabase;
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


    @Override
    public void onCreate() {
        super.onCreate();
        new SekretessDependencyProvider(getApplicationContext());
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        SekretessDependencyProvider.authenticatedWebSocket().destroy();
    }
}
