package ru.lakeroko.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import ru.lakeroko.model.User;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public class UserDaoImpl implements UserDAO {

    private final SessionFactory sessionFactory;

    public UserDaoImpl() {
        Configuration configuration = new Configuration().addAnnotatedClass(User.class);
        this.sessionFactory = configuration.buildSessionFactory();
    }

    @Override
    public Optional<User> findById(Integer id) {
        Session session = sessionFactory.getCurrentSession();
        User user = null;

        try {
            session.beginTransaction();
            user = session.get(User.class, id);
            session.getTransaction().commit();
        } catch (Exception e) {
            if (session.getTransaction() != null) {
                session.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            session.close();
        }

        return Optional.ofNullable(user);
    }

    @Override
    public Optional<User> findByUserId(BigInteger userId) {
        Session session = sessionFactory.getCurrentSession();
        User user = null;

        try {
            session.beginTransaction();
            user = session.createQuery("from User where userId = :userId", User.class)
                    .setParameter("userId", userId)
                    .uniqueResult();
            session.getTransaction().commit();
        } catch (Exception e) {
            if (session.getTransaction() != null) {
                session.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            session.close();
        }

        return Optional.ofNullable(user);
    }

    @Override
    public List<User> findAll() {
        Session session = sessionFactory.getCurrentSession();
        List<User> users = null;

        try {
            session.beginTransaction();
            users = session.createQuery("from User", User.class).list();
            session.getTransaction().commit();
        } catch (Exception e) {
            if (session.getTransaction() != null) {
                session.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            session.close();
        }

        return users;
    }

    @Override
    public User create(User user) {
        Session session = sessionFactory.getCurrentSession();

        try {
            session.beginTransaction();
            session.save(user);
            session.getTransaction().commit();
        } catch (Exception e) {
            if (session.getTransaction() != null) {
                session.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            session.close();
        }

        return user;
    }

    @Override
    public void update(User user) {
        Session session = sessionFactory.getCurrentSession();

        try {
            session.beginTransaction();
            session.update(user);
            session.getTransaction().commit();
        } catch (Exception e) {
            if (session.getTransaction() != null) {
                session.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    @Override
    public void delete(User user) {
        Session session = sessionFactory.getCurrentSession();

        try {
            session.beginTransaction();
            User newUser = session.get(User.class, user.getId());
            if (newUser != null) {
                session.delete(user);
            }
            session.getTransaction().commit();
        } catch (Exception e) {
            if (session.getTransaction() != null) {
                session.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            session.close();
        }
    }
}