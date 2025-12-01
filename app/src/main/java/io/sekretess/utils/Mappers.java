package io.sekretess.utils;

import org.signal.libsignal.protocol.InvalidKeyException;
import org.signal.libsignal.protocol.state.KyberPreKeyRecord;

import java.util.Base64;

import io.sekretess.dto.KeyBundleDto;
import io.sekretess.dto.KeyBundle;
import io.sekretess.dto.UserDto;

public class Mappers {
    private static final Base64.Encoder encoder = Base64.getEncoder();

    public static KeyBundleDto toKeyBundleDto(KeyBundle keyBundle) throws InvalidKeyException {
        KeyBundleDto keyBundleDto = new KeyBundleDto();
        keyBundleDto.setRegId(keyBundle.getRegistrationId());
        keyBundleDto.setIk(encoder.encodeToString(keyBundle.getIdentityKeyPair().getPublicKey()
                .serialize()));
        keyBundleDto.setSpk(encoder.encodeToString(keyBundle.getSignedPreKeyRecord().getKeyPair()
                .getPublicKey().serialize()));
        keyBundleDto.setSpkID(String.valueOf(keyBundle.getSignedPreKeyRecord().getId()));
        keyBundleDto.setSPKSignature(encoder.encodeToString(keyBundle.getSignedPreKeyRecord().getSignature()));
        keyBundleDto.setOpk(SerializationUtils.serializeSignedPreKeys(keyBundle.getOpk()));
        // Setting PostQuantum keys
        KyberPreKeyRecord[] kyberPreKeyRecords = keyBundle.getKyberPreKeyRecords();
        KyberPreKeyRecord lastKyberPreKeyRecord = kyberPreKeyRecords[kyberPreKeyRecords.length - 1];
        keyBundleDto.setOpqk(SerializationUtils.serializeKyberPreKeys(kyberPreKeyRecords));
        keyBundleDto.setPqspk(SerializationUtils.serializeKyberPreKey(lastKyberPreKeyRecord));
        keyBundleDto.setPqspkSignature(encoder.encodeToString(lastKyberPreKeyRecord.getSignature()));
        keyBundleDto.setPqspkid(String.valueOf(lastKyberPreKeyRecord.getId()));

        return keyBundleDto;
    }

    public static UserDto applyKeyBundle(UserDto userDto, KeyBundle keyBundle) throws InvalidKeyException {
        userDto.setRegId(keyBundle.getRegistrationId());
        userDto.setIk(encoder.encodeToString(keyBundle.getIdentityKeyPair().getPublicKey()
                .serialize()));
        userDto.setSpk(encoder.encodeToString(keyBundle.getSignedPreKeyRecord().getKeyPair()
                .getPublicKey().serialize()));
        userDto.setSpkID(String.valueOf(keyBundle.getSignedPreKeyRecord().getId()));
        userDto.setSPKSignature(encoder.encodeToString(keyBundle.getSignedPreKeyRecord().getSignature()));
        userDto.setOpk(SerializationUtils.serializeSignedPreKeys(keyBundle.getOpk()));
        // Setting PostQuantum keys
        KyberPreKeyRecord[] kyberPreKeyRecords = keyBundle.getKyberPreKeyRecords();
        KyberPreKeyRecord lastKyberPreKeyRecord = kyberPreKeyRecords[kyberPreKeyRecords.length - 1];
        userDto.setOpqk(SerializationUtils.serializeKyberPreKeys(kyberPreKeyRecords));
        userDto.setPqspk(SerializationUtils.serializeKyberPreKey(lastKyberPreKeyRecord));
        userDto.setPqspkSignature(encoder.encodeToString(lastKyberPreKeyRecord.getSignature()));
        userDto.setPqspkid(String.valueOf(lastKyberPreKeyRecord.getId()));

        return userDto;
    }
}
