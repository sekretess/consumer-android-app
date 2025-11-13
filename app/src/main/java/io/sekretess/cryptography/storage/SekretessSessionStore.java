package io.sekretess.cryptography.storage;

import org.signal.libsignal.protocol.NoSessionException;
import org.signal.libsignal.protocol.SignalProtocolAddress;
import org.signal.libsignal.protocol.state.SessionRecord;
import org.signal.libsignal.protocol.state.SessionStore;

import java.util.List;

import io.sekretess.repository.SessionRepository;

public class SekretessSessionStore implements SessionStore {

    private final SessionRepository sessionRepository;

    public SekretessSessionStore(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    public SessionRecord loadSession(SignalProtocolAddress address) {
        return sessionRepository.loadSession(address);
    }

    @Override
    public List<SessionRecord> loadExistingSessions(List<SignalProtocolAddress> addresses) throws NoSessionException {
        return sessionRepository.loadExistingSessions(addresses);
    }

    @Override
    public List<Integer> getSubDeviceSessions(String name) {
        return sessionRepository.getSubDeviceSessions(name);
    }

    @Override
    public void storeSession(SignalProtocolAddress address, SessionRecord record) {
        sessionRepository.storeSession(address, record);
    }

    @Override
    public boolean containsSession(SignalProtocolAddress address) {
        return sessionRepository.containsSession(address);
    }

    @Override
    public void deleteSession(SignalProtocolAddress address) {
        sessionRepository.removeSession(address);
    }

    @Override
    public void deleteAllSessions(String name) {
        sessionRepository.removeAllSessions(name);
    }

    public void clearStorage() {
        sessionRepository.clearStorage();
    }
}
