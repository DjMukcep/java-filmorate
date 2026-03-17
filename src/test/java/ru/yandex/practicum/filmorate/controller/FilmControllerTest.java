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
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;


import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
@ActiveProfiles("test")
public class FilmControllerTest {

    @Autowired
    private FilmController filmController;

    @Autowired
    private FilmService filmService;

    @Autowired
    private UserService userService;

    private Validator validator;

    private Film createdFilm;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }

        createdFilm = Film.builder()
                .name("default-film")
                .description("default-description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .mpa(new Rating(1L, "G"))
                .duration(100)
                .build();

        filmController.addFilm(createdFilm);
    }

    @Test
    void shouldAddFilmWhenFilmValid() {
        Film validFilm = Film.builder()
                .name("film")
                .description("description")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .mpa(new Rating(2L, ""))
                .genres(Set.of(new Genre(1L, "")))
                .duration(100)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        Film film = filmController.addFilm(validFilm);

        assertTrue(violations.isEmpty());
        assertNotNull(film.getId());
        assertEquals("film", film.getName());
        assertEquals("description", film.getDescription());
        assertEquals("1895-12-28", film.getReleaseDate().toString());
        assertEquals(100, film.getDuration());
        assertEquals("", film.getMpa().getName());
        assertEquals(2L, film.getMpa().getId());
        assertEquals(1, film.getGenres().size());
        assertEquals("", film.getGenres().iterator().next().getName());
    }


    @Test
    void shouldHaveValidationErrorsWhenFilmNameEmpty() {
        String wrongName = "";
        Film wrongFilm = Film.builder()
                .name(wrongName)
                .description("description")
                .releaseDate(LocalDate.of(1999, 2, 2))
                .mpa(new Rating(2L, ""))
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
                .mpa(new Rating(1L, ""))
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
                .mpa(new Rating(1L, ""))
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
                .mpa(new Rating(1L, ""))
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
                .mpa(new Rating(1L, "G"))
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
                .mpa(new Rating(1L, "G"))
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
                .mpa(new Rating(1L, "G"))
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
                .mpa(new Rating(1L, "G"))
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
                .mpa(new Rating(1L, "G"))
                .duration(null)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(wrongFilm);
        String message = violations.iterator().next().getMessage();

        assertEquals("Длительность фильма должна быть указана.", message);
    }

    @Test
    void shouldUpdateFilmWhenFilmFound() {
        Film validFilm = Film.builder()
                .id(createdFilm.getId())
                .name("film")
                .description("description")
                .releaseDate(LocalDate.of(2000, 12, 28))
                .mpa(new Rating(1L, "G"))
                .duration(100)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        Film film = filmController.updateFilm(validFilm);

        assertNotNull(film);
        assertTrue(violations.isEmpty());
        assertTrue(filmController.getFilms().contains(film));
        assertFalse(filmController.getFilms().size() > 1);

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
                .mpa(new Rating(1L, "G"))
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
        List<User> users = setUpUsers();
        List<Film> testFilms = setUpFilms();
        long firstUserId = users.get(0).getId();
        long secondUserId = users.get(1).getId();
        long lastUserId = users.get(2).getId();
        long firstFilmId = createdFilm.getId();
        long secondFilmId = testFilms.get(0).getId();
        long lastFilmId = testFilms.get(1).getId();

        filmController.addLike(firstFilmId, firstUserId);

        filmController.addLike(secondFilmId, firstUserId);
        filmController.addLike(secondFilmId, secondUserId);
        filmController.addLike(secondFilmId, lastUserId);

        filmController.addLike(lastFilmId, firstUserId);
        filmController.addLike(lastFilmId, secondUserId);
        List<Film> films = filmController.getMostPopularFilms(3);

        assertEquals("film2", films.get(0).getName());
        assertEquals("film3", films.get(1).getName());
        assertEquals("default-film", films.get(2).getName());
    }

    @Test
    void shouldAddLike() {
        List<User> users = setUpUsers();
        long firstUserId = users.get(0).getId();
        long lastUserId = users.get(2).getId();
        long firstFilmId = createdFilm.getId();

        filmController.addLike(firstFilmId, firstUserId);
        filmController.addLike(firstFilmId, lastUserId);
        Set<Long> likes = filmService.getLikes(firstFilmId);

        assertEquals(2, likes.size());
        assertTrue(filmService.getLikes(firstFilmId).contains(firstUserId));
        assertTrue(filmService.getLikes(firstFilmId).contains(lastUserId));
    }

    @Test
    void shouldRemoveLike() {
        List<User> users = setUpUsers();
        long filmId = createdFilm.getId();
        long lastUserId = users.getLast().getId();
        long firstUserId = users.getFirst().getId();

        filmController.addLike(filmId, firstUserId);
        filmController.addLike(filmId, lastUserId);
        filmController.deleteLike(filmId, lastUserId);

        assertEquals(1, filmService.getLikes(filmId).size());
        assertFalse(filmService.getLikes(filmId).contains(lastUserId));
        assertTrue(filmService.getLikes(filmId).contains(firstUserId));
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
                NotFoundException.class, () -> filmController.addLike(createdFilm.getId(), 9999L));

        assertEquals("Пользователь с id: [9999] не найден.", exception.getMessage());
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
                NotFoundException.class, () -> filmController.deleteLike(createdFilm.getId(), 9999L));

        assertEquals("Пользователь с id: [9999] не найден.", exception.getMessage());
    }

    List<User> setUpUsers() {
        User u1 = User.builder()
                .email("test@email.com")
                .login("test-login")
                .name("name1")
                .birthday(LocalDate.of(2000, 12, 12))
                .build();
        User u2 = User.builder()
                .email("test@email.com")
                .login("test-login")
                .name("name2")
                .birthday(LocalDate.of(2000, 12, 12))
                .build();
        User u3 = User.builder()
                .email("test@email.com")
                .login("test-login")
                .name("name3")
                .birthday(LocalDate.of(2000, 12, 12))
                .build();
        User user1 = userService.addUser(u1);
        User user2 = userService.addUser(u2);
        User user3 = userService.addUser(u3);
        return Arrays.asList(user1, user2, user3);
    }

    List<Film> setUpFilms() {
        Film f2 = Film.builder()
                .name("film2")
                .description("_".repeat(200))
                .releaseDate(LocalDate.of(1895, 12, 28))
                .mpa(new Rating(1L, ""))
                .duration(100)
                .build();
        Film f3 = Film.builder()
                .name("film3")
                .description("_".repeat(200))
                .releaseDate(LocalDate.of(1895, 12, 28))
                .mpa(new Rating(3L, ""))
                .duration(100)
                .build();
        Film film2 = filmController.addFilm(f2);
        Film film3 = filmController.addFilm(f3);
        return Arrays.asList(film2, film3);
    }
}
