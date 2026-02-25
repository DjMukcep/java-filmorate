package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class UserControllerTest {

    private UserController userController;
    private Validator validator;

    @BeforeEach
    void setUp() {
        userController = new UserController(new UserService(new InMemoryUserStorage()));
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
        User user = User.builder()
                .email("test@email.com")
                .login("test-login")
                .name("test-name")
                .birthday(LocalDate.of(2000, 12, 12))
                .build();

        userController.addUser(user);
    }

    @Test
    void shouldAddUserWhenUserIsValid() {
        User validUser = User.builder()
                .email("user@email.com")
                .login("user-login")
                .name("user-name")
                .birthday(LocalDate.of(2002, 12, 12))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        User user = userController.addUser(validUser);

        assertTrue(violations.isEmpty());
        assertNotNull(user);
        assertEquals(2, user.getId());
        assertEquals(user.getEmail(), validUser.getEmail());
        assertEquals(user.getLogin(), validUser.getLogin());
        assertEquals(user.getBirthday(), validUser.getBirthday());
        assertTrue(userController.getUsers().contains(user));
    }

    @Test
    void shouldHaveValidationErrorsWhenUserEmailIsEmpty() {
        String wrongEmail = "";
        User wrongUser = User.builder()
                .email(wrongEmail)
                .login("user-login")
                .name("user-name")
                .birthday(LocalDate.of(2002, 12, 12))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(wrongUser);
        String message = violations.iterator().next().getMessage();

        assertEquals("Поле email обязательно к заполнению", message);
    }

    @Test
    void shouldHaveValidationErrorsWhenUserEmailIsWrong() {
        String wrongEmail = "my-email";
        User wrongUser = User.builder()
                .email(wrongEmail)
                .login("user-login")
                .name("user-name")
                .birthday(LocalDate.of(2002, 12, 12))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(wrongUser);
        String message = violations.iterator().next().getMessage();

        assertEquals("Обнаружен некорректный email.", message);
    }

    @Test
    void shouldHaveValidationErrorsWhenUserEmailIsNull() {
        User wrongUser = User.builder()
                .email(null)
                .login("user-login")
                .name("user-name")
                .birthday(LocalDate.of(2002, 12, 12))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(wrongUser);
        String message = violations.iterator().next().getMessage();

        assertEquals("Поле email обязательно к заполнению", message);
    }

    @Test
    void shouldHaveValidationErrorsWhenLoginIsEmpty() {
        String wrongLogin = "";
        User wrongUser = User.builder()
                .email("user@email.com")
                .login(wrongLogin)
                .name("user-name")
                .birthday(LocalDate.of(2002, 12, 12))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(wrongUser);
        boolean hasBlankMessage = violations.stream()
                .anyMatch(v -> v.getMessage().equals("Имя пользователя обязательно к заполнению."));

        assertTrue(hasBlankMessage, "Сообщение о пустом логине не найдено");
    }

    @Test
    void shouldHaveValidationErrorsWhenLoginIsNull() {
        User wrongUser = User.builder()
                .email("user@email.com")
                .login(null)
                .name("user-name")
                .birthday(LocalDate.of(2002, 12, 12))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(wrongUser);
        boolean hasBlankMessage = violations.stream()
                .anyMatch(v -> v.getMessage().equals("Имя пользователя обязательно к заполнению."));

        assertTrue(hasBlankMessage, "Сообщение о пустом логине не найдено");
    }

    @Test
    void shouldHaveValidationErrorsWhenLoginHasSpaces() {
        String wrongLogin = "my wrong login";
        User wrongUser = User.builder()
                .email("user@email.com")
                .login(wrongLogin)
                .name("user-name")
                .birthday(LocalDate.of(2002, 12, 12))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(wrongUser);
        String message = violations.iterator().next().getMessage();

        assertEquals("В имени пользователя пробелы не допускаются.", message);
    }

    @Test
    void shouldAddUserWhenNameIsEmpty() {
        String name = "";
        User validUser = User.builder()
                .email("user@email.com")
                .login("user-login")
                .name(name)
                .birthday(LocalDate.of(2002, 12, 12))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        User user = userController.addUser(validUser);

        assertTrue(violations.isEmpty());
        assertNotNull(user);
        assertEquals(2, user.getId());
        assertEquals(user.getEmail(), validUser.getEmail());
        assertEquals(user.getLogin(), validUser.getLogin());
        assertEquals(user.getName(), validUser.getLogin());
        assertEquals(user.getBirthday(), validUser.getBirthday());
        assertTrue(userController.getUsers().contains(user));
    }

    @Test
    void shouldAddUserWhenNameIsNull() {
        User validUser = User.builder()
                .email("user@email.com")
                .login("user-login")
                .name(null)
                .birthday(LocalDate.of(2002, 12, 12))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        User user = userController.addUser(validUser);

        assertTrue(violations.isEmpty());
        assertNotNull(user);
        assertEquals(2, user.getId());
        assertEquals(user.getEmail(), validUser.getEmail());
        assertEquals(user.getLogin(), validUser.getLogin());
        assertEquals(user.getName(), validUser.getLogin());
        assertEquals(user.getBirthday(), validUser.getBirthday());
        assertTrue(userController.getUsers().contains(user));
    }

    @Test
    void shouldHaveValidationErrorsWhenBirthDateIsNotPast() {
        LocalDate wrongBirthDate = LocalDate.now();
        User wrongUser = User.builder()
                .email("user@email.com")
                .login("user-login")
                .name("user-name")
                .birthday(wrongBirthDate)
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(wrongUser);
        String message = violations.iterator().next().getMessage();

        assertEquals("Дата дня рожденья должна быть в прошлом.", message);
    }

    @Test
    void shouldUpdateUserWhenUserFound() {
        User validUser = User.builder()
                .id(1L)
                .email("user@email.com")
                .login("user-login")
                .name("user-name")
                .birthday(LocalDate.of(2002, 12, 12))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        User user = userController.updateUser(validUser);

        assertTrue(violations.isEmpty());
        assertNotNull(user);
        assertTrue(userController.getUsers().contains(user));
        assertFalse(userController.getUsers().size() > 1);

        assertEquals(1, user.getId());
        assertEquals(user.getEmail(), validUser.getEmail());
        assertEquals(user.getLogin(), validUser.getLogin());
        assertEquals(user.getName(), validUser.getName());
        assertEquals(user.getBirthday(), validUser.getBirthday());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        User wrongUser = User.builder()
                .id(9999L)
                .email("user@email.com")
                .login("user-login")
                .name("user-name")
                .birthday(LocalDate.of(2002, 12, 12))
                .build();

        NotFoundException exception = assertThrows(
                NotFoundException.class, () -> userController.updateUser(wrongUser));

        assertEquals("Пользователь с id = 9999 не найден.", exception.getMessage());
        assertFalse(userController.getUsers().size() > 1);
        assertNotEquals(wrongUser, userController.getUsers().getFirst());
    }

    @Test
    void shouldUpdateUserWhenNameIsNull() {
        User validUser = User.builder()
                .id(1L)
                .email("user@email.com")
                .login("user-login")
                .name(null)
                .birthday(LocalDate.of(2002, 12, 12))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        User user = userController.updateUser(validUser);

        assertNotNull(user);
        assertTrue(violations.isEmpty());
        assertFalse(userController.getUsers().size() > 1);
        assertTrue(userController.getUsers().contains(user));

        assertEquals(1, user.getId());
        assertEquals(user.getEmail(), validUser.getEmail());
        assertEquals(user.getLogin(), validUser.getLogin());
        assertEquals(user.getName(), validUser.getLogin());
        assertEquals(user.getBirthday(), validUser.getBirthday());
    }

    @Test
    void shouldAddFriendWhenValidFriendPassed() {
        User friend = User.builder()
                .email("user@email.com")
                .login("user-login")
                .name("friend-name")
                .birthday(LocalDate.of(2002, 12, 12))
                .build();
        userController.addUser(friend);

        userController.addFriend(1L, friend.getId());
        List<User> friends = userController.getFriends(1L);
        List<User> friendOfFriend = userController.getFriends(friend.getId());

        assertTrue(friends.contains(friend));
        assertEquals(1L, friendOfFriend.getFirst().getId());
    }

    @Test
    void shouldNotAddFriendWhenWithIdSameAsUserId() {
        ValidationException exception = assertThrows(
                ValidationException.class, () -> userController.addFriend(1L, 1L));

        assertEquals("Попытка добавить или удалить друга с тем же id, что и у пользователя.", exception.getMessage());
        assertTrue(userController.getFriends(1L).isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenTryDeleteFriendWithIdSameAsUserId() {
        ValidationException exception = assertThrows(
                ValidationException.class, () -> userController.deleteFriend(1L, 1L));

        assertEquals("Попытка добавить или удалить друга с тем же id, что и у пользователя.", exception.getMessage());
        assertTrue(userController.getFriends(1L).isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenIdFriendNotFoundWhileAddFriend() {
        NotFoundException exception = assertThrows(
                NotFoundException.class, () -> userController.addFriend(1L, 999L));

        assertEquals("Пользователь с id = 999 не найден.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenIdFriendNotFoundWhileDeleteFriend() {
        NotFoundException exception = assertThrows(
                NotFoundException.class, () -> userController.deleteFriend(1L, 999L));

        assertEquals("Пользователь с id = 999 не найден.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenIdUserNotFoundWhileAddFriend() {
        NotFoundException exception = assertThrows(
                NotFoundException.class, () -> userController.addFriend(999L, 1L));

        assertEquals("Пользователь с id = 999 не найден.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenIdUserNotFoundWhileDeleteFriend() {
        NotFoundException exception = assertThrows(
                NotFoundException.class, () -> userController.deleteFriend(999L, 1L));

        assertEquals("Пользователь с id = 999 не найден.", exception.getMessage());
    }

    @Test
    void shouldReturnRightCommonFriends() {
        setUpUserFriends();
        List<User> commFriends = userController.getCommonFriends(1L, 4L);
        assertEquals(1, commFriends.size());
        assertEquals(3, commFriends.getFirst().getId());
    }

    void setUpUserFriends() {
        User user2 = User.builder()
                .email("test@email.com")
                .login("test-login")
                .name("name1")
                .birthday(LocalDate.of(2000, 12, 12))
                .build();
        User user3 = User.builder()
                .email("test@email.com")
                .login("test-login")
                .name("name2")
                .birthday(LocalDate.of(2000, 12, 12))
                .build();
        User user4 = User.builder()
                .email("test@email.com")
                .login("test-login")
                .name("name3")
                .birthday(LocalDate.of(2000, 12, 12))
                .build();

        userController.addUser(user2);
        userController.addUser(user3);
        userController.addUser(user4);

        userController.addFriend(1L, user2.getId());
        userController.addFriend(1L, user3.getId());
        userController.addFriend(1L, user4.getId());

        userController.addFriend(user4.getId(), user3.getId());
    }
}
