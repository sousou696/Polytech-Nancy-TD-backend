package com.example.todoapp.business.service;
import com.example.todoapp.business.model.Task;
import com.example.todoapp.dao.TaskDao;
import java.util.List;
import java.util.Optional;

    public class TaskService {
        private final TaskDao dao;

        public TaskService(TaskDao dao) { this.dao = dao; }

        public Task create(Task task) throws Exception {
            // Règle du TD : done est toujours false à la création
            task.setDone(false);
            return dao.save(task);
        }

        public List<Task> getAll(boolean todoOnly) throws Exception {
            return todoOnly ? dao.findAllTodoOnly() : dao.findAll();
        }

        // ... ajoutez findById, update, delete
    }
}
