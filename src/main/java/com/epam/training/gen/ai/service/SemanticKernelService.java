package com.epam.training.gen.ai.service;

import com.epam.training.gen.ai.dto.BookDto;
import com.epam.training.gen.ai.dto.ChatBookResponse;
import com.epam.training.gen.ai.dto.ChatKernelRequest;
import com.epam.training.gen.ai.dto.ChatResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.semantickernel.Kernel;
import lombok.RequiredArgsConstructor;
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

    public ChatResponse getKernelResponseUsingSimplePrompt(ChatKernelRequest chatKernelRequest) {
        String userInput = chatKernelRequest.getInput();
        String result = Objects.requireNonNull(Objects.requireNonNull(kernel.invokePromptAsync(userInput).block()).getResult()).toString();
        log.info("Received result: {}", result);
        return new ChatResponse(List.of(result));
    }

    public ChatBookResponse getKernelJsonResponse(ChatKernelRequest chatKernelRequest) {
        String userInput = chatKernelRequest.getInput();
        String result = Objects.requireNonNull(Objects.requireNonNull(kernel.invokePromptAsync(getJsonPrompt(userInput)).block()).getResult()).toString();

        log.info("Received result: {}", result);
        List<BookDto> jsonResult = mapJsonToBooks(result).getResponse();
        log.info("Mapped result: {}", jsonResult);
        return mapJsonToBooks(result);
    }

    private String getJsonPrompt(String input) {
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
                                }
                """.formatted(input);
    }

    private ChatBookResponse mapJsonToBooks(String jsonInput) {
        try {
            List<BookDto> books = objectMapper.readValue(jsonInput, new TypeReference<>() {
            });
            return new ChatBookResponse(books);
        } catch (IOException e) {
            log.error("Error while mapping books to json", e);
            return new ChatBookResponse();
        }
    }
}
