package io.sekretess.db.repository;

import android.util.Log;

import org.signal.libsignal.protocol.SignalProtocolAddress;
import org.signal.libsignal.protocol.state.SessionRecord;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import io.sekretess.db.SekretessDatabase;
import io.sekretess.db.dao.SessionDao;
import io.sekretess.db.model.SessionEntity;
import io.sekretess.dependency.SekretessDependencyProvider;

public class SessionRepository {
    private final String TAG = SessionRepository.class.getName();
    private final SessionDao sessionDao;
    private final Base64.Encoder base64Encoder = Base64.getEncoder();
    private final Base64.Decoder base64Decoder = Base64.getDecoder();


    public SessionRepository() {
        SekretessDatabase sekretessDatabase = SekretessDatabase
                .getInstance(SekretessDependencyProvider.applicationContext());
        sessionDao = sekretessDatabase.sessionDao();
    }

    public void removeSession(SignalProtocolAddress address) {
        sessionDao.removeSession(address.getName(), address.getDeviceId());
    }

    public void removeAllSessions(String name) {
        sessionDao.removeSession(name);
    }

    public List<SessionRecord> loadExistingSessions(List<SignalProtocolAddress> addresses) {
        List<SessionRecord> sessionRecords = new ArrayList<>();
        for (SignalProtocolAddress address : addresses) {
            SessionRecord sessionRecord = findSession(address);
            if (sessionRecord != null) {
                sessionRecords.add(sessionRecord);
            }
        }
        return sessionRecords;
    }

    public SessionRecord findSession(SignalProtocolAddress address) {
        SessionEntity sessionEntity = sessionDao.findSession(address.getDeviceId(),
                address.getName());
        if (sessionEntity != null) {
            try {
                return new SessionRecord(base64Decoder.decode(sessionEntity.getSession()));
            } catch (Exception e) {
                Log.e(TAG, "Error occurred during load session.", e);
                return null;
            }
        }
        return null;
    }

    public List<Integer> getSubDeviceSessions(String name) {
        return sessionDao.getSubDeviceSessions(name);
    }

    public boolean containsSession(SignalProtocolAddress address) {
        return findSession(address) != null;
    }


    public void storeSession(SignalProtocolAddress address, SessionRecord sessionRecord) {
        String serviceId = "";
        if (address.getServiceId() != null) {
            serviceId = base64Encoder.encodeToString(address.getServiceId().toServiceIdBinary());
        }
        SessionEntity sessionEntity =
                new SessionEntity(base64Encoder.encodeToString(sessionRecord.serialize()),
                        address.getName(), serviceId, address.getDeviceId(), System.currentTimeMillis());
        sessionDao.insert(sessionEntity);
    }

    public void clearStorage() {
        sessionDao.clear();
    }
}
