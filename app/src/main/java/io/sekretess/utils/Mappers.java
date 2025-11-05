package io.sekretess.utils;

import androidx.annotation.WorkerThread;

import org.signal.libsignal.protocol.InvalidKeyException;

import java.util.Base64;

import io.sekretess.dto.KeyBundleDto;
import io.sekretess.dto.KeyMaterial;
import io.sekretess.dto.UserDto;

public class Mappers {
    private static final Base64.Encoder encoder = Base64.getEncoder();

    public static KeyBundleDto toKeyBundleDto(KeyMaterial keyMaterial) throws InvalidKeyException {
        KeyBundleDto keyBundle = new KeyBundleDto();
        keyBundle.setRegId(keyMaterial.getRegistrationId());
        keyBundle.setIk(encoder.encodeToString(keyMaterial.getIdentityKeyPair().getPublicKey()
                .serialize()));
        keyBundle.setSpk(encoder.encodeToString(keyMaterial.getSignedPreKeyRecord().getKeyPair()
                .getPublicKey().serialize()));
        keyBundle.setSpkID(String.valueOf(keyMaterial.getSignedPreKeyRecord().getId()));
        keyBundle.setSPKSignature(encoder.encodeToString(keyMaterial.getSignedPreKeyRecord().getSignature()));
        keyBundle.setOpk(SerializationUtils.serializeSignedPreKeys(keyMaterial.getOpk()));
        // Setting PostQuantum keys
        keyBundle.setOpqk(SerializationUtils.serializeKyberPreKeys(keyMaterial.getSerializedKyberPreKeys()));
        keyBundle.setPqspk(SerializationUtils.serializeKyberPreKey(keyMaterial.getLastResortKyberPreKey()));
        keyBundle.setPqspkSignature(encoder.encodeToString(keyMaterial.getLastResortKeyberPreKeySignature()));
        keyBundle.setPqspkid(String.valueOf(keyMaterial.getLastResortKyberPreKeyId()));
        return keyBundle;
    }
}
