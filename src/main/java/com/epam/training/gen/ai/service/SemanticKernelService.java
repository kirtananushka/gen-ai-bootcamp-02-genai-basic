package com.epam.training.gen.ai.service;

import com.epam.training.gen.ai.dto.BookDto;
import com.epam.training.gen.ai.dto.ChatBookResponse;
import com.epam.training.gen.ai.dto.ChatRequest;
import com.epam.training.gen.ai.dto.ChatResponse;
import com.epam.training.gen.ai.exception.OpenAiException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.orchestration.FunctionResult;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class SemanticKernelService {
    private final Kernel kernel;
    private final ObjectMapper objectMapper;
    private final ChatHistory chatHistory;
    private final ChatCompletionService chatCompletionService;

    public ChatResponse getSimplePromptResponse(ChatRequest chatRequest) {
        log.info("Entering getSimplePromptResponse with input: {}", chatRequest.getInput());
        String userInput = chatRequest.getInput();
        String result = Objects.requireNonNull(Objects.requireNonNull(kernel.invokePromptAsync(userInput).block()).getResult()).toString();
        log.info("Received result: {}", result);
        ChatResponse response = new ChatResponse(List.of(result));
        log.info("Exiting getSimplePromptResponse with response: {}", response);
        return response;
    }

    @SneakyThrows
    public ChatBookResponse getJsonResponse(ChatRequest chatRequest) {
        log.info("Entering getJsonResponse with input: {}", chatRequest.getInput());
        String userInput = chatRequest.getInput();
        String result = Objects.requireNonNull(Objects.requireNonNull(kernel.invokePromptAsync(createJsonPrompt(userInput)).block()).getResult()).toString();

        return getChatBookResponse(result);
    }

    public ChatBookResponse getJsonResponseWithSettings(ChatRequest chatRequest) {
        log.info("Entering getJsonResponseWithSettings with input: {}", chatRequest.getInput());
        String userInput = chatRequest.getInput();
        PromptExecutionSettings settings = createPromptExecutionSettings(chatRequest);

        FunctionResult<Object> response = kernel.invokePromptAsync(createJsonPrompt(userInput))
                .withPromptExecutionSettings(settings)
                .block();

        String result = response.getResult().toString();
        log.info("Received result: {}", result);

        log.info("Usage: prompt tokens={}, completion tokens={}, total tokens={}",
                response.getMetadata().getUsage().getPromptTokens(),
                response.getMetadata().getUsage().getCompletionTokens(),
                response.getMetadata().getUsage().getTotalTokens());

        return getChatBookResponse(result);
    }

    public ChatBookResponse getJsonResponseWithHistory(ChatRequest chatRequest) {
        log.info("User > {}", chatRequest.getInput());
        String userInput = chatRequest.getInput();
        PromptExecutionSettings settings = createPromptExecutionSettings(chatRequest);

        chatHistory.addUserMessage(createJsonPrompt(userInput));

        List<ChatMessageContent<?>> response = chatCompletionService
                .getChatMessageContentsAsync(
                        chatHistory,
                        kernel,
                        InvocationContext.builder()
                                .withPromptExecutionSettings(settings)
                                .build())
                .block();

        String result = response.get(response.size() - 1).getContent();
        chatHistory.addAssistantMessage(result);
        log.info("Assistant > {}", result);
        log.info("Messages in chat history: {}", chatHistory.getMessages().size());
        return getChatBookResponse(result);
    }

    private String createJsonPrompt(String input) {
        return """
                ## Instructions
                Provide the response using the following format:
                [
                    {
                        "author": "author1",
                        "title": "title1"
                    },
                    {
                        "author": "author2",
                        "title": "title2"
                    }
                ]
                ```
                ## User Input
                The user input is: "%s"
                }
                ```
                """.formatted(input);
    }

    private PromptExecutionSettings createPromptExecutionSettings(ChatRequest chatRequest) {
        return PromptExecutionSettings.builder()
                .withMaxTokens(chatRequest.getMaxTokens() != null ? chatRequest.getMaxTokens() : 256)
                .withTemperature(chatRequest.getTemperature() != null ? chatRequest.getTemperature() : 1.0)
                .build();
    }

    private ChatBookResponse getChatBookResponse(String result) {
        try {
            List<BookDto> jsonResult = mapJsonToBooks(result).getResponse();
            log.info("Mapped result: {}", jsonResult);
            ChatBookResponse chatBookResponse = mapJsonToBooks(result);
            log.info("Exiting getJsonResponseWithSettings with response: {}", chatBookResponse);
            return chatBookResponse;
        } catch (Exception e) {
            log.error("Error while mapping JSON to books", e);
            throw new OpenAiException("Error mapping JSON to books: " + e.getMessage() + ".\n\nOriginal result: " + result, e);
        }
    }

    private ChatBookResponse mapJsonToBooks(String json) throws IOException {
        List<BookDto> books = objectMapper.readValue(json, new TypeReference<>() {
        });
        return new ChatBookResponse(books);
    }
}
