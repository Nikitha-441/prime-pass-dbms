package com.primepass.dao;

import com.primepass.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class UserDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<User> userRowMapper = new RowMapper<User>() {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setUserId(rs.getInt("user_id"));
            user.setName(rs.getString("name"));
            user.setEmail(rs.getString("email"));
            user.setPassword(rs.getString("password"));
            user.setRole(rs.getString("role"));
            return user;
        }
    };

    public User findByEmail(String email) {
        String sql = "SELECT * FROM Users WHERE email = ?";
        try {
            return jdbcTemplate.queryForObject(sql, userRowMapper, email);
        } catch (Exception e) {
            return null;
        }
    }

    public User findById(Integer userId) {
        String sql = "SELECT * FROM Users WHERE user_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, userRowMapper, userId);
        } catch (Exception e) {
            return null;
        }
    }

    public User createUser(String name, String email, String password) {
        String sql = "INSERT INTO Users (name, email, password, role) VALUES (?, ?, ?, 'user') RETURNING *";
        try {
            return jdbcTemplate.queryForObject(sql, userRowMapper, name, email, password);
        } catch (Exception e) {
            return null;
        }
    }
}

