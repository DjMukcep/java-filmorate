package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.user.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserStorage {

    void saveUser(User user);

    void addFriend(Long userId, Long friendId);

    void removeFriend(Long userId, Long friendId);

    List<User> getUsers();

    List<User> getFriends(User user);

    Optional<User> getUserById(Long filmId);

    List<Long> findExistentIds(Set<Long> ids);
}
