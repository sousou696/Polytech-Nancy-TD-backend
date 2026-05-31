package com.example.todoapp.dto;

public record ErrorDto(
        String field,
        String message
) {}