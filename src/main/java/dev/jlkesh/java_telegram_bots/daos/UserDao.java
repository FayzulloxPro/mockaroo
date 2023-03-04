package dev.jlkesh.java_telegram_bots.daos;

import dev.jlkesh.java_telegram_bots.domains.UserDomain;
import lombok.NonNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class UserDao extends Dao {

    public void save(@NonNull UserDomain domain) throws SQLException {
        Connection connection = getConnection();
        PreparedStatement pst = connection.prepareStatement("insert into users(chat_id,username, password, first_name) values(?,?,?,?);");
        pst.setString(1, domain.getChatID());
        pst.setString(2, domain.getUsername());
        pst.setString(3, domain.getPassword());
        pst.setString(4, domain.getFirstName());
        pst.execute();
    }

    public boolean isRegistered(String chatId) throws SQLException {

        Connection connection=getConnection();
        PreparedStatement pst = connection.prepareStatement("select * from users where chat_id=?");
        pst.setString(1, chatId);
        ResultSet resultSet = pst.executeQuery();
        return resultSet.next();
    }

    public UserDomain getUser(String chatId){
        Connection connection = getConnection();
        try (PreparedStatement ps = connection.prepareStatement("select * from users where chat_id=?")){
            ps.setString(1, chatId);

            ResultSet set = ps.executeQuery();

            UserDomain userDomain = new UserDomain();
            if (set.next()) {
                userDomain.setUsername(set.getString("username"));
                userDomain.setFirstName(set.getString("first_name"));
                userDomain.setLanguage(set.getString("language"));
            }
            return userDomain;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setLanguage(Long chatID, String lan) {

        Connection connection = getConnection();
        
        try (PreparedStatement ps = connection.prepareStatement("update users set language = ? where chat_id=?;")){
            ps.setString(1,lan);
            ps.setString(2, String.valueOf(chatID));
            ps.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
