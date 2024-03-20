package io.hexlet;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Application {

    public static void main(final String[] args) throws SQLException {

        var conn = DriverManager.getConnection("jdbc:h2:mem:hexlet_test");

        var sql = "CREATE TABLE users (id BIGINT PRIMARY KEY AUTO_INCREMENT, username VARCHAR(255), phone VARCHAR(255))";
        try (var statement = conn.createStatement()) {
            statement.execute(sql);
        }

        var sql2 = "INSERT INTO users (username, phone) VALUES ('tommy', '123456789')";
        try (var statement2 = conn.createStatement()) {
            statement2.executeUpdate(sql2);
        }

        var sql4 = "INSERT INTO users (username, phone) VALUES (?, ?)";
        try (var preparedStatement = conn.prepareStatement(sql4)) {
            preparedStatement.setString(1, "Tommy");
            preparedStatement.setString(2, "33333333");
            preparedStatement.executeUpdate();

            preparedStatement.setString(1, "Maria");
            preparedStatement.setString(2, "44444444");
            preparedStatement.executeUpdate();
        }

        var sql5 = "INSERT INTO users (username, phone) VALUES (?, ?)";
        try (var preparedStatement = conn.prepareStatement(sql5, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, "Sarah");
            preparedStatement.setString(2, "333333333");
            preparedStatement.executeUpdate();
            // Если ключ составной, значений может быть несколько
            // В нашем случае, ключ всего один
            var generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                System.out.println(generatedKeys.getLong(1));
            } else {
                throw new SQLException("DB have not returned an id after saving the entity");
            }
        }

        var sql3 = "SELECT username, phone FROM users";
        try (var statement3 = conn.createStatement()) {
            var resultSet = statement3.executeQuery(sql3);
            while (resultSet.next()) {
                System.out.format("%s %s\n",
                        resultSet.getString("username"),
                        resultSet.getString("phone"));
            }
        }

        var sql6 = "DELETE FROM users WHERE username = ?";
        try (var preparedStatement = conn.prepareStatement(sql6)) {
            preparedStatement.setString(1, "Sarah");
            preparedStatement.executeUpdate();
        }

        conn.close();

        System.out.println("====");

        Connection conn2 = DriverManager.getConnection("jdbc:h2:mem:hexlet_test2");
        UserDAO dao = new UserDAO(conn2);

        User user = new User("Maria", "888888888");
        System.out.println(user.getId()); // null
        dao.save(user);
        System.out.println(user.getId()); // Здесь уже выводится какой-то id

        // Возвращается Optional<User>
        User user2 = dao.find(user.getId()).get();
        System.out.println(user2.getId() == user.getId()); // true

        System.out.println("+++");
        dao.show();
        dao.delete(1L);

        System.out.println("DDD+++");
        dao.show();
        conn2.close();
    }
}