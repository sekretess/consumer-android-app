package io.sekretess.repository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import io.sekretess.dto.MessageBriefDto;
import io.sekretess.dto.MessageRecordDto;

public class MessageRepositoryTest {

    @Mock
    private DbHelper mockDbHelper;

    @Mock
    private SQLiteDatabase mockDb;

    @Mock
    private Cursor mockCursor;

    private MessageRepository messageRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(mockDbHelper.getWritableDatabase()).thenReturn(mockDb);
        when(mockDbHelper.getReadableDatabase()).thenReturn(mockDb);
        messageRepository = new MessageRepository(mockDbHelper);
    }

    @Test
    public void testStoreDecryptedMessage() {
        messageRepository.storeDecryptedMessage("sender", "message", "username");
        verify(mockDb).insert(eq("sekretes_message_store"), isNull(), any(ContentValues.class));
    }

    @Test
    public void testGetMessageBriefs() {
        when(mockDb.query(anyString(), any(), anyString(), any(), any(), any(), anyString(), anyString())).thenReturn(mockCursor);
        when(mockCursor.moveToNext()).thenReturn(true).thenReturn(false);
        when(mockCursor.getString(0)).thenReturn("sender");
        when(mockCursor.getString(1)).thenReturn("message");

        List<MessageBriefDto> briefs = messageRepository.getMessageBriefs("username");

        assertNotNull(briefs);
        assertFalse(briefs.isEmpty());
        assertEquals("sender", briefs.get(0).getSender());
        assertEquals("message", briefs.get(0).getMessageText());
    }

    @Test
    public void testGetTopSenders() {
        when(mockDb.query(anyString(), any(), any(), any(), any(), any(), anyString(), anyString())).thenReturn(mockCursor);
        when(mockCursor.moveToNext()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(mockCursor.getString(0)).thenReturn("sender1").thenReturn("sender2");

        List<String> topSenders = messageRepository.getTopSenders();

        assertNotNull(topSenders);
        assertEquals(2, topSenders.size());
        assertEquals("sender1", topSenders.get(0));
        assertEquals("sender2", topSenders.get(1));
    }

    @Test
    public void testLoadMessages() {
        when(mockDb.query(anyString(), any(), anyString(), any(), any(), any(), anyString())).thenReturn(mockCursor);
        when(mockCursor.moveToNext()).thenReturn(true).thenReturn(false);
        when(mockCursor.getLong(0)).thenReturn(1L);
        when(mockCursor.getString(1)).thenReturn("sender");
        when(mockCursor.getString(2)).thenReturn("message");
        when(mockCursor.getLong(3)).thenReturn(System.currentTimeMillis());

        List<MessageRecordDto> messages = messageRepository.loadMessages("sender");

        assertNotNull(messages);
        assertFalse(messages.isEmpty());
    }
}
