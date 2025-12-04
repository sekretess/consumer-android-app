package io.sekretess.repository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class RegistrationRepositoryTest {

    @Mock
    private SekretessDatabase mockSekretessDatabase;

    @Mock
    private SQLiteDatabase mockDb;

    @Mock
    private Cursor mockCursor;

    private RegistrationRepository registrationRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(mockSekretessDatabase.getWritableDatabase()).thenReturn(mockDb);
        when(mockSekretessDatabase.getReadableDatabase()).thenReturn(mockDb);
        registrationRepository = new RegistrationRepository(mockSekretessDatabase);
    }

    @Test
    public void testGetRegistrationId_exists() {
        when(mockDb.query(anyString(), any(), any(), any(), any(), any(), any())).thenReturn(mockCursor);
        when(mockCursor.moveToNext()).thenReturn(true);
        when(mockCursor.getColumnIndexOrThrow(anyString())).thenReturn(0);
        when(mockCursor.getInt(0)).thenReturn(12345);

        int registrationId = registrationRepository.getRegistrationId();

        assertEquals(12345, registrationId);
    }

    @Test
    public void testGetRegistrationId_notExists() {
        when(mockDb.query(anyString(), any(), any(), any(), any(), any(), any())).thenReturn(mockCursor);
        when(mockCursor.moveToNext()).thenReturn(false);

        int registrationId = registrationRepository.getRegistrationId();

        assertEquals(0, registrationId);
    }

    @Test
    public void testStoreRegistrationId() {
        registrationRepository.storeRegistrationId(12345);

        verify(mockDb).insert(eq("registration_id_store"), isNull(), any(ContentValues.class));
    }

    @Test
    public void testClearStorage() {
        registrationRepository.clearStorage();
        verify(mockDb).delete(eq("registration_id_store"), isNull(), isNull());
    }
}
