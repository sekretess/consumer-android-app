package com.sekretess;

public interface Constants {
    String EVENT_LOGIN = "login-event";
    String EVENT_INITIALIZE_KEY = "initialize-key-event";
    String EVENT_UPDATE_KEY = "update-key-event";
    String EVENT_NEW_INCOMING_ENCRYPTED_MESSAGE = "new-incoming-encrypted-message-event";

    String EVENT_REFRESH_TOKEN_FAILED = "refresh-token-failed-event";
    String EVENT_NEW_INCOMING_MESSAGE = "new-incoming-mesage-event";
    String EVENT_SIGNUP_FAILED = "signup-failed-event";

    byte[] password = "sekretes_!23dg5333".getBytes();
}
