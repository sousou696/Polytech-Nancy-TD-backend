package com.example.todoapp.business.model;

public record TasksModel(
        int id,
        String title,
        String description,
        boolean done)
{}
