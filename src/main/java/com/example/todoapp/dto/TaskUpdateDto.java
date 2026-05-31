package com.example.todoapp.dto;

public record TaskUpdateDto(
        String title,
        String description,
        boolean done
) {}