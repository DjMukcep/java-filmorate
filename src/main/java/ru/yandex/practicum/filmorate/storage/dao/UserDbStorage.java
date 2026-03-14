package ru.yandex.practicum.filmorate.storage.dao;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.user.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.dao.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.storage.dao.query.QueryHandler;

import java.util.List;
import java.util.Optional;

import static ru.yandex.practicum.filmorate.storage.dao.query.Query.*;

@Repository
@Qualifier("DB")
public class UserDbStorage extends BaseDbStorage<User> implements UserStorage {

    public UserDbStorage(JdbcTemplate jdbc, UserRowMapper mapper, QueryHandler queryHandler) {
        super(jdbc, mapper, queryHandler);
    }

    @Override
    public User addUser(User user) {
        long id = insert(queryHandler.get(ADD_USER),
                user.getName(),
                user.getEmail(),
                user.getLogin(),
                user.getBirthday()
        );
        user.setId(id);
        return user;
    }

    @Override
    public User updateUser(User user) {
        update(queryHandler.get(UPDATE_USER),
                user.getName(),
                user.getEmail(),
                user.getLogin(),
                user.getBirthday(),
                user.getId()
        );
        return user;
    }

    @Override
    public void setFriendStatus(User user, Long friendId, FriendshipStatus status) {
        update(queryHandler.get(ADD_FRIEND_STATUS),
                user.getId(),
                friendId,
                status.getId()
        );
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        jdbc.update(queryHandler.get(REMOVE_USER_FRIEND),
                userId,
                friendId
        );
    }

    @Override
    public List<User> getUsers() {
        return findMany(queryHandler.get(FIND_ALL_USERS));
    }

    @Override
    public List<User> getFriends(User user) {
        return findMany(queryHandler.get(FIND_USER_FRIENDS),user.getId());
    }

    @Override
    public Optional<User> getUserById(Long userId) {
        return findOne(queryHandler.get(FIND_USER),userId);
    }
}
