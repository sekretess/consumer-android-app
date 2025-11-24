package io.sekretess.repository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.signal.libsignal.protocol.InvalidMessageException;
import org.signal.libsignal.protocol.SignalProtocolAddress;
import org.signal.libsignal.protocol.groups.state.SenderKeyRecord;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

public class SenderKeyRepositoryTest {

    @Mock
    private DbHelper mockDbHelper;

    @Mock
    private SQLiteDatabase mockDb;

    @Mock
    private Cursor mockCursor;

    private SenderKeyRepository senderKeyRepository;
    private SenderKeyRecord testSenderKeyRecord;
    private SignalProtocolAddress testAddress;
    private UUID testDistributionId;

    @Before
    public void setUp() throws IOException, InvalidMessageException {
        MockitoAnnotations.initMocks(this);
        when(mockDbHelper.getWritableDatabase()).thenReturn(mockDb);
        when(mockDbHelper.getReadableDatabase()).thenReturn(mockDb);
        senderKeyRepository = new SenderKeyRepository(mockDbHelper);
        testSenderKeyRecord = new SenderKeyRecord(new byte[0]);
        testAddress = new SignalProtocolAddress("test_user", 1);
        testDistributionId = UUID.randomUUID();
    }

    @Test
    public void testStoreSenderKey() {
        senderKeyRepository.storeSenderKey(testAddress, testDistributionId, testSenderKeyRecord);
        verify(mockDb).insert(eq("sender_key_store"), isNull(), any(ContentValues.class));
    }

    @Test
    public void testLoadSenderKey() {
        when(mockDb.query(anyString(), any(), anyString(), any(), any(), any(), any())).thenReturn(mockCursor);
        when(mockCursor.moveToNext()).thenReturn(true).thenReturn(false);
        when(mockCursor.getString(1)).thenReturn(Base64.getEncoder().encodeToString(testSenderKeyRecord.serialize()));

        SenderKeyRecord record = senderKeyRepository.loadSenderKey(testAddress, testDistributionId);

        assertNotNull(record);
    }

    @Test
    public void testClearStorage() {
        senderKeyRepository.clearStorage();
        verify(mockDb).delete(eq("sender_key_store"), isNull(), isNull());
    }
}
