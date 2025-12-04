package io.sekretess.repository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class AuthRepositoryTest {

    @Mock
    private SekretessDatabase mockSekretessDatabase;

    @Mock
    private SQLiteDatabase mockDb;

    @Mock
    private Cursor mockCursor;

    @Captor
    private ArgumentCaptor<ContentValues> contentValuesCaptor;

    private AuthRepository authRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(mockSekretessDatabase.getWritableDatabase()).thenReturn(mockDb);
        when(mockSekretessDatabase.getReadableDatabase()).thenReturn(mockDb);
        authRepository = new AuthRepository(mockSekretessDatabase);
    }

    @Test
    public void testStoreAuthState() {
        String authState = "{\"token\":\"test_token\"}";
        authRepository.storeAuthState(authState);

        verify(mockDb).delete(eq("auth_state_store"), isNull(), isNull());
        verify(mockDb).insert(eq("auth_state_store"), isNull(), any(ContentValues.class));
    }

    @Test
    public void testRemoveAuthState() {
        authRepository.removeAuthState();
        verify(mockDb).delete(eq("auth_state_store"), isNull(), isNull());
    }

    @Test
    public void testGetAuthState_success() {
        when(mockDb.query(anyString(), any(), any(), any(), any(), any(), any())).thenReturn(mockCursor);
        when(mockCursor.moveToNext()).thenReturn(true);
        when(mockCursor.getColumnIndexOrThrow(anyString())).thenReturn(0);
        when(mockCursor.getString(0)).thenReturn("{\"token\":\"test_token\"}");

        String authState = authRepository.getAuthState();

        assertEquals("{\"token\":\"test_token\"}", authState);
    }

    @Test
    public void testGetAuthState_failure() {
        when(mockDb.query(anyString(), any(), any(), any(), any(), any(), any())).thenReturn(mockCursor);
        when(mockCursor.moveToNext()).thenReturn(false);

        String authState = authRepository.getAuthState();

        assertEquals(null, authState);
    }

    @Test
    public void testLogout() {
        authRepository.logout();
        verify(mockDb).beginTransaction();
        verify(mockDb).delete(eq("auth_state_store"), isNull(), isNull());
        verify(mockDb).setTransactionSuccessful();
        verify(mockDb).endTransaction();
    }
}
