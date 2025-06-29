package io.sekretess.enums;

public enum MessageType {
    ADVERTISEMENT,
    KEY_DISTRIBUTION,
    PRIVATE,
    UNKNOWN;


    public static MessageType getInstance(String name) {
        return switch (name.toLowerCase()) {
            case "advert" -> ADVERTISEMENT;
            case "key_dist" -> KEY_DISTRIBUTION;
            case "private" -> PRIVATE;
            default -> UNKNOWN;
        };
    }
}

