package com.example.todoapp.dao;

import com.example.todoapp.business.model.TasksModel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

    public class SQLiteDao {

        private static final String URL = "jdbc:sqlite:tasks.db";

        public SQLiteDao() {
            try (Connection conn = DriverManager.getConnection(URL);
                 Statement stmt = conn.createStatement()) {
                stmt.execute("""
                CREATE TABLE IF NOT EXISTS tasks (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    description TEXT,
                    done INTEGER NOT NULL DEFAULT 0
                )
            """);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        public TasksModel save(TasksModel task) {
            String sql = "INSERT INTO tasks (title, description, done) VALUES (?, ?, ?)";
            try (Connection conn = DriverManager.getConnection(URL);
                 PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, task.title());
                stmt.setString(2, task.description());
                stmt.setInt(3, task.done() ? 1 : 0);
                stmt.executeUpdate();
                ResultSet keys = stmt.getGeneratedKeys();
                int id = keys.next() ? keys.getInt(1) : task.id();
                return new TasksModel(id, task.title(), task.description(), task.done());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        public Optional<TasksModel> findById(int id) {
            String sql = "SELECT * FROM tasks WHERE id = ?";
            try (Connection conn = DriverManager.getConnection(URL);
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return Optional.of(new TasksModel(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getInt("done") == 1
                    ));
                }
                return Optional.empty();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        public List<TasksModel> findAll() {
            String sql = "SELECT * FROM tasks";
            return executeFindQuery(sql);
        }

        public List<TasksModel> findAllTodoOnly() {
            String sql = "SELECT * FROM tasks WHERE done = 0";
            return executeFindQuery(sql);
        }

        public boolean deleteById(int id) {
            String sql = "DELETE FROM tasks WHERE id = ?";
            try (Connection conn = DriverManager.getConnection(URL);
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        public boolean update(int id, TasksModel task) {
            String sql = "UPDATE tasks SET title = ?, description = ?, done = ? WHERE id = ?";
            try (Connection conn = DriverManager.getConnection(URL);
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, task.title());
                stmt.setString(2, task.description());
                stmt.setInt(3, task.done() ? 1 : 0);
                stmt.setInt(4, id);
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        private List<TasksModel> executeFindQuery(String sql) {
            List<TasksModel> tasks = new ArrayList<>();
            try (Connection conn = DriverManager.getConnection(URL);
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    tasks.add(new TasksModel(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getInt("done") == 1
                    ));
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return tasks;
        }
    }

