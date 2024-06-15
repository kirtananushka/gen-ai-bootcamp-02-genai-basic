package com.epam.training.gen.ai.controller;

import com.epam.training.gen.ai.dto.ChatBookResponse;
import com.epam.training.gen.ai.dto.ChatKernelRequest;
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
    public ResponseEntity<ChatResponse> getSimplePromptKernelResponse(@RequestBody ChatKernelRequest chatKernelRequest) {
        ChatResponse response = semanticKernelService.getKernelResponseUsingSimplePrompt(chatKernelRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/kernel/json")
    public ResponseEntity<ChatBookResponse> getJsonPromptKernelResponse(@RequestBody ChatKernelRequest chatKernelRequest) {
        ChatBookResponse response = semanticKernelService.getKernelJsonResponse(chatKernelRequest);
        return ResponseEntity.ok(response);
    }
}
