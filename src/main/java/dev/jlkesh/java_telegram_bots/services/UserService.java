package dev.jlkesh.java_telegram_bots.services;

import dev.jlkesh.java_telegram_bots.daos.UserDao;
import dev.jlkesh.java_telegram_bots.domains.UserDomain;
import dev.jlkesh.java_telegram_bots.utils.PasswordEncoderUtils;
import lombok.NonNull;

import java.sql.SQLException;

public class UserService {

    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public void create(@NonNull UserDomain domain) {
        domain.setPassword(PasswordEncoderUtils.encode(domain.getPassword()));
        try {
            userDao.save(domain);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isRegistered(@NonNull String chatId){
        try{
            return userDao.isRegistered(chatId);
        }catch (SQLException e){
            throw new RuntimeException();
        }
    }
}
