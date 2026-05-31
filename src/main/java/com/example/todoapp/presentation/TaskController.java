package com.example.todoapp.presentation;

import com.example.todoapp.JsonUtils;
import com.example.todoapp.business.model.TasksModel;
import com.example.todoapp.dto.ErrorDto;
import com.example.todoapp.dto.TaskCreateDto;
import com.example.todoapp.dto.TaskResponseDto;
import com.example.todoapp.dto.TaskUpdateDto;
import com.example.todoapp.business.service.TaskService;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.nonNull;

public class TaskController {

    private static final Pattern ID_PATH = Pattern.compile("^/tasks/([0-9]+)$");
    private static final TaskService service = new TaskService();

    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            //region Manage POST /tasks
            if ("POST".equals(method) && "/tasks".equals(path)) {
                TaskCreateDto input = JsonUtils.deserialize(new String(exchange.getRequestBody().readAllBytes(), UTF_8), TaskCreateDto.class);
                if (input.title() == null || input.title().length() > 50) {
                    sendResponse(exchange, 400, JsonUtils.serialize(new ErrorDto("title", "Le titre est obligatoire et doit faire maximum 50 caractères")));
                    return;
                }
                if (input.description() != null && input.description().length() > 255) {
                    sendResponse(exchange, 400, JsonUtils.serialize(new ErrorDto("description", "La description doit faire maximum 255 caractères")));
                    return;
                }
                TasksModel created = service.createTask(new TasksModel(0, input.title(), input.description(), false));
                exchange.getResponseHeaders().add("Location", "/tasks/" + created.id());
                sendResponse(exchange, 201, JsonUtils.serialize(new TaskResponseDto(created.id(), created.title(), created.description(), created.done())));
                return;
            }
            //endregion

            //region Manage GET /tasks
            if ("GET".equals(method) && "/tasks".equals(path)) {
                String query = exchange.getRequestURI().getQuery();
                boolean todoOnly = nonNull(query) && query.contains("todo-only=true");
                List<TasksModel> tasks = service.getTasks(todoOnly);
                if (tasks.isEmpty()) {
                    sendResponse(exchange, 204, null);
                } else {
                    List<TaskResponseDto> response = tasks.stream()
                            .map(t -> new TaskResponseDto(t.id(), t.title(), t.description(), t.done()))
                            .toList();
                    sendResponse(exchange, 200, JsonUtils.serialize(response));
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
                    TasksModel t = task.get();
                    sendResponse(exchange, 200, JsonUtils.serialize(new TaskResponseDto(t.id(), t.title(), t.description(), t.done())));
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
                TaskUpdateDto input = JsonUtils.deserialize(new String(exchange.getRequestBody().readAllBytes(), UTF_8), TaskUpdateDto.class);
                if (input.title() == null || input.title().length() > 50) {
                    sendResponse(exchange, 400, JsonUtils.serialize(new ErrorDto("title", "Le titre est obligatoire et doit faire maximum 50 caractères")));
                    return;
                }
                if (input.description() != null && input.description().length() > 255) {
                    sendResponse(exchange, 400, JsonUtils.serialize(new ErrorDto("description", "La description doit faire maximum 255 caractères")));
                    return;
                }
                boolean updated = service.updateTask(id, new TasksModel(id, input.title(), input.description(), input.done()));
                sendResponse(exchange, updated ? 204 : 404, null);
                return;
            }
            //endregion

            // Otherwise → 404
            sendResponse(exchange, 404, null);

        } catch (Exception e) {
            sendResponse(exchange, 500, JsonUtils.serialize(new ErrorDto("error", "Erreur interne du serveur")));
        }
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