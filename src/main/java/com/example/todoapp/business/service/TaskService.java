package com.example.todoapp.business.service;

import com.example.todoapp.business.model.TasksModel;
import com.example.todoapp.dao.SQLiteDao;

import java.util.List;
import java.util.Optional;

public class TaskService {

    private final SQLiteDao dao = new SQLiteDao();

    public List<TasksModel> getTasks(boolean todoOnly) {
        return todoOnly ? dao.findAllTodoOnly() : dao.findAll();
    }

    public Optional<TasksModel> getTaskById(int id) {
        return dao.findById(id);
    }

    public TasksModel createTask(TasksModel task) {
        return dao.save(task);
    }

    public boolean deleteTask(int id) {
        return dao.deleteById(id);
    }

    public boolean updateTask(int id, TasksModel task) {
        return dao.update(id, task);
    }
}