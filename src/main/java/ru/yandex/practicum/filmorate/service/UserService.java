package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import static ru.yandex.practicum.filmorate.model.FriendshipStatus.*;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    public User getUserById(Long id) {
        return findUserOrThrow(id);
    }

    public User addUser(User user) {
        validate(user);
        User newUser = userStorage.addUser(user);
        log.info("New user: {}", newUser);
        return newUser;
    }

    public User updateUser(User newUser) {
        validate(newUser);
        findUserOrThrow(newUser.getId());
        log.info("Update user: {}", newUser);
        return userStorage.updateUser(newUser);
    }

    public void addFriend(Long userId, Long friendId) {
        checkUserConflict(userId, friendId);
        User user = findUserOrThrow(userId);
        User friend = findUserOrThrow(friendId);
        List<User> friends = userStorage.getFriends(friend);


        if (friends.contains(user)) {
            userStorage.setFriendStatus(user, friendId, CONFIRMED);
            userStorage.setFriendStatus(friend, userId, CONFIRMED);
            log.info("User with id: {} got confirmed friend with id: {}", userId, friendId);
            return;
        }

        log.info("User with id: {} got unconfirmed friend with id: {}", userId, friendId);
        userStorage.setFriendStatus(user, friendId, UNCONFIRMED);
    }

    public void deleteFriend(Long id, Long friendId) {
        checkUserConflict(id, friendId);
        User user = findUserOrThrow(id);
        User friend = findUserOrThrow(friendId);
        List<User> friends = userStorage.getFriends(friend);

        log.info("Friendship broken between user id = {} and user id = {}", id, friendId);
        if (friends.contains(user)) {
            userStorage.setFriendStatus(friend, id, UNCONFIRMED);
            log.info("User with id: {} has unconfirmed friendship now with user id: {}", friendId, id);
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
