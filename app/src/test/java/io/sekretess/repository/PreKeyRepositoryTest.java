package io.sekretess.repository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.signal.libsignal.protocol.InvalidMessageException;
import org.signal.libsignal.protocol.state.PreKeyRecord;
import org.signal.libsignal.protocol.state.SignalProtocolStore;
import org.signal.libsignal.protocol.state.SignedPreKeyRecord;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

public class PreKeyRepositoryTest {

    @Mock
    private SekretessDatabase mockSekretessDatabase;

    @Mock
    private SQLiteDatabase mockDb;

    @Mock
    private Cursor mockCursor;

    @Mock
    private PreKeyRecord mockPreKeyRecord;

    @Mock
    private SignedPreKeyRecord mockSignedPreKeyRecord;

    private PreKeyRepository preKeyRepository;

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        when(mockSekretessDatabase.getWritableDatabase()).thenReturn(mockDb);
        when(mockSekretessDatabase.getReadableDatabase()).thenReturn(mockDb);
        preKeyRepository = new PreKeyRepository(mockSekretessDatabase);

        when(mockPreKeyRecord.getId()).thenReturn(1);
        when(mockPreKeyRecord.serialize()).thenReturn(new byte[0]);
        when(mockSignedPreKeyRecord.getId()).thenReturn(1);
        when(mockSignedPreKeyRecord.serialize()).thenReturn(new byte[0]);
    }

    @Test
    public void testLoadSignedPreKeys() throws InvalidMessageException {
        String encodedRecord = Base64.getEncoder().encodeToString(mockSignedPreKeyRecord.serialize());
        when(mockDb.query(anyString(), any(), any(), any(), any(), any(), any())).thenReturn(mockCursor);
        when(mockCursor.moveToNext()).thenReturn(true).thenReturn(false);
        when(mockCursor.getColumnIndexOrThrow(anyString())).thenReturn(0);
        when(mockCursor.getString(0)).thenReturn(encodedRecord);

        List<SignedPreKeyRecord> records = preKeyRepository.loadSignedPreKeys();

        assertEquals(1, records.size());
    }

    @Test
    public void testGetSignedPreKeyRecord() throws InvalidMessageException {
        String encodedRecord = Base64.getEncoder().encodeToString(mockSignedPreKeyRecord.serialize());
        when(mockDb.query(anyString(), any(), any(), any(), any(), any(), any(), any())).thenReturn(mockCursor);
        when(mockCursor.moveToNext()).thenReturn(true).thenReturn(false);
        when(mockCursor.getColumnIndexOrThrow(anyString())).thenReturn(0);
        when(mockCursor.getString(0)).thenReturn(encodedRecord);

        SignedPreKeyRecord record = preKeyRepository.getSignedPreKeyRecord(1);

        assertNotNull(record);
    }

    @Test
    public void testRemoveSignedPreKey() {
        preKeyRepository.removeSignedPreKey(1);
        verify(mockDb).delete(eq("signed_prekey_record_store"), eq("spk_id=?"), any());
    }

    @Test
    public void testStoreSignedPreKeyRecord() {
        preKeyRepository.storeSignedPreKeyRecord(mockSignedPreKeyRecord);
        verify(mockDb).insert(eq("signed_prekey_record_store"), isNull(), any(ContentValues.class));
    }

    @Test
    public void testLoadPreKeyRecords() throws InvalidMessageException {
        String encodedRecord = Base64.getEncoder().encodeToString(mockPreKeyRecord.serialize());
        when(mockDb.query(anyString(), any(), any(), any(), any(), any(), any())).thenReturn(mockCursor);
        when(mockCursor.moveToNext()).thenReturn(true).thenReturn(false);
        when(mockCursor.getColumnIndexOrThrow(anyString())).thenReturn(0);
        when(mockCursor.getString(0)).thenReturn(encodedRecord);
        when(mockPreKeyRecord.getId()).thenReturn(0);

        SignalProtocolStore mockStore = mock(SignalProtocolStore.class);
        preKeyRepository.loadPreKeyRecords(mockStore);

        verify(mockStore).storePreKey(eq(mockPreKeyRecord.getId()), any(PreKeyRecord.class));
    }

    @Test
    public void testRemovePreKeyRecord() {
        preKeyRepository.removePreKeyRecord(1);
        verify(mockDb).delete(eq("prekey_record_store"), eq("prekey_id=?"), any());
    }

    @Test
    public void testStorePreKeyRecord() {
        preKeyRepository.storePreKeyRecord(mockPreKeyRecord);
        verify(mockDb).insert(eq("prekey_record_store"), isNull(), any(ContentValues.class));
    }

    @Test
    public void testLoadPreKey() throws InvalidMessageException {
        String encodedRecord = Base64.getEncoder().encodeToString(mockPreKeyRecord.serialize());
        when(mockDb.query(anyString(), any(), any(), any(), any(), any(), any())).thenReturn(mockCursor);
        when(mockCursor.moveToNext()).thenReturn(true).thenReturn(false);
        when(mockCursor.getColumnIndexOrThrow(anyString())).thenReturn(0);
        when(mockCursor.getString(0)).thenReturn(encodedRecord);

        PreKeyRecord record = preKeyRepository.loadPreKey(1);

        assertNotNull(record);
    }

    @Test
    public void testClearStorage() {
        preKeyRepository.clearStorage();
        verify(mockDb).delete(eq("prekey_record_store"), isNull(), isNull());
    }
}
