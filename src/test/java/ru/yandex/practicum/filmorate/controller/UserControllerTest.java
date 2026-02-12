package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class UserControllerTest {

    private UserController userController;
    private Validator validator;

    @BeforeEach
    void setUp() {
        userController = new UserController();
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
        User user = User.builder()
                .id(1)
                .email("test@email.com")
                .login("test-login")
                .name("test-name")
                .birthday(LocalDate.of(2000, 12, 12))
                .build();

        Errors errors = new BeanPropertyBindingResult(user, "user");
        userController.addUser(user, errors);
    }

    @Test
    void shouldAddUserWhenUserIsValid() {
        User validUser = User.builder()
                .email("user@email.com")
                .login("user-login")
                .name("user-name")
                .birthday(LocalDate.of(2002, 12, 12))
                .build();

        Errors errors = new BeanPropertyBindingResult(validUser, "user");
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        User user = userController.addUser(validUser, errors);

        assertTrue(violations.isEmpty());
        assertNotNull(user);
        assertEquals(2, user.getId());
        assertEquals(user.getEmail(), validUser.getEmail());
        assertEquals(user.getLogin(), validUser.getLogin());
        assertEquals(user.getBirthday(), validUser.getBirthday());
        assertTrue(userController.getUsers().contains(user));
    }

    @Test
    void shouldNotAddUserWhenUserEmailIsEmpty() {
        String wrongEmail = "";
        User wrongUser = User.builder()
                .email(wrongEmail)
                .login("user-login")
                .name("user-name")
                .birthday(LocalDate.of(2002, 12, 12))
                .build();

        Errors errors = processErrors(wrongUser);
        ValidationException exception = assertThrows(
                ValidationException.class, () -> userController.addUser(wrongUser, errors));

        assertEquals("User validation didn't pass - wrong email.", exception.getMessage());
        assertFalse(userController.getUsers().size() > 1);
        assertNotEquals(wrongEmail, userController.getUsers().getFirst().getEmail());
    }

    @Test
    void shouldNotAddUserWhenUserEmailIsWrong() {
        String wrongEmail = "my-email";
        User wrongUser = User.builder()
                .email(wrongEmail)
                .login("user-login")
                .name("user-name")
                .birthday(LocalDate.of(2002, 12, 12))
                .build();

        Errors errors = processErrors(wrongUser);
        ValidationException exception = assertThrows(
                ValidationException.class, () -> userController.addUser(wrongUser, errors));

        assertEquals("User validation didn't pass - wrong email.", exception.getMessage());
        assertFalse(userController.getUsers().size() > 1);
        assertNotEquals(wrongEmail, userController.getUsers().getFirst().getEmail());
    }

    @Test
    void shouldNotAddUserWhenUserEmailIsNull() {
        User wrongUser = User.builder()
                .email(null)
                .login("user-login")
                .name("user-name")
                .birthday(LocalDate.of(2002, 12, 12))
                .build();

        Errors errors = processErrors(wrongUser);
        ValidationException exception = assertThrows(
                ValidationException.class, () -> userController.addUser(wrongUser, errors));

        assertEquals("User validation didn't pass - wrong email.", exception.getMessage());
        assertFalse(userController.getUsers().size() > 1);
        assertNotEquals(null, userController.getUsers().getFirst().getEmail());
    }

    @Test
    void shouldNotAddUserWhenLoginIsEmpty() {
        String wrongLogin = "";
        User wrongUser = User.builder()
                .email("user@email.com")
                .login(wrongLogin)
                .name("user-name")
                .birthday(LocalDate.of(2002, 12, 12))
                .build();

        Errors errors = processErrors(wrongUser);
        ValidationException exception = assertThrows(
                ValidationException.class, () -> userController.addUser(wrongUser, errors));

        assertEquals("User validation didn't pass - wrong login.", exception.getMessage());
        assertFalse(userController.getUsers().size() > 1);
        assertNotEquals(wrongLogin, userController.getUsers().getFirst().getLogin());
    }

    @Test
    void shouldNotAddUserWhenLoginIsNull() {
        User wrongUser = User.builder()
                .email("user@email.com")
                .login(null)
                .name("user-name")
                .birthday(LocalDate.of(2002, 12, 12))
                .build();

        Errors errors = processErrors(wrongUser);
        ValidationException exception = assertThrows(
                ValidationException.class, () -> userController.addUser(wrongUser, errors));

        assertEquals("User validation didn't pass - wrong login.", exception.getMessage());
        assertFalse(userController.getUsers().size() > 1);
        assertNotEquals(null, userController.getUsers().getFirst().getLogin());
    }

    @Test
    void shouldNotAddUserWhenLoginHasSpaces() {
        String wrongLogin = "my wrong login";
        User wrongUser = User.builder()
                .email("user@email.com")
                .login(wrongLogin)
                .name("user-name")
                .birthday(LocalDate.of(2002, 12, 12))
                .build();

        Errors errors = processErrors(wrongUser);
        ValidationException exception = assertThrows(
                ValidationException.class, () -> userController.addUser(wrongUser, errors));

        assertEquals("User validation didn't pass - wrong login.", exception.getMessage());
        assertFalse(userController.getUsers().size() > 1);
        assertNotEquals(wrongLogin, userController.getUsers().getFirst().getLogin());
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

        Errors errors = new BeanPropertyBindingResult(validUser, "user");
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        User user = userController.addUser(validUser, errors);

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

        Errors errors = new BeanPropertyBindingResult(validUser, "user");
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        User user = userController.addUser(validUser, errors);

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
    void shouldNotAddUserWhenBirthDateIsNotPast() {
        LocalDate wrongBirthDate = LocalDate.now();
        User wrongUser = User.builder()
                .email("user@email.com")
                .login("user-login")
                .name("user-name")
                .birthday(wrongBirthDate)
                .build();

        Errors errors = processErrors(wrongUser);
        ValidationException exception = assertThrows(
                ValidationException.class, () -> userController.addUser(wrongUser, errors));

        assertEquals("User validation didn't pass - wrong birth date.", exception.getMessage());
        assertFalse(userController.getUsers().size() > 1);
        assertNotEquals(wrongBirthDate, userController.getUsers().getFirst().getBirthday());
    }

    @Test
    void shouldUpdateUserWhenUserFound() {
        User validUser = User.builder()
                .id(1)
                .email("user@email.com")
                .login("user-login")
                .name("user-name")
                .birthday(LocalDate.of(2002, 12, 12))
                .build();

        Errors errors = new BeanPropertyBindingResult(validUser, "user");
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        User user = userController.updateUser(validUser, errors);

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
                .id(9999)
                .email("user@email.com")
                .login("user-login")
                .name("user-name")
                .birthday(LocalDate.of(2002, 12, 12))
                .build();

        Errors errors = processErrors(wrongUser);
        ValidationException exception = assertThrows(
                ValidationException.class, () -> userController.updateUser(wrongUser, errors));

        assertEquals("User not found.", exception.getMessage());
        assertFalse(userController.getUsers().size() > 1);
        assertNotEquals(wrongUser, userController.getUsers().getFirst());
    }

    @Test
    void shouldNotUpdateUserWhenUserEmailIsEmpty() {
        String wrongEmail = "";
        User wrongUser = User.builder()
                .email(wrongEmail)
                .login("user-login")
                .name("user-name")
                .birthday(LocalDate.of(2002, 12, 12))
                .build();

        Errors errors = processErrors(wrongUser);
        ValidationException exception = assertThrows(
                ValidationException.class, () -> userController.updateUser(wrongUser, errors));

        assertEquals("User validation didn't pass - wrong email.", exception.getMessage());
        assertFalse(userController.getUsers().size() > 1);
        assertNotEquals(wrongEmail, userController.getUsers().getFirst().getEmail());
    }

    @Test
    void shouldNotUpdateUserWhenUserEmailIsWrong() {
        String wrongEmail = "my-email";
        User wrongUser = User.builder()
                .email(wrongEmail)
                .login("user-login")
                .name("user-name")
                .birthday(LocalDate.of(2002, 12, 12))
                .build();

        Errors errors = processErrors(wrongUser);
        ValidationException exception = assertThrows(
                ValidationException.class, () -> userController.updateUser(wrongUser, errors));

        assertEquals("User validation didn't pass - wrong email.", exception.getMessage());
        assertFalse(userController.getUsers().size() > 1);
        assertNotEquals(wrongEmail, userController.getUsers().getFirst().getEmail());
    }

    @Test
    void shouldNotUpdateUserWhenUserEmailIsNull() {
        User wrongUser = User.builder()
                .email(null)
                .login("user-login")
                .name("user-name")
                .birthday(LocalDate.of(2002, 12, 12))
                .build();

        Errors errors = processErrors(wrongUser);
        ValidationException exception = assertThrows(
                ValidationException.class, () -> userController.updateUser(wrongUser, errors));

        assertEquals("User validation didn't pass - wrong email.", exception.getMessage());
        assertFalse(userController.getUsers().size() > 1);
        assertNotEquals(null, userController.getUsers().getFirst().getEmail());
    }

    @Test
    void shouldNotUpdateUserWhenLoginIsEmpty() {
        String wrongLogin = "";
        User wrongUser = User.builder()
                .email("user@email.com")
                .login(wrongLogin)
                .name("user-name")
                .birthday(LocalDate.of(2002, 12, 12))
                .build();

        Errors errors = processErrors(wrongUser);
        ValidationException exception = assertThrows(
                ValidationException.class, () -> userController.updateUser(wrongUser, errors));

        assertEquals("User validation didn't pass - wrong login.", exception.getMessage());
        assertFalse(userController.getUsers().size() > 1);
        assertNotEquals(wrongLogin, userController.getUsers().getFirst().getLogin());
    }

    @Test
    void shouldNotUpdateUserWhenLoginIsNull() {
        User wrongUser = User.builder()
                .email("user@email.com")
                .login(null)
                .name("user-name")
                .birthday(LocalDate.of(2002, 12, 12))
                .build();

        Errors errors = processErrors(wrongUser);
        ValidationException exception = assertThrows(
                ValidationException.class, () -> userController.updateUser(wrongUser, errors));

        assertEquals("User validation didn't pass - wrong login.", exception.getMessage());
        assertFalse(userController.getUsers().size() > 1);
        assertNotEquals(null, userController.getUsers().getFirst().getLogin());
    }

    @Test
    void shouldNotUpdateUserWhenLoginHasSpaces() {
        String wrongLogin = "my wrong login";
        User wrongUser = User.builder()
                .email("user@email.com")
                .login(wrongLogin)
                .name("user-name")
                .birthday(LocalDate.of(2002, 12, 12))
                .build();

        Errors errors = processErrors(wrongUser);
        ValidationException exception = assertThrows(
                ValidationException.class, () -> userController.updateUser(wrongUser, errors));

        assertEquals("User validation didn't pass - wrong login.", exception.getMessage());
        assertFalse(userController.getUsers().size() > 1);
        assertNotEquals(wrongLogin, userController.getUsers().getFirst().getLogin());
    }

    @Test
    void shouldUpdateUserWhenNameIsEmpty() {
        String name = "";
        User validUser = User.builder()
                .id(1)
                .email("user@email.com")
                .login("user-login")
                .name(name)
                .birthday(LocalDate.of(2002, 12, 12))
                .build();

        Errors errors = new BeanPropertyBindingResult(validUser, "user");
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        User user = userController.updateUser(validUser, errors);

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
    void shouldUpdateUserWhenNameIsNull() {
        User validUser = User.builder()
                .id(1)
                .email("user@email.com")
                .login("user-login")
                .name(null)
                .birthday(LocalDate.of(2002, 12, 12))
                .build();

        Errors errors = new BeanPropertyBindingResult(validUser, "user");
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        User user = userController.updateUser(validUser, errors);

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
    void shouldNotUpdateUserWhenBirthDateIsNotPast() {
        LocalDate wrongBirthDate = LocalDate.now();
        User wrongUser = User.builder()
                .email("user@email.com")
                .login("user-login")
                .name("user-name")
                .birthday(wrongBirthDate)
                .build();

        Errors errors = processErrors(wrongUser);
        ValidationException exception = assertThrows(
                ValidationException.class, () -> userController.updateUser(wrongUser, errors));

        assertEquals("User validation didn't pass - wrong birth date.", exception.getMessage());
        assertFalse(userController.getUsers().size() > 1);
        assertNotEquals(wrongBirthDate, userController.getUsers().getFirst().getBirthday());
    }

    Errors processErrors(User wrongUser) {
        Errors errors = new BeanPropertyBindingResult(wrongUser, "user");
        for (ConstraintViolation<User> v : validator.validate(wrongUser)) {
            errors.rejectValue(v.getPropertyPath().toString(), "invalid", v.getMessage());
        }
        return errors;
    }
}
