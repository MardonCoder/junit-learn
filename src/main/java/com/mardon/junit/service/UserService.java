package com.mardon.junit.service;

import com.mardon.junit.dao.UserDao;
import com.mardon.junit.dto.User;

import java.util.*;

import static java.util.Arrays.asList;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

public class UserService {

    private final List<User> users = new ArrayList<>();
    private final UserDao dao;

    public boolean delete(Integer userId){
        return dao.delete(userId);
    }

    public UserService(UserDao dao) {
        this.dao = dao;
    }

    public List<User> getAll() {
        return users;
    }

    public void add(User... users) {
        this.users.addAll(asList(users));
    }

    public Optional<User> login(String username, String password) {
        if (username == null || password == null) throw new IllegalArgumentException("username or password is null");
        return users.stream().filter(user -> user.getUsername().equals(username))
                 .filter(user -> user.getPassword().equals(password)).findFirst();
    }

    public Map<Integer, User> getAllConvertedById() {
        return users.stream().collect(toMap(User::getId, identity()));
    }
}
