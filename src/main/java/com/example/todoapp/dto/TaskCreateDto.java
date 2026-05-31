package com.example.todoapp.dto;

public record TaskCreateDto(
        String title,
        String description
) {}