package com.sekretess;

import android.net.Uri;

import java.net.URI;

public interface Constants {

    String USERNAME_CLAIM = "preferred_username";
    String EVENT_LOGIN = "login-event";
    String EVENT_INITIALIZE_KEY = "initialize-key-event";
    String EVENT_UPDATE_KEY = "update-key-event";
    String EVENT_NEW_INCOMING_ENCRYPTED_MESSAGE = "new-incoming-encrypted-message-event";

    String EVENT_REFRESH_TOKEN_FAILED = "refresh-token-failed-event";

    String EVENT_NEW_INCOMING_MESSAGE = "new-incoming-mesage-event";
    String EVENT_SIGNUP_FAILED = "signup-failed-event";

    byte[] password = "sekretes_!23dg5333".getBytes();

    String CONSUMER_API_URL = "http://consumer.sekretess.co:8081/api/v1/consumers";
    String BUSINESS_API_URL = "http://business.sekretess.co:8082/api/v1/businesses";

    Uri KEYCLOAK_OPENID_CONFIGURATION_URL = Uri.parse("https://auth.sekretess.co/realms/consumer/.well-known/openid-configuration");


    Uri AUTH_REDIRECT_URL = Uri.parse("com.sekretess:/oauth2redirect");
}
