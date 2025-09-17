package io.sekretess;

import android.net.Uri;

public interface Constants {

    String USERNAME_CLAIM = "preferred_username";
    String EVENT_LOGIN = "login-event";
    String EVENT_SIGNUP = "signup-event";
    String EVENT_UPDATE_KEY = "update-key-event";


    String SEKRETESS_NOTIFICATION_CHANNEL_NAME = "sekretess_notif";
    String EVENT_TOKEN_ISSUE = "token-issue-event";

    String EVENT_NEW_INCOMING_MESSAGE = "new-incoming-mesage-event";
    String RABBIT_MQ_CONSUMER_QUEUE_SUFFIX = "_consumer";


    Uri AUTH_REDIRECT_URL = Uri.parse("io.sekretess:/oauth2redirect");
}
