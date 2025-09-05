package io.sekretess.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.Base64;

public class ImageUtils {

    public static Bitmap bitmapFromBase64(String imageBase64) {
        byte[] imageBytes = Base64.getDecoder().decode(imageBase64);
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }
}
