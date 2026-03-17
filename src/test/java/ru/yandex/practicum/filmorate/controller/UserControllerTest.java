package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
@ActiveProfiles("test")
public class UserControllerTest {

    @Autowired
    private UserController userController;
    private Validator validator;
    private User createUser;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
        createUser = User.builder()
                .email("test@email.com")
                .login("test-login")
                .name("test-name")
                .birthday(LocalDate.of(2000, 12, 12))
                .build();

        userController.addUser(createUser);
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
                .id(createUser.getId())
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

        assertEquals("Пользователь с id: [9999] не найден.", exception.getMessage());
        assertFalse(userController.getUsers().size() > 1);
        assertNotEquals(wrongUser, userController.getUsers().getFirst());
    }

    @Test
    void shouldUpdateUserWhenNameIsNull() {
        User validUser = User.builder()
                .id(createUser.getId())
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

        userController.addFriend(createUser.getId(), friend.getId());
        List<User> friends = userController.getFriends(createUser.getId());
        List<User> friendOfFriend = userController.getFriends(friend.getId());

        assertTrue(friends.contains(friend));
        assertTrue(friendOfFriend.isEmpty());
    }

    @Test
    void shouldNotAddFriendWhenWithIdSameAsUserId() {
        long id = createUser.getId();
        ValidationException exception = assertThrows(
                ValidationException.class, () -> userController.addFriend(id, id));

        assertEquals("Попытка добавить или удалить друга с тем же id, что и у пользователя.", exception.getMessage());
        assertTrue(userController.getFriends(id).isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenTryDeleteFriendWithIdSameAsUserId() {
        long id = createUser.getId();
        ValidationException exception = assertThrows(
                ValidationException.class, () -> userController.deleteFriend(id, id));

        assertEquals("Попытка добавить или удалить друга с тем же id, что и у пользователя.", exception.getMessage());
        assertTrue(userController.getFriends(id).isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenIdFriendNotFoundWhileAddFriend() {
        NotFoundException exception = assertThrows(
                NotFoundException.class, () -> userController.addFriend(createUser.getId(), 999L));

        assertEquals("Пользователь с id: [999] не найден.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenIdFriendNotFoundWhileDeleteFriend() {
        NotFoundException exception = assertThrows(
                NotFoundException.class, () -> userController.deleteFriend(createUser.getId(), 999L));

        assertEquals("Пользователь с id: [999] не найден.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenIdUserNotFoundWhileAddFriend() {
        NotFoundException exception = assertThrows(
                NotFoundException.class, () -> userController.addFriend(999L, createUser.getId()));

        assertEquals("Пользователь с id: [999] не найден.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenIdUserNotFoundWhileDeleteFriend() {
        NotFoundException exception = assertThrows(
                NotFoundException.class, () -> userController.deleteFriend(999L, createUser.getId()));

        assertEquals("Пользователь с id: [999] не найден.", exception.getMessage());
    }

    @Test
    void shouldReturnRightCommonFriends() {
        List<User> users = setUpUserFriends();
        long firstUser = createUser.getId();
        long thirdUser = users.get(1).getId();
        long fourthUser = users.get(2).getId();
        List<User> commFriends = userController.getCommonFriends(firstUser, fourthUser);
        assertEquals(1, commFriends.size());
        assertEquals(thirdUser, commFriends.getFirst().getId());
    }

    List<User> setUpUserFriends() {
        long id = createUser.getId();
        User u2 = User.builder()
                .email("test@email.com")
                .login("test-login")
                .name("name1")
                .birthday(LocalDate.of(2000, 12, 12))
                .build();
        User u3 = User.builder()
                .email("test@email.com")
                .login("test-login")
                .name("name2")
                .birthday(LocalDate.of(2000, 12, 12))
                .build();
        User u4 = User.builder()
                .email("test@email.com")
                .login("test-login")
                .name("name3")
                .birthday(LocalDate.of(2000, 12, 12))
                .build();

        User user2 = userController.addUser(u2);
        User user3 = userController.addUser(u3);
        User user4 = userController.addUser(u4);

        userController.addFriend(id, u2.getId());
        userController.addFriend(id, u3.getId());
        userController.addFriend(id, u4.getId());

        userController.addFriend(u4.getId(), u3.getId());
        return Arrays.asList(user2, user3, user4);
    }
}
