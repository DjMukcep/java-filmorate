package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import static ru.yandex.practicum.filmorate.model.FriendshipStatus.*;

import java.util.*;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;

    public UserService(@Qualifier("DB") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    public User getUserById(Long id) {
        return findUserOrThrow(id);
    }

    public User addUser(User user) {
        validate(user);
        log.info("Added user: {}", user.getName());
        return userStorage.addUser(user);
    }

    public User updateUser(User newUser) {
        validate(newUser);
        findUserOrThrow(newUser.getId());
        log.info("Updated user with id: {}", newUser.getId());
        return userStorage.updateUser(newUser);
    }

    public void addFriend(Long userId, Long friendId) {
        checkUserConflict(userId, friendId);
        User user = findUserOrThrow(userId);
        User friend = findUserOrThrow(friendId);
        log.info("User with id: {} got friend with id: {}", userId, friendId);

        if (friend.getFriends().containsKey(userId)) {
            userStorage.setFriendStatus(user, friendId, CONFIRMED);
            userStorage.setFriendStatus(friend, userId, CONFIRMED);
            return;
        }

        userStorage.setFriendStatus(user, friendId, UNCONFIRMED);
    }

    public void deleteFriend(Long id, Long friendId) {
        checkUserConflict(id, friendId);
        findUserOrThrow(id);
        User friend = findUserOrThrow(friendId);

        log.info("Friendship broken between user id = {} and user id = {}", id, friendId);
        if (friend.getFriends().containsKey(id)) {
            userStorage.setFriendStatus(friend, id, UNCONFIRMED);
        }
        userStorage.removeFriend(id, friendId);
    }

    public List<User> getFriends(Long userId) {
        User user = findUserOrThrow(userId);
        return userStorage.getFriends(user);
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        User user = findUserOrThrow(userId);
        User other = findUserOrThrow(otherId);

        List<User> userFriendIds = userStorage.getFriends(user);
        Set<User> otherFriendIds = new HashSet<>(userStorage.getFriends(other));

        return userFriendIds.stream()
                .filter(otherFriendIds::contains)
                .toList();
    }

    private User findUserOrThrow(Long userId) {
        return userStorage.getUserById(userId).orElseThrow(() -> {
            log.error("User with id = {} not found.", userId);
            return new NotFoundException("Пользователь с id: [" + userId + "] не найден.");
        });
    }

    private void checkUserConflict(Long id, Long friendId) {
        if (Objects.equals(id, friendId)) {
            log.error("User with id = {} conflicted with friendId = {}.", id, friendId);
            throw new ValidationException(
                    "Попытка добавить или удалить друга с тем же id, что и у пользователя.");
        }
    }

    private void validate(User user) {
        checkName(user);
    }

    private void checkName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
