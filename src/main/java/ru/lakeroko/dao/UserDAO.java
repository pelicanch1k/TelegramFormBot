package ru.lakeroko.dao;

import ru.lakeroko.model.User;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface UserDAO {
    Optional<User> findById(Integer id);
    Optional<User> findByUserId(BigInteger userId);
    List<User> findAll();
    User create(User user);
    void update(User user);
    void delete(User user);
}