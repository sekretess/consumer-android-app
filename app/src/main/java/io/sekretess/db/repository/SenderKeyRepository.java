package io.sekretess.db.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import org.signal.libsignal.protocol.InvalidMessageException;
import org.signal.libsignal.protocol.SignalProtocolAddress;
import org.signal.libsignal.protocol.groups.state.SenderKeyRecord;

import java.util.Base64;
import java.util.UUID;

import io.sekretess.db.SekretessDatabase;
import io.sekretess.db.dao.SenderKeyDao;
import io.sekretess.db.model.SenderKeyEntity;
import io.sekretess.dependency.SekretessDependencyProvider;

public class SenderKeyRepository {

    private SenderKeyDao senderKeyDao;
    private final String TAG = SenderKeyRepository.class.getName();
    private final Base64.Encoder base64Encoder = Base64.getEncoder();
    private final Base64.Decoder base64Decoder = Base64.getDecoder();


    public SenderKeyRepository() {
        SekretessDatabase sekretessDatabase = SekretessDatabase.getInstance(SekretessDependencyProvider.applicationContext());
        this.senderKeyDao = sekretessDatabase.senderKeyDao();
    }

    public void storeSenderKey(SignalProtocolAddress sender, UUID distributionId, SenderKeyRecord record) {
        SenderKeyEntity senderKeyEntity = new SenderKeyEntity(sender.getDeviceId(), sender.getName(),
                base64Encoder.encodeToString(record.serialize()), distributionId.toString(),
                System.currentTimeMillis());
        senderKeyDao.insert(senderKeyEntity);
    }

    public SenderKeyRecord loadSenderKey(SignalProtocolAddress sender, UUID distributionId) {
        SenderKeyEntity senderKeyRecord = senderKeyDao.getSenderKeyRecord(sender.getDeviceId(), sender.getName(),
                distributionId.toString());

        if (senderKeyRecord != null) {
            try {
                return new SenderKeyRecord(base64Decoder.decode(senderKeyRecord.getSenderKeyRecord()));
            } catch (Exception e) {
                Log.e(TAG, "Error occurred while getting SenderKeyRecord", e);
                return null;
            }
        }
        return null;
    }

    public void clearStorage() {
        senderKeyDao.clear();
    }
}
