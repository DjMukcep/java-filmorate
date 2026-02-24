package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    public User addUser(User user, Errors errors) {
        validate(user, errors);

        Long id = (long) (getUsers().size() + 1);
        user.setId(id);
        userStorage.saveUser(user);
        log.info("Added user: {}", user.getName());
        return user;
    }

    public User updateUser(User newUser, Errors errors) {
        validate(newUser, errors);

        return userStorage.getUserById(newUser.getId())
                .map(user -> processUpdateUser(newUser))
                .orElseThrow(() -> {
                    log.error("Error updating user: {}", newUser.getName());
                    return new NotFoundException("User with id = " + newUser.getId() + " not found.");
                });
    }

    public void addFriend(Long id, Long friendId) {
        checkUserPresence(id, friendId);
        checkUserConflict(id, friendId);

        log.info("User with id: {} got friend with id: {}", id, friendId);
        userStorage.addFriend(friendId, id);
    }

    public void deleteFriend(Long id, Long friendId) {
        checkUserPresence(id, friendId);
        checkUserConflict(id, friendId);

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
        Arrays.stream(userIds).forEach(this::findUserOrThrow);
    }

    private void checkUserConflict(Long id, Long friendId) {
        if (Objects.equals(id, friendId)) {
            log.error("User with id = {} conflicted with friendId = {}.", id, friendId);
            throw new ValidationException("Detected try to pass user and friend with same id.");
        }
    }

    private User findUserOrThrow(Long userId) {
        return userStorage.getUserById(userId).orElseThrow(() -> {
            log.error("User with id = {} not found.", userId);
            return new NotFoundException("User with id = " + userId + " not found.");
        });
    }

    private User processUpdateUser(User newUser) {
        userStorage.saveUser(newUser);
        log.info("Updated user with id: {}", newUser.getId());
        return newUser;
    }

    private void validate(User user, Errors errors) {
        checkLogin(errors);
        checkName(user);
        checkEmail(errors);
        checkBirthDate(errors);
    }

    private void checkLogin(Errors errors) {
        if (errors.hasFieldErrors("login")) {
            processError("User validation didn't pass - wrong login.");
        }
    }

    private void checkName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    private void checkEmail(Errors errors) {
        if (errors.hasFieldErrors("email")) {
            processError("User validation didn't pass - wrong email.");
        }
    }

    private void checkBirthDate(Errors errors) {
        if (errors.hasFieldErrors("birthday")) {
            processError("User validation didn't pass - wrong birth date.");
        }
    }

    private void processError(String message) {
        log.error(message);
        throw new ValidationException(message);
    }
}
