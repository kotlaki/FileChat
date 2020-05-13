package serverapp;

import java.sql.*;

public class SqlWorker {
    private static Connection connection;
    private static Statement stmt;

    // подключаемся к БД с исользованием драйвера jdbc sqllite
    public static void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:workDB.db");
            stmt = connection.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getNickByLoginAndPass(String login, String pass) {
        try {
            ResultSet rs = stmt.executeQuery("SELECT nickname FROM user WHERE login = '" + login + "' and password = '" + pass +"'");
            int myHash = pass.hashCode();
            if (rs.next()) {
                String nick = rs.getString(1);
                return nick;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String addUser(String login, String pass, String nick) {
        try {
            String query = "INSERT INTO user (login, password, nickname) VALUES (?, ?, ?);";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, login);
            ps.setInt(2, Integer.parseInt(pass));
            ps.setString(3, nick);
            ps.executeUpdate();
            return "Пользователь добавлен!!!";
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // прверяеместь ли совпадения в БД по 2 пользователям. если есть возвращаем true
    public static boolean checkAddBL(String nick, String nickFrom) {
        try {
            ResultSet rs = stmt.executeQuery("SELECT bl_nick, bl_nick_from FROM BlackList WHERE bl_nick = '"
                    + nick + "' and bl_nick_from = '" + nickFrom + "'");
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Ошибка проверки наличия пары пользователей для добавления в ЧС!!!");
        }
        return false;
    }


    public static void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
