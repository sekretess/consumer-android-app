package io.sekretess.repository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.signal.libsignal.protocol.SignalProtocolAddress;
import org.signal.libsignal.protocol.state.SessionRecord;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import io.sekretess.model.SessionStoreEntity;

public class SessionRepositoryTest {

    @Mock
    private SekretessDatabase mockSekretessDatabase;

    @Mock
    private SQLiteDatabase mockDb;

    @Mock
    private Cursor mockCursor;

    private SessionRepository sessionRepository;
    private SessionRecord testSessionRecord;
    private SignalProtocolAddress testAddress;

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        when(mockSekretessDatabase.getWritableDatabase()).thenReturn(mockDb);
        when(mockSekretessDatabase.getReadableDatabase()).thenReturn(mockDb);
        sessionRepository = new SessionRepository(mockSekretessDatabase);
        testSessionRecord = new SessionRecord();
        testAddress = new SignalProtocolAddress("test_user", 1);
    }

    @Test
    public void testRemoveSession() {
        sessionRepository.removeSession(testAddress);
        verify(mockDb).delete(eq("session_store"), anyString(), any());
    }

    @Test
    public void testRemoveAllSessions() {
        sessionRepository.removeAllSessions("test_user");
        verify(mockDb).delete(eq("session_store"), anyString(), any());
    }

    @Test
    public void testLoadExistingSessions() {
        when(mockDb.query(anyString(), any(), anyString(), any(), any(), any(), any())).thenReturn(mockCursor);
        when(mockCursor.moveToNext()).thenReturn(true).thenReturn(false);
        when(mockCursor.getColumnIndexOrThrow(SessionStoreEntity.COLUMN_SESSION)).thenReturn(0);
        when(mockCursor.getString(0)).thenReturn(Base64.getEncoder().encodeToString(testSessionRecord.serialize()));

        List<SessionRecord> records = sessionRepository.loadExistingSessions(Collections.singletonList(testAddress));

        assertNotNull(records);
        assertFalse(records.isEmpty());
    }

    @Test
    public void testLoadSession() {
        when(mockDb.query(anyString(), any(), anyString(), any(), any(), any(), any())).thenReturn(mockCursor);
        when(mockCursor.moveToNext()).thenReturn(true).thenReturn(false);
        when(mockCursor.getColumnIndexOrThrow(SessionStoreEntity.COLUMN_SESSION)).thenReturn(0);
        when(mockCursor.getString(0)).thenReturn(Base64.getEncoder().encodeToString(testSessionRecord.serialize()));

        SessionRecord record = sessionRepository.loadSession(testAddress);

        assertNotNull(record);
    }

    @Test
    public void testGetSubDeviceSessions() {
        when(mockDb.query(anyString(), any(), anyString(), any(), any(), any(), any())).thenReturn(mockCursor);
        when(mockCursor.moveToNext()).thenReturn(true).thenReturn(false);
        when(mockCursor.getColumnIndexOrThrow(SessionStoreEntity.COLUMN_ADDRESS_DEVICE_ID)).thenReturn(0);
        when(mockCursor.getInt(0)).thenReturn(2);

        List<Integer> deviceIds = sessionRepository.getSubDeviceSessions("test_user");

        assertNotNull(deviceIds);
        assertFalse(deviceIds.isEmpty());
        assertEquals(Integer.valueOf(2), deviceIds.get(0));
    }

    @Test
    public void testContainsSession() {
        when(mockDb.query(anyString(), any(), anyString(), any(), any(), any(), any())).thenReturn(mockCursor);
        when(mockCursor.moveToNext()).thenReturn(true).thenReturn(false);
        when(mockCursor.getColumnIndexOrThrow(SessionStoreEntity.COLUMN_SESSION)).thenReturn(0);
        when(mockCursor.getString(0)).thenReturn(Base64.getEncoder().encodeToString(testSessionRecord.serialize()));

        assertTrue(sessionRepository.containsSession(testAddress));
    }

    @Test
    public void testStoreSession() {
        sessionRepository.storeSession(testAddress, testSessionRecord);
        verify(mockDb).insert(eq("session_store"), isNull(), any(ContentValues.class));
    }

    @Test
    public void testClearStorage() {
        sessionRepository.clearStorage();
        verify(mockDb).delete(eq("session_store"), isNull(), isNull());
    }
}
