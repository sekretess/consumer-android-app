package io.sekretess.repository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.signal.libsignal.protocol.InvalidMessageException;
import org.signal.libsignal.protocol.state.KyberPreKeyRecord;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.IOException;
import java.util.List;

import io.sekretess.model.KyberPreKeyRecordsEntity;

public class KyberPreKeyRepositoryTest {

    @Mock
    private DbHelper mockDbHelper;

    @Mock
    private SQLiteDatabase mockDb;

    @Mock
    private Cursor mockCursor;

    private KyberPreKeyRepository kyberPreKeyRepository;

    @Before
    public void setUp() throws IOException, InvalidMessageException {
        MockitoAnnotations.openMocks(this);
        when(mockDbHelper.getWritableDatabase()).thenReturn(mockDb);
        when(mockDbHelper.getReadableDatabase()).thenReturn(mockDb);
        kyberPreKeyRepository = new KyberPreKeyRepository(mockDbHelper);
    }

    @Test
    public void testMarkKyberPreKeyUsed() {
        kyberPreKeyRepository.markKyberPreKeyUsed(1);
        verify(mockDb).updateWithOnConflict(eq(KyberPreKeyRecordsEntity.TABLE_NAME), any(ContentValues.class),
                        anyString(), any(), anyInt());
    }

    @Test
    public void testStoreKyberPreKey() throws InvalidMessageException {
        KyberPreKeyRecord mockKyberPreKeyRecord = mock(KyberPreKeyRecord.class);
        when(mockKyberPreKeyRecord.getId()).thenReturn(1);
        when(mockKyberPreKeyRecord.serialize()).thenReturn(new byte[0]);
        kyberPreKeyRepository.storeKyberPreKey(mockKyberPreKeyRecord);
        verify(mockDb).insert(eq(KyberPreKeyRecordsEntity.TABLE_NAME), isNull(), any(ContentValues.class));
    }

    @Test
    public void testLoadKyberPreKey_invalidData() {
        when(mockDb.query(anyString(), any(), anyString(), any(), any(), any(), any())).thenReturn(mockCursor);
        when(mockCursor.moveToNext()).thenReturn(true).thenReturn(false);
        when(mockCursor.getColumnIndexOrThrow(KyberPreKeyRecordsEntity.COLUMN_KPK_RECORD)).thenReturn(0);
        when(mockCursor.getString(0)).thenReturn("invalid");

        KyberPreKeyRecord record = kyberPreKeyRepository.loadKyberPreKey(1);

        assertNull(record);
    }

    @Test
    public void testLoadKyberPreKeys_invalidData() {
        when(mockDb.query(anyString(), any(), any(), any(), any(), any(), any())).thenReturn(mockCursor);
        when(mockCursor.moveToNext()).thenReturn(true).thenReturn(false);
        when(mockCursor.getColumnIndexOrThrow(KyberPreKeyRecordsEntity.COLUMN_KPK_RECORD)).thenReturn(0);
        when(mockCursor.getString(0)).thenReturn("invalid");

        List<KyberPreKeyRecord> records = kyberPreKeyRepository.loadKyberPreKeys();

        assertNotNull(records);
        assertTrue(records.isEmpty());
    }

    @Test
    public void testClearStorage() {
        kyberPreKeyRepository.clearStorage();
        verify(mockDb).delete(eq(KyberPreKeyRecordsEntity.TABLE_NAME), isNull(), isNull());
    }
}
