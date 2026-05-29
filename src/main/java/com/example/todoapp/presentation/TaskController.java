package com.example.todoapp.presentation;

import com.example.todoapp.JsonUtils;
import com.example.todoapp.business.model.TasksModel;
import com.example.todoapp.business.service.TaskService;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.nonNull;

public class TaskController {

    private static final Pattern ID_PATH = Pattern.compile("^/tasks/([0-9]+)$");
    private static final TaskService service = new TaskService();

    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        //region Manage POST /tasks
        if ("POST".equals(method) && "/tasks".equals(path)) {
            TasksModel input = JsonUtils.deserialize(new String(exchange.getRequestBody().readAllBytes(), UTF_8), TasksModel.class);
            TasksModel createdTask = service.createTask(input);
            exchange.getResponseHeaders().add("Location", "/tasks/" + createdTask.id());
            sendResponse(exchange, 201, JsonUtils.serialize(createdTask));
            return;
        }
        //endregion

        //region Manage GET /tasks
        if ("GET".equals(method) && "/tasks".equals(path)) {
            String query = exchange.getRequestURI().getQuery();
            boolean todoOnly = nonNull(query) && query.contains("todo-only=true");
            var tasks = service.getTasks(todoOnly);
            if (tasks.isEmpty()) {
                sendResponse(exchange, 204, null);
            } else {
                sendResponse(exchange, 200, JsonUtils.serialize(tasks));
            }
            return;
        }
        //endregion

        //region Manage GET /tasks/{id}
        Matcher m = ID_PATH.matcher(path);
        if ("GET".equals(method) && m.matches()) {
            int id = Integer.parseInt(m.group(1));
            Optional<TasksModel> task = service.getTaskById(id);
            if (task.isPresent()) {
                sendResponse(exchange, 200, JsonUtils.serialize(task.get()));
            } else {
                sendResponse(exchange, 404, null);
            }
            return;
        }
        //endregion

        //region Manage DELETE /tasks/{id}
        m = ID_PATH.matcher(path);
        if ("DELETE".equals(method) && m.matches()) {
            int id = Integer.parseInt(m.group(1));
            boolean deleted = service.deleteTask(id);
            sendResponse(exchange, deleted ? 204 : 404, null);
            return;
        }
        //endregion

        //region Manage PUT /tasks/{id}
        m = ID_PATH.matcher(path);
        if ("PUT".equals(method) && m.matches()) {
            int id = Integer.parseInt(m.group(1));
            TasksModel input = JsonUtils.deserialize(new String(exchange.getRequestBody().readAllBytes(), UTF_8), TasksModel.class);
            boolean updated = service.updateTask(id, input);
            sendResponse(exchange, updated ? 204 : 404, null);
            return;
        }
        //endregion

        // Otherwise → 404
        sendResponse(exchange, 404, null);
    }

    private void sendResponse(HttpExchange exchange, int status, String json) throws IOException {
        if (nonNull(json)) {
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            byte[] bytes = json.getBytes(UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } else {
            exchange.sendResponseHeaders(status, 0);
            exchange.close();
        }
    }
}