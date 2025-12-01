package io.sekretess.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import io.sekretess.SekretessApplication;
import io.sekretess.repository.MessageRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.lifecycle.MutableLiveData;

public class SekretessMessageServiceTest {

    @Mock
    private MessageRepository mockMessageRepository;

    @Mock
    private SekretessCryptographicService mockCryptographicService;

    @Mock
    private SekretessApplication mockApplication;

    @Mock
    private AuthService mockAuthService;

    private SekretessMessageService messageService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(mockApplication.getAuthService()).thenReturn(mockAuthService);
        when(mockApplication.getMessageEventsLiveData()).thenReturn(new MutableLiveData<>());
        messageService = new SekretessMessageService(mockMessageRepository, mockCryptographicService, mockApplication);
    }

    @Test
    public void testHandleMessage_advertisement() {
        String jsonPayload = "{\"sender\":\"test_sender\",\"type\":\"advertisement\",\"text\":\"test_message\"}";
        when(mockCryptographicService.decryptGroupChatMessage(anyString(), anyString())).thenReturn(Optional.of("decrypted_message"));

        messageService.handleMessage(jsonPayload);

        verify(mockMessageRepository).storeDecryptedMessage(eq("test_sender"), eq("decrypted_message"), any());
        verify(mockApplication.getMessageEventsLiveData()).postValue("new-message");
    }

    @Test
    public void testHandleMessage_private() {
        String jsonPayload = "{\"sender\":\"test_sender\",\"type\":\"private\",\"text\":\"test_message\"}";
        when(mockCryptographicService.decryptPrivateMessage(anyString(), anyString())).thenReturn(Optional.of("decrypted_message"));

        messageService.handleMessage(jsonPayload);
        verify(mockMessageRepository).storeDecryptedMessage(eq("test_sender"), eq("decrypted_message"), any());
        verify(mockApplication.getMessageEventsLiveData()).postValue("new-message");
    }

    @Test
    public void testHandleMessage_keyDistribution() {
        String jsonPayload = "{\"sender\":\"test_sender\",\"type\":\"key_distribution\",\"text\":\"test_message\"}";
        when(mockCryptographicService.decryptPrivateMessage(anyString(), anyString())).thenReturn(Optional.of("decrypted_message"));

        messageService.handleMessage(jsonPayload);

        verify(mockCryptographicService).processKeyDistributionMessage(eq("test_sender"), eq("decrypted_message"));
        verify(mockMessageRepository, never()).storeDecryptedMessage(any(), any(), any());
    }
}
