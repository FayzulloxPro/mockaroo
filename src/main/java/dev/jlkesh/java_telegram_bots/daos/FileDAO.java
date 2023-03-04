package dev.jlkesh.java_telegram_bots.daos;


import dev.jlkesh.java_telegram_bots.domains.FIleDomain;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FileDAO extends Dao {
    private static FileDAO instance;

    private static final String SELECT_FILES = "select chat_id, file_id, created_at, file_name, rows_count from files where chat_id=? order by created_at desc offset ? limit ?;";
    private static final String INSERT = "insert into files(chat_id, file_id, file_name, rows_count) values(?,?,?,?);";

    private FileDAO() {
    }

    public static FileDAO getInstance() {
        if (Objects.isNull(instance)) {
            synchronized (FileDAO.class) {
                if (Objects.isNull(instance)) {
                    instance = new FileDAO();
                }
            }
        }
        return instance;
    }

    public List<FIleDomain> getFiles(int page, int count, String chatId) {
        Connection connection = getConnection();
        List<FIleDomain> domainList = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(SELECT_FILES)) {
            ps.setString(1, chatId);
            ps.setInt(2, (page - 1) * count);
            ps.setInt(3, count);

            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {
                FIleDomain fIleDomain = FIleDomain.builder()
                        .chatId(resultSet.getString("chat_id"))
                        .fileId(resultSet.getString("file_id"))
                        .createdAt(resultSet.getTimestamp("created_at").toLocalDateTime())
                        .fileName(resultSet.getString("file_name"))
                        .rowsCount(resultSet.getInt("rows_count"))
                        .build();
                domainList.add(fIleDomain);
            }
            return domainList;
        } catch (SQLException e) {
            // TODO: 2/13/2023 I need to write log here
            return new ArrayList<>();
        }
    }

    public FIleDomain getFile(String fileId) {
        Connection connection = getConnection();
        FIleDomain file = null;
        try (PreparedStatement ps = connection.prepareStatement("select * from files where file_id=? and is_deleted=false")) {
            ps.setString(1, fileId);
            ResultSet set = ps.executeQuery();
            if (set.next()) {
                file = FIleDomain.builder()
                        .chatId(set.getString("chat_id"))
                        .fileId(set.getString("file_id"))
                        .fileName(set.getString("file_name"))
                        .createdAt(set.getTimestamp("created_at").toLocalDateTime())
                        .rowsCount(set.getInt("rows_count"))
                        .build();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return file;
    }

    public void save(FIleDomain domain) {
        Connection connection = getConnection();
        try (PreparedStatement ps = connection.prepareStatement(INSERT)) {
            ps.setString(1, domain.getChatId());
            ps.setString(2, domain.getFileId());
            ps.setString(3, domain.getFileName());
            ps.setInt(4, domain.getRowsCount());
            ps.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
