package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {

    User addUser(User user);

    User updateUser(User user);

    void setFriendStatus(User user, Long friendId, FriendshipStatus status);

    void removeFriend(Long userId, Long friendId);

    List<User> getUsers();

    List<User> getFriends(User user);

    Optional<User> getUserById(Long filmId);
}
