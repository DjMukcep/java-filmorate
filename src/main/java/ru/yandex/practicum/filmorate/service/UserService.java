package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;


    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    public User addUser(User user) {
        validate(user);

        Long id = (long) (getUsers().size() + 1);
        user.setId(id);
        userStorage.saveUser(user);
        log.info("Added user: {}", user.getName());
        return user;
    }

    public User updateUser(User newUser) {
        validate(newUser);

        return userStorage.getUserById(newUser.getId())
                .map(user -> processUpdateUser(newUser))
                .orElseThrow(() -> {
                    log.error("Error updating user: {}", newUser.getName());
                    return new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден.");
                });
    }

    public void addFriend(Long id, Long friendId) {
        checkUserConflict(id, friendId);
        checkUserPresence(id, friendId);

        log.info("User with id: {} got friend with id: {}", id, friendId);
        userStorage.addFriend(friendId, id);
    }

    public void deleteFriend(Long id, Long friendId) {
        checkUserConflict(id, friendId);
        checkUserPresence(id, friendId);

        log.info("Friendship broken between user id = {} and user id = {}", id, friendId);
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

    void checkUserPresence(Long... userIds) {
        Set<Long> userIdSet = Set.of(userIds);
        List<Long> foundIds = userStorage.findExistentIds(userIdSet);

        if (userIdSet.size() != foundIds.size()) {
            String missingIds = userIdSet.stream()
                    .filter(ids -> !foundIds.contains(ids))
                    .collect(Collectors.toSet()).toString();
            log.error("Users not found with ids: {}", missingIds);
            throw new NotFoundException("Не найдены пользователи с id: " + missingIds);
        }
    }

    private void checkUserConflict(Long id, Long friendId) {
        if (Objects.equals(id, friendId)) {
            log.error("User with id = {} conflicted with friendId = {}.", id, friendId);
            throw new ValidationException(
                    "Попытка добавить или удалить друга с тем же id, что и у пользователя.");
        }
    }

    private User findUserOrThrow(Long userId) {
        return userStorage.getUserById(userId).orElseThrow(() -> {
            log.error("User with id = {} not found.", userId);
            return new NotFoundException("Пользователь с id = " + userId + " не найден.");
        });
    }

    private User processUpdateUser(User newUser) {
        userStorage.saveUser(newUser);
        log.info("Updated user with id: {}", newUser.getId());
        return newUser;
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
