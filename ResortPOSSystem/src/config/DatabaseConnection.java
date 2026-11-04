package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String HOST     = "localhost";
    private static final String PORT     = "3306";
    private static final String DATABASE = "resort_pos";
    private static final String USER     = "root";
    private static final String PASSWORD = "";

    private static final String URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE
            + "?sslMode=DISABLED&allowPublicKeyRetrieval=true&serverTimezone=Asia/Manila";

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Hindi mahanap ang MySQL Connector/J. "
                    + "Pakisigurong naka-add ang JAR sa Libraries.", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}