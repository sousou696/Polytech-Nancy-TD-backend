package com.example.todoapp.dto;

public record TaskResponseDto(
        int id,
        String title,
        String description,
        boolean done
) {}