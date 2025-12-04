package io.sekretess.repository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.signal.libsignal.protocol.IdentityKey;
import org.signal.libsignal.protocol.IdentityKeyPair;
import org.signal.libsignal.protocol.InvalidKeyException;
import org.signal.libsignal.protocol.SignalProtocolAddress;
import org.signal.libsignal.protocol.state.IdentityKeyStore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Base64;

public class IdentityKeyRepositoryTest {

    @Mock
    private SekretessDatabase mockSekretessDatabase;

    @Mock
    private SQLiteDatabase mockDb;

    @Mock
    private Cursor mockCursor;

    private IdentityKeyRepository identityKeyRepository;
    private IdentityKeyPair testIdentityKeyPair;
    private SignalProtocolAddress testAddress;

    @Before
    public void setUp() throws InvalidKeyException {
        MockitoAnnotations.initMocks(this);
        when(mockSekretessDatabase.getWritableDatabase()).thenReturn(mockDb);
        when(mockSekretessDatabase.getReadableDatabase()).thenReturn(mockDb);
        identityKeyRepository = new IdentityKeyRepository(mockSekretessDatabase);
        testIdentityKeyPair = IdentityKeyPair.generate();
        testAddress = new SignalProtocolAddress("test_user", 1);
    }

    @Test
    public void testGetIdentityKeyPair_exists() throws InvalidKeyException {
        String encodedKeyPair = Base64.getEncoder().encodeToString(testIdentityKeyPair.serialize());
        when(mockDb.query(anyString(), any(), any(), any(), any(), any(), any())).thenReturn(mockCursor);
        when(mockCursor.moveToNext()).thenReturn(true);
        when(mockCursor.getColumnIndexOrThrow(anyString())).thenReturn(0);
        when(mockCursor.getString(0)).thenReturn(encodedKeyPair);

        IdentityKeyPair retrievedKeyPair = identityKeyRepository.getIdentityKeyPair();

        assertNotNull(retrievedKeyPair);
        assertEquals(testIdentityKeyPair.getPublicKey(), retrievedKeyPair.getPublicKey());
    }

    @Test
    public void testGetIdentityKeyPair_notExists() {
        when(mockDb.query(anyString(), any(), any(), any(), any(), any(), any())).thenReturn(mockCursor);
        when(mockCursor.moveToNext()).thenReturn(false);

        IdentityKeyPair retrievedKeyPair = identityKeyRepository.getIdentityKeyPair();

        assertNull(retrievedKeyPair);
    }

    @Test
    public void testStoreIdentityKeyPair() {
        identityKeyRepository.storeIdentityKeyPair(testIdentityKeyPair);
        verify(mockDb).insert(eq("ikp_store"), isNull(), any(ContentValues.class));
    }

    @Test
    public void testSaveIdentity_new() {
        when(mockDb.query(anyString(), any(), any(), any(), any(), any(), any())).thenReturn(mockCursor);
        when(mockCursor.moveToNext()).thenReturn(false);

        IdentityKeyStore.IdentityChange identityChange = identityKeyRepository.saveIdentity(testAddress, testIdentityKeyPair.getPublicKey());

        assertEquals(IdentityKeyStore.IdentityChange.NEW_OR_UNCHANGED, identityChange);
        verify(mockDb).insert(eq("identity_key_store"), isNull(), any(ContentValues.class));
    }

    @Test
    public void testSaveIdentity_existing() {
        String encodedKey = Base64.getEncoder().encodeToString(testIdentityKeyPair.getPublicKey().serialize());
        when(mockDb.query(anyString(), any(), any(), any(), any(), any(), any())).thenReturn(mockCursor);
        when(mockCursor.moveToNext()).thenReturn(true);
        when(mockCursor.getColumnIndexOrThrow(anyString())).thenReturn(0);
        when(mockCursor.getString(0)).thenReturn(encodedKey);

        IdentityKeyStore.IdentityChange identityChange = identityKeyRepository.saveIdentity(testAddress, testIdentityKeyPair.getPublicKey());

        assertEquals(IdentityKeyStore.IdentityChange.REPLACED_EXISTING, identityChange);
        verify(mockDb).update(eq("identity_key_store"), any(ContentValues.class), anyString(), any());
    }

    @Test
    public void testGetIdentity_exists() throws InvalidKeyException {
        String encodedKey = Base64.getEncoder().encodeToString(testIdentityKeyPair.getPublicKey().serialize());
        when(mockDb.query(anyString(), any(), any(), any(), any(), any(), any())).thenReturn(mockCursor);
        when(mockCursor.moveToNext()).thenReturn(true).thenReturn(false);
        when(mockCursor.getColumnIndexOrThrow(anyString())).thenReturn(0);
        when(mockCursor.getString(0)).thenReturn(encodedKey);

        IdentityKey retrievedKey = identityKeyRepository.getIdentity(testAddress);

        assertNotNull(retrievedKey);
        assertEquals(testIdentityKeyPair.getPublicKey(), retrievedKey);
    }

    @Test
    public void testGetIdentity_notExists() {
        when(mockDb.query(anyString(), any(), any(), any(), any(), any(), any())).thenReturn(mockCursor);
        when(mockCursor.moveToNext()).thenReturn(false);

        IdentityKey retrievedKey = identityKeyRepository.getIdentity(testAddress);

        assertNull(retrievedKey);
    }

    @Test
    public void testClearStorage() {
        identityKeyRepository.clearStorage();
        verify(mockDb).delete(eq("ikp_store"), isNull(), isNull());
    }
}
