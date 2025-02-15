package ru.lakeroko.dao;

import ru.lakeroko.model.User;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface UserDAO {
    public static Optional<User> findById(Integer id) {
        return Optional.empty();
    }

    public static Optional<User> findByUserId(BigInteger userId) {
        return Optional.empty();
    }

    public static List<User> findAll() {
        return null;
    }

    public static User create(User user) {
        return null;
    }

    public static void update(User user) {}

    public static void delete(User user) {}
}
