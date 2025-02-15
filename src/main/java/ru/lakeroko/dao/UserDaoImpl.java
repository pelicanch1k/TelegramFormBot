package ru.lakeroko.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import ru.lakeroko.model.User;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public class UserDaoImpl {

    public static Optional<User> findById(Integer id) {
        Configuration configuration = new Configuration().addAnnotatedClass(User.class);

        SessionFactory sessionFactory = configuration.buildSessionFactory();
        Session session = sessionFactory.getCurrentSession();

        User user = null;

        try {
            session.beginTransaction();

            user = session.get(User.class, id);

            session.getTransaction().commit();

        } finally {
            sessionFactory.close();
        }

        return Optional.of(user);
    }

    public static Optional<User> findByUserId(BigInteger userId) {
        Configuration configuration = new Configuration().addAnnotatedClass(User.class);

        SessionFactory sessionFactory = configuration.buildSessionFactory();
        Session session = sessionFactory.getCurrentSession();

        User user = null;

        try {
            session.beginTransaction();

            // Создаем HQL-запрос для поиска пользователя по user_id
            user = session.createQuery("from User where userId = :userId", User.class)
                    .setParameter("userId", userId)
                    .uniqueResult();

            session.getTransaction().commit();

        } finally {
            sessionFactory.close();
        }

        return Optional.ofNullable(user);
    }


    public static List<User> findAll() {
        Configuration configuration = new Configuration().addAnnotatedClass(User.class);

        SessionFactory sessionFactory = configuration.buildSessionFactory();
        Session session = sessionFactory.getCurrentSession();


        List<User> users = null;

        try {
            session.beginTransaction();

            users = session.createQuery("from User", User.class).list();

            session.getTransaction().commit();

        } finally {
            sessionFactory.close();
        }

        return users;
    }


    public static User save(User user) {
        Configuration configuration = new Configuration().addAnnotatedClass(User.class);

        SessionFactory sessionFactory = configuration.buildSessionFactory();
        Session session = sessionFactory.getCurrentSession();


        try {
            session.beginTransaction();

            session.save(user);

            session.getTransaction().commit();
        } finally {
            session.close();
        }

        return user;
    }

    public static void update(User user) {
        Configuration configuration = new Configuration().addAnnotatedClass(User.class);

        SessionFactory sessionFactory = configuration.buildSessionFactory();
        Session session = sessionFactory.getCurrentSession();

        try {
            session.beginTransaction();

            session.update(user);

            session.getTransaction().commit();
        } finally {
            sessionFactory.close();
        }
    }

    public static void delete(User user) {
        Configuration configuration = new Configuration().addAnnotatedClass(User.class);

        SessionFactory sessionFactory = configuration.buildSessionFactory();
        Session session = sessionFactory.getCurrentSession();

        try {
            session.beginTransaction();

            User newUser = session.get(User.class, user.getId());
            if (newUser != null) {
                session.delete(user);
            }

            session.getTransaction().commit();
        } finally {
            sessionFactory.close();
        }
    }
}