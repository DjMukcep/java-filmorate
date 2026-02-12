package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final List<User> users = new ArrayList<>();

    @GetMapping
    public List<User> getUsers() {
        return List.copyOf(users);
    }

    @PostMapping
    public User addUser(@Valid @RequestBody User user, Errors errors) {
        validate(user, errors);
        int id = users.size() + 1;
        user.setId(id);
        users.add(user);
        log.info("Added user: {}", user.getName());
        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User newUser, Errors errors) {
        validate(newUser, errors);

        return users.stream()
                .filter(user -> user.getId().equals(newUser.getId()))
                .findFirst()
                .map(user -> processUpdateUser(user, newUser))
                .orElseThrow(() -> new ValidationException("User not found."));
    }

    private User processUpdateUser(User oldUser, User newUser) {
        int index = users.indexOf(oldUser);
        newUser.setId(oldUser.getId());
        users.set(index, newUser);
        log.info("Updated user with id: {}", newUser.getId());
        return newUser;
    }

    private void validate(@Valid User user, Errors errors) {
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
