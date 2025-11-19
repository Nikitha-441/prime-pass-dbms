package com.primepass.service;

import com.primepass.dao.UserDao;
import com.primepass.model.User;
import com.primepass.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {
    @Autowired
    private UserDao userDao;

    @Autowired
    private JwtUtil jwtUtil;

    public Map<String, Object> login(String email, String password) {
        Map<String, Object> response = new HashMap<>();
        
        User user = userDao.findByEmail(email);
        if (user == null || !user.getPassword().equals(password)) {
            response.put("success", false);
            response.put("message", "Invalid email or password");
            return response;
        }

        String token = jwtUtil.generateToken(user.getUserId(), user.getEmail(), user.getRole());
        
        response.put("success", true);
        response.put("token", token);
        response.put("user", Map.of(
            "userId", user.getUserId(),
            "name", user.getName(),
            "email", user.getEmail(),
            "role", user.getRole()
        ));
        
        return response;
    }

    public Map<String, Object> signup(String name, String email, String password) {
        Map<String, Object> response = new HashMap<>();
        
        // Check if user already exists
        if (userDao.findByEmail(email) != null) {
            response.put("success", false);
            response.put("message", "Email already registered");
            return response;
        }

        User user = userDao.createUser(name, email, password);
        if (user == null) {
            response.put("success", false);
            response.put("message", "Registration failed");
            return response;
        }

        String token = jwtUtil.generateToken(user.getUserId(), user.getEmail(), user.getRole());
        
        response.put("success", true);
        response.put("token", token);
        response.put("user", Map.of(
            "userId", user.getUserId(),
            "name", user.getName(),
            "email", user.getEmail(),
            "role", user.getRole()
        ));
        
        return response;
    }
}

