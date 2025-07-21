package io.sekretess.utils;

import org.signal.libsignal.protocol.InvalidKeyException;

import java.util.Base64;

import io.sekretess.dto.KeyBundleDto;
import io.sekretess.dto.KeyMaterial;
import io.sekretess.dto.UserDto;

public class Mappers {
    private static final Base64.Encoder encoder = Base64.getEncoder();

    public static UserDto toUserDto(KeyMaterial keyMaterial) throws InvalidKeyException {
        UserDto userDto = new UserDto();
        userDto.setRegId(keyMaterial.getRegistrationId());
        userDto.setIk(encoder.encodeToString(keyMaterial.getIdentityKeyPair().getPublicKey()
                .serialize()));
        userDto.setSpk(encoder.encodeToString(keyMaterial.getSignedPreKeyRecord().getKeyPair()
                .getPublicKey().serialize()));
        userDto.setSpkID(String.valueOf(keyMaterial.getSignedPreKeyRecord().getId()));
        userDto.setSPKSignature(encoder.encodeToString(keyMaterial.getSignedPreKeyRecord().getSignature()));
        userDto.setOpk(keyMaterial.getOpk());
        // Setting PostQuantum keys
        userDto.setOpqk(keyMaterial.getSerializedKyberPreKeys());
        userDto.setPqspk(keyMaterial.getLastResortKyberPreKey());
        userDto.setPqspkSignature(encoder.encodeToString(keyMaterial.getLastResortKeyberPreKeySignature()));
        userDto.setPqspkid(String.valueOf(keyMaterial.getLastResortKyberPreKeyId()));
        return userDto;
    }

    public static KeyBundleDto toKeyBundleDto(KeyMaterial keyMaterial) throws InvalidKeyException {
        KeyBundleDto keyBundle = new KeyBundleDto();
        keyBundle.setRegId(keyMaterial.getRegistrationId());
        keyBundle.setIk(encoder.encodeToString(keyMaterial.getIdentityKeyPair().getPublicKey()
                .serialize()));
        keyBundle.setSpk(encoder.encodeToString(keyMaterial.getSignedPreKeyRecord().getKeyPair()
                .getPublicKey().serialize()));
        keyBundle.setSpkID(String.valueOf(keyMaterial.getSignedPreKeyRecord().getId()));
        keyBundle.setSPKSignature(encoder.encodeToString(keyMaterial.getSignedPreKeyRecord().getSignature()));
        keyBundle.setOpk(keyMaterial.getOpk());
        // Setting PostQuantum keys
        keyBundle.setOpqk(keyMaterial.getSerializedKyberPreKeys());
        keyBundle.setPqspk(keyMaterial.getLastResortKyberPreKey());
        keyBundle.setPqspkSignature(encoder.encodeToString(keyMaterial.getLastResortKeyberPreKeySignature()));
        keyBundle.setPqspkid(String.valueOf(keyMaterial.getLastResortKyberPreKeyId()));
        return keyBundle;
    }
}
