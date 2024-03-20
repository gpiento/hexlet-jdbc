package io.hexlet;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class UserDAO {
    private Connection connection;

    public UserDAO(Connection conn) throws SQLException {
        connection = conn;

        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet tables = metaData.getTables(null, null, "users", new String[]{"TABLE"});

        if (!tables.next()) {
            var sql = "CREATE TABLE users (id BIGINT PRIMARY KEY AUTO_INCREMENT, username VARCHAR(255), phone VARCHAR(255))";
            try (var statement = connection.createStatement()) {
                statement.execute(sql);
            }
        }
    }

    public void save(User user) throws SQLException {
        // Если пользователь новый, выполняем вставку
        // Иначе обновляем
        if (user.getId() == null) {
            String sql = "INSERT INTO users (username, phone) VALUES (?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, user.getName());
                preparedStatement.setString(2, user.getPhone());
                preparedStatement.executeUpdate();
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                // Если идентификатор сгенерирован, извлекаем его и добавляем в сохраненный объект
                if (generatedKeys.next()) {
                    // Обязательно устанавливаем id в сохраненный объект
                    user.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("DB have not returned an id after saving an entity");
                }
            }
        } else {
            var sql = "UPDATE users SET username = ?, phone = ? WHERE id = " + user.getId();
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, user.getPhone());
                preparedStatement.setString(2, user.getName());
                preparedStatement.executeUpdate();
                var generatedKeys = preparedStatement.getGeneratedKeys();
                // Если идентификатор сгенерирован, извлекаем его и добавляем в сохраненный объект
                if (generatedKeys.next()) {
                    // Обязательно устанавливаем id в сохраненный объект
                    user.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("DB have not returned an id after saving an entity");
                }
            }
        }
    }

    // Возвращается Optional<User>
    // Это упрощает обработку ситуаций, когда в базе ничего не найдено
    public Optional<User> find(Long id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                String username = resultSet.getString("username");
                String phone = resultSet.getString("phone");
                User user = new User(username, phone);
                user.setId(id);
                return Optional.of(user);
            }
            return Optional.empty();
        }
    }

    public void delete(Long id) throws SQLException {
        var sql = "DELETE FROM users WHERE id = " + id;
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
        }
    }

    public void show() throws SQLException {
        var sql3 = "SELECT * FROM users";
        try (var statement3 = connection.createStatement()) {
            var resultSet = statement3.executeQuery(sql3);
            while (resultSet.next()) {
                System.out.format("%s %s %s\n",
                        resultSet.getString("id"),
                        resultSet.getString("username"),
                        resultSet.getString("phone"));
            }
        }
    }
}
