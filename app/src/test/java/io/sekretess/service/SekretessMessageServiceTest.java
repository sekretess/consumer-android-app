package io.sekretess.service;

import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import io.sekretess.SekretessApplication;
import io.sekretess.dto.MessageDto;
import io.sekretess.repository.MessageRepository;

@RunWith(MockitoJUnitRunner.class)

public class SekretessMessageServiceTest {

    @Mock
    private MessageRepository messageRepository;
    @Mock
    private SekretessCryptographicService sekretessCryptographicService;

    @Mock
    private SekretessApplication sekretessApplication;

    @Mock
    private AuthService authService;


    @Test
    public void handleMessage_with_valid_ADVERTISEMENT_message() throws JsonProcessingException {
        SekretessMessageService underTest = new SekretessMessageService(messageRepository, sekretessCryptographicService, sekretessApplication);
        when(authService.getUsername()).thenReturn("elnur");
        when(sekretessApplication.getAuthService()).thenReturn(authService);
        // Verify that when a valid ADVERTISEMENT message is received, `processAdvertisementMessage` is called with the correct parameters.
        MessageDto messageDto = new MessageDto();
        messageDto.setSender("test-sender");
        messageDto.setType("advert");
        messageDto.setText("test-message");
        ObjectMapper objectMapper = new ObjectMapper();
        String s = objectMapper.writeValueAsString(messageDto);
        underTest.handleMessage(s);

    }

    @Test
    public void handleMessage_with_valid_PRIVATE_message() {
        // Verify that when a valid PRIVATE message is received, `processPrivateMessage` is called with the correct parameters.
        // TODO implement test
    }

    @Test
    public void handleMessage_with_valid_KEY_DISTRIBUTION_message() {
        // Verify that when a valid KEY_DISTRIBUTION message is received, `processPrivateMessage` is called with `KEY_DISTRIBUTION` type.
        // TODO implement test
    }

    @Test
    public void handleMessage_with_malformed_JSON() {
        // Test the behavior when `messageText` is not a valid JSON string. The method should catch the `JsonProcessingException` and log the error without crashing.
        // TODO implement test
    }

    @Test
    public void handleMessage_with_missing__text__field() {
        // Test with a valid JSON but missing the 'text' field. The method should handle the resulting `NullPointerException` or similar error gracefully.
        // TODO implement test
    }

    @Test
    public void handleMessage_with_missing__type__field() {
        // Test with a valid JSON but missing the 'type' field. The method should handle the error when `MessageType.getInstance` is called with null.
        // TODO implement test
    }

    @Test
    public void handleMessage_with_invalid_message__type_() {
        // Test with a message type that is not one of the recognized `MessageType` enum values. The switch statement should fall through to the default case and do nothing.
        // TODO implement test
    }

    @Test
    public void handleMessage_with_missing_exchange_sender_fields() {
        // Test ADVERTISEMENT message missing 'businessExchange' and PRIVATE/KEY_DISTRIBUTION messages missing 'consumerExchange' or 'sender'.
        // Ensure null values are handled without crashing.
        // TODO implement test
    }

    @Test
    public void handleMessage_with_an_empty_message_string() {
        // Test how the method handles an empty string for `messageText`. It should be caught by the try-catch block.
        // TODO implement test
    }

    @Test
    public void handleMessage_when_decryption_fails_in_processAdvertisementMessage() {
        // Verify that if `decryptGroupChatMessage` returns an empty `Optional`, no message is stored, no LiveData is posted, and no notification is published.
        // TODO implement test
    }

    @Test
    public void handleMessage_when_decryption_succeeds_in_processAdvertisementMessage() {
        // Verify that upon successful decryption, `messageRepository.storeDecryptedMessage` is called, a 'new-message' event is posted, and `publishNotification` is invoked.
        // TODO implement test
    }

    @Test
    public void handleMessage_when_decryption_fails_in_processPrivateMessage() {
        // Verify that if `decryptPrivateMessage` returns an empty `Optional`, no further processing (key distribution or message storing) occurs.
        // TODO implement test
    }

    @Test
    public void handleMessage_when_PRIVATE_message_decryption_succeeds() {
        // Verify that for a decrypted PRIVATE message, the message is stored, a 'new-message' event is posted, and a notification is published.
        // TODO implement test
    }

    @Test
    public void handleMessage_when_KEY_DISTRIBUTION_message_decryption_succeeds() {
        // Verify that for a decrypted KEY_DISTRIBUTION message, `sekretessCryptographicService.processKeyDistributionMessage` is called, and the message is NOT stored.
        // TODO implement test
    }

    @Test
    public void handleMessage_when_any_dependency_throws_an_exception() {
        // Test the scenario where a dependency like `objectMapper` or `sekretessCryptographicService` throws an unexpected runtime exception.
        // The method should catch it and log the error.
        // TODO implement test
    }

    @Test
    public void getMessageBriefs_successful_retrieval() {
        // Verify that the method correctly calls `messageRepository.getMessageBriefs` with the current username and returns the expected list of `MessageBriefDto`.
        // TODO implement test
    }

    @Test
    public void getMessageBriefs_when_repository_returns_an_empty_list() {
        // Verify that an empty list is returned when the `messageRepository` finds no message briefs for the user.
        // TODO implement test
    }

    @Test
    public void getMessageBriefs_when_repository_returns_null() {
        // Test the behavior when `messageRepository.getMessageBriefs` returns null. The method should return null without crashing.
        // TODO implement test
    }

    @Test
    public void getMessageBriefs_when_auth_service_throws_an_exception() {
        // Test the scenario where `sekretessApplication.getAuthService().getUsername()` throws an exception. This should be handled by the caller or result in a crash if not caught.
        // TODO implement test
    }

    @Test
    public void getTopSenders_successful_retrieval() {
        // Verify that the method correctly calls `messageRepository.getTopSenders` and returns the expected list of sender strings.
        // TODO implement test
    }

    @Test
    public void getTopSenders_when_repository_returns_an_empty_list() {
        // Verify that an empty list is returned when the `messageRepository` finds no top senders.
        // TODO implement test
    }

    @Test
    public void getTopSenders_when_repository_returns_null() {
        // Test the behavior when `messageRepository.getTopSenders` returns null. The method should propagate the null return value.
        // TODO implement test
    }

    @Test
    public void loadMessages_with_a_valid_sender() {
        // Verify that `messageRepository.loadMessages` is called with the correct 'from' parameter and returns the corresponding list of `MessageRecordDto`.
        // TODO implement test
    }

    @Test
    public void loadMessages_for_a_sender_with_no_messages() {
        // Verify that an empty list is returned when `messageRepository.loadMessages` is called for a sender with whom there is no message history.
        // TODO implement test
    }

    @Test
    public void loadMessages_with_a_null__from__parameter() {
        // Test the behavior when the 'from' parameter is null. The method should pass the null to the repository.
        // TODO implement test
    }

    @Test
    public void loadMessages_with_an_empty_string__from__parameter() {
        // Test the behavior when the 'from' parameter is an empty string. The method should pass the empty string to the repository.
        // TODO implement test
    }

}