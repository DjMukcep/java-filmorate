package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;


import static org.junit.jupiter.api.Assertions.*;

public class FilmControllerTest {

    private FilmController filmController;
    private Validator validator;
    private FilmService filmService;
    private UserService userService;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
        userService = new UserService(new InMemoryUserStorage());
        filmService = new FilmService(new InMemoryFilmStorage(), userService);
        filmController = new FilmController(filmService);
        Film film = Film.builder()
                .name("default-film")
                .description("default-description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(100)
                .build();

        filmController.addFilm(film);
    }

    @Test
    void shouldAddFilmWhenFilmValid() {
        Film validFilm = Film.builder()
                .name("film")
                .description("_".repeat(200))
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(100)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        Film film = filmController.addFilm(validFilm);

        assertTrue(violations.isEmpty());
        assertNotNull(film.getId());
        assertEquals(film.getName(), validFilm.getName());
        assertEquals(film.getDescription(), validFilm.getDescription());
        assertEquals(film.getReleaseDate(), validFilm.getReleaseDate());
        assertEquals(film.getDuration(), validFilm.getDuration());
        assertTrue(filmController.getFilms().contains(validFilm));
    }


    @Test
    void shouldHaveValidationErrorsWhenFilmNameEmpty() {
        String wrongName = "";
        Film wrongFilm = Film.builder()
                .name(wrongName)
                .description("description")
                .releaseDate(LocalDate.of(1999, 2, 2))
                .duration(100)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(wrongFilm);
        String message = violations.iterator().next().getMessage();

        assertFalse(violations.isEmpty());
        assertEquals("Название фильма не может быть пустым.", message);
    }

    @Test
    void shouldHaveValidationErrorsWhenFilmNameIsNull() {
        Film wrongFilm = Film.builder()
                .name(null)
                .description("description")
                .releaseDate(LocalDate.of(1999, 2, 2))
                .duration(100)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(wrongFilm);
        String message = violations.iterator().next().getMessage();

        assertEquals("Название фильма не может быть пустым.", message);
    }

    @Test
    void shouldHaveValidationErrorsWhenDescriptionOverMaxLength() {
        String wrongDescription = "_".repeat(201);
        Film wrongFilm = Film.builder()
                .name("film")
                .description(wrongDescription)
                .releaseDate(LocalDate.of(1999, 2, 2))
                .duration(100)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(wrongFilm);
        String message = violations.iterator().next().getMessage();

        assertEquals("Описание не должно превышать 200 символов.", message);
    }

    @Test
    void shouldHaveValidationErrorsWhenDescriptionIsNull() {
        Film wrongFilm = Film.builder()
                .name("film")
                .description(null)
                .releaseDate(LocalDate.of(1999, 2, 2))
                .duration(100)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(wrongFilm);
        String message = violations.iterator().next().getMessage();

        assertEquals("Описание обязательно к заполнению.", message);
    }

    @Test
    void shouldNotAddAndUpdateFilmWhenReleaseDateBeforeMinDate() {
        LocalDate wrongReleaseDate = LocalDate.of(1895, 12, 27);
        Film wrongFilm = Film.builder()
                .name("film")
                .description("description")
                .releaseDate(wrongReleaseDate)
                .duration(100)
                .build();

        ValidationException exception = assertThrows(
                ValidationException.class, () -> filmController.addFilm(wrongFilm));

        assertEquals("Самая ранняя разрешенная дата фильма: 1895-12-28", exception.getMessage());
        assertFalse(filmController.getFilms().size() > 1);
        assertNotEquals(filmController.getFilms().getFirst().getReleaseDate(), wrongReleaseDate);
    }

    @Test
    void shouldNotAddAndUpdateFilmWhenReleaseDateIsNull() {
        Film wrongFilm = Film.builder()
                .name("film")
                .description("description")
                .releaseDate(null)
                .duration(100)
                .build();

        ValidationException exception = assertThrows(
                ValidationException.class, () -> filmController.addFilm(wrongFilm));

        assertEquals("Самая ранняя разрешенная дата фильма: 1895-12-28", exception.getMessage());
        assertFalse(filmController.getFilms().size() > 1);
        assertNotEquals(null, filmController.getFilms().getFirst().getReleaseDate());
    }

    @Test
    void shouldHaveValidationErrorsWhenDurationIsZero() {
        Integer wrongDuration = 0;
        Film wrongFilm = Film.builder()
                .name("film")
                .description("description")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(wrongDuration)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(wrongFilm);
        String message = violations.iterator().next().getMessage();

        assertEquals("Продолжительность фильма должна быть положительной.", message);
    }

    @Test
    void shouldHaveValidationErrorsWhenDurationIsNegative() {
        Integer wrongDuration = -100;
        Film wrongFilm = Film.builder()
                .name("film")
                .description("description")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(wrongDuration)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(wrongFilm);
        String message = violations.iterator().next().getMessage();

        assertEquals("Продолжительность фильма должна быть положительной.", message);
    }

    @Test
    void shouldNHaveValidationErrorsWhenDurationIsNull() {
        Film wrongFilm = Film.builder()
                .name("film")
                .description("description")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(null)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(wrongFilm);
        String message = violations.iterator().next().getMessage();

        assertEquals("Длительность фильма должна быть указана.", message);
    }

    @Test
    void shouldUpdateFilmWhenFilmFound() {
        Film validFilm = Film.builder()
                .id(1L)
                .name("film")
                .description("description")
                .releaseDate(LocalDate.of(2000, 12, 28))
                .duration(100)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        Film film = filmController.updateFilm(validFilm);

        assertNotNull(film);
        assertTrue(violations.isEmpty());
        assertTrue(filmController.getFilms().contains(film));
        assertFalse(filmController.getFilms().size() > 1);

        assertEquals(1, film.getId());
        assertEquals(film.getName(), validFilm.getName());
        assertEquals(film.getDescription(), validFilm.getDescription());
        assertEquals(film.getReleaseDate(), validFilm.getReleaseDate());
        assertEquals(film.getDuration(), validFilm.getDuration());
    }

    @Test
    void shouldThrowExceptionWhenFilmNotFound() {
        Film wrongFilm = Film.builder()
                .id(9999L)
                .name("film")
                .description("description")
                .releaseDate(LocalDate.of(2000, 12, 28))
                .duration(100)
                .build();

        NotFoundException exception = assertThrows(
                NotFoundException.class, () -> filmController.updateFilm(wrongFilm));

        assertEquals("Фильм с id = 9999 не найден.", exception.getMessage());
        assertFalse(filmController.getFilms().size() > 1);
        assertFalse(filmController.getFilms().getFirst().getName().isEmpty());
        assertNotEquals(wrongFilm, filmController.getFilms().getFirst());
    }

    @Test
    void shouldReturnCollectionFilmsInRightOrder() {
        setUpUsers();
        setUpFilms();

        filmController.addLike(1L, 1L);

        filmController.addLike(2L, 1L);
        filmController.addLike(2L, 2L);
        filmController.addLike(2L, 3L);

        filmController.addLike(3L, 1L);
        filmController.addLike(3L, 2L);
        List<Film> films = filmController.getMostPopularFilms(3);

        assertEquals("film2", films.get(0).getName());
        assertEquals("film3", films.get(1).getName());
        assertEquals("default-film", films.get(2).getName());
    }

    @Test
    void shouldAddLike() {
        setUpUsers();

        filmController.addLike(1L, 1L);
        filmController.addLike(1L, 3L);
        Set<Long> likes = filmService.getLikes(1L);

        assertEquals(2, likes.size());
        assertTrue(filmService.getLikes(1L).contains(1L));
        assertTrue(filmService.getLikes(1L).contains(3L));
    }

    @Test
    void shouldRemoveLike() {
        setUpUsers();

        filmController.addLike(1L, 1L);
        filmController.addLike(1L, 3L);
        filmController.deleteLike(1L, 3L);

        assertEquals(1, filmService.getLikes(1L).size());
        assertFalse(filmService.getLikes(1L).contains(3L));
        assertTrue(filmService.getLikes(1L).contains(1L));
    }

    @Test
    void shouldThrowNotFoundWhenFilmNotFoundWhileAddingLike() {
        setUpUsers();

        NotFoundException exception = assertThrows(
                NotFoundException.class, () -> filmController.addLike(9999L, 1L));

        assertEquals("Фильм с id = " + 9999L + " не найден.", exception.getMessage());
    }

    @Test
    void shouldThrowNotFoundWhenUserNotFoundWhileAddingLike() {
        NotFoundException exception = assertThrows(
                NotFoundException.class, () -> filmController.addLike(1L, 9999L));

        assertEquals("Пользователь с id = 9999 не найден.", exception.getMessage());
    }

    @Test
    void shouldThrowNotFoundWhenFilmNotFoundWhileDeletingLike() {
        setUpUsers();

        NotFoundException exception = assertThrows(
                NotFoundException.class, () -> filmController.deleteLike(9999L, 1L));

        assertEquals("Фильм с id = " + 9999L + " не найден.", exception.getMessage());
    }

    @Test
    void shouldThrowNotFoundWhenUserNotFoundWhileDeletingLike() {
        NotFoundException exception = assertThrows(
                NotFoundException.class, () -> filmController.deleteLike(1L, 9999L));

        assertEquals("Пользователь с id = 9999 не найден.", exception.getMessage());
    }

    void setUpUsers() {
        User user1 = User.builder()
                .email("test@email.com")
                .login("test-login")
                .name("name1")
                .birthday(LocalDate.of(2000, 12, 12))
                .build();
        User user2 = User.builder()
                .email("test@email.com")
                .login("test-login")
                .name("name2")
                .birthday(LocalDate.of(2000, 12, 12))
                .build();
        User user3 = User.builder()
                .email("test@email.com")
                .login("test-login")
                .name("name3")
                .birthday(LocalDate.of(2000, 12, 12))
                .build();
        userService.addUser(user1);
        userService.addUser(user2);
        userService.addUser(user3);
    }

    void setUpFilms() {
        Film film2 = Film.builder()
                .name("film2")
                .description("_".repeat(200))
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(100)
                .build();
        Film film3 = Film.builder()
                .name("film3")
                .description("_".repeat(200))
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(100)
                .build();
        filmController.addFilm(film2);
        filmController.addFilm(film3);
    }
}
