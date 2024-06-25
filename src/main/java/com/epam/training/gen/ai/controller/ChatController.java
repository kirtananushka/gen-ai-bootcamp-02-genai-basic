package com.epam.training.gen.ai.controller;

import com.epam.training.gen.ai.dto.ChatBookResponse;
import com.epam.training.gen.ai.dto.ChatRequest;
import com.epam.training.gen.ai.dto.ChatResponse;
import com.epam.training.gen.ai.service.SemanticKernelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final SemanticKernelService semanticKernelService;

    @PostMapping("/kernel/simple")
    public ResponseEntity<ChatResponse> getSimplePromptResponse(@RequestBody ChatRequest chatRequest) {
        ChatResponse response = semanticKernelService.getSimplePromptResponse(chatRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/kernel/json")
    public ResponseEntity<ChatBookResponse> getJsonResponse(@RequestBody ChatRequest chatRequest) {
        ChatBookResponse response = semanticKernelService.getJsonResponse(chatRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/kernel/json-with-settings")
    public ResponseEntity<ChatBookResponse> getJsonResponseWithSettings(@RequestBody ChatRequest chatRequest) {
        ChatBookResponse response = semanticKernelService.getJsonResponseWithSettings(chatRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/kernel/json-with-history")
    public ResponseEntity<ChatBookResponse> getJsonResponseWithHistory(@RequestBody ChatRequest chatRequest) {
        ChatBookResponse response = semanticKernelService.getJsonResponseWithHistory(chatRequest);
        return ResponseEntity.ok(response);
    }
}
