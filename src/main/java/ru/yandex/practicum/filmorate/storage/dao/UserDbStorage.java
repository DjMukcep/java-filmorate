package ru.yandex.practicum.filmorate.storage.dao;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.dao.mapper.UserRowMapper;

import java.util.List;
import java.util.Optional;

@Repository
@Qualifier("DB")
public class UserDbStorage extends BaseDbStorage<User> implements UserStorage {

    public UserDbStorage(JdbcTemplate jdbc, UserRowMapper mapper) {
        super(jdbc, mapper);
    }

    @Override
    public User addUser(User user) {
        String sql = """
                INSERT INTO users(user_name, email, login, birthday)
                VALUES (?,?,?,?)
                """;

        long id = insert(sql, user.getName(), user.getEmail(), user.getLogin(), user.getBirthday());
        user.setId(id);
        return user;
    }

    @Override
    public User updateUser(User user) {
        String sql = """
                UPDATE users SET user_name=?, email=?, login=?, birthday=?
                WHERE user_id=?
                """;

        update(sql, user.getName(), user.getEmail(), user.getLogin(), user.getBirthday(), user.getId());
        return user;
    }

    @Override
    public void setFriendStatus(User user, Long friendId, FriendshipStatus status) {
        String sql = """
                MERGE INTO friendship (user_id, friend_id, status_id)
                KEY(user_id, friend_id) VALUES (?, ?, ?)
                """;

        update(sql, user.getId(), friendId, status.getId());
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        String sql = """
                DELETE FROM friendship
                WHERE user_id=? AND friend_id=?
                """;

        jdbc.update(sql, userId, friendId);
    }

    @Override
    public List<User> getUsers() {
        String sql = "SELECT * FROM users";
        return findMany(sql);
    }

    @Override
    public List<User> getFriends(User user) {
        String sql = """
                SELECT u.* FROM users u
                JOIN friendship f ON u.user_id = f.friend_id
                WHERE f.user_id=?
                """;

        return findMany(sql, user.getId());
    }

    @Override
    public Optional<User> getUserById(Long userId) {
        String sql = """
                SELECT * FROM users
                WHERE user_id=?
                """;

        return findOne(sql, userId);
    }
}
