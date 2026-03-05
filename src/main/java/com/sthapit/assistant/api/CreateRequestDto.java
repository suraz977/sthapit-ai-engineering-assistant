package com.sthapit.assistant.api;

import jakarta.validation.constraints.NotBlank;

public record CreateRequestDto(@NotBlank String text) {}