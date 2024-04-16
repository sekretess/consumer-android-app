package com.sekretess.model;

import android.provider.BaseColumns;

public class MessageEntity  implements BaseColumns {
    public static final String TABLE_NAME = "sekretes_messages";

    public static final String COLUMN_SENDER = "sender";
    public static final String COLUMN_CREATED_AT = "created_at";

    public static final String COLUMN_MESSAGE_BODY = "message_body";
}
