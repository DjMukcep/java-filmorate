package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

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

        Errors errors = new BeanPropertyBindingResult(film, "film");
        filmController.addFilm(film, errors);
    }

    @Test
    void shouldAddFilmWhenFilmValid() {
        Film validFilm = Film.builder()
                .name("film")
                .description("_".repeat(200))
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(100)
                .build();

        Errors errors = new BeanPropertyBindingResult(validFilm, "film");
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        Film film = filmController.addFilm(validFilm, errors);

        assertTrue(violations.isEmpty());
        assertNotNull(film.getId());
        assertEquals(film.getName(), validFilm.getName());
        assertEquals(film.getDescription(), validFilm.getDescription());
        assertEquals(film.getReleaseDate(), validFilm.getReleaseDate());
        assertEquals(film.getDuration(), validFilm.getDuration());
        assertTrue(filmController.getFilms().contains(validFilm));
    }


    @Test
    void shouldNotAddFilmWhenFilmNameEmpty() {
        String wrongName = "";
        Film wrongFilm = Film.builder()
                .name(wrongName)
                .description("description")
                .releaseDate(LocalDate.of(1999, 2, 2))
                .duration(100)
                .build();

        Errors errors = processErrors(wrongFilm);
        ValidationException exception = assertThrows(
                ValidationException.class, () -> filmController.addFilm(wrongFilm, errors));

        assertEquals("Film validation didn't pass - wrong name.", exception.getMessage());
        assertFalse(filmController.getFilms().size() > 1);
        assertNotEquals(wrongName, filmController.getFilms().getFirst().getName());
    }

    @Test
    void shouldNotAddFilmWhenFilmNameIsNull() {
        Film wrongFilm = Film.builder()
                .name(null)
                .description("description")
                .releaseDate(LocalDate.of(1999, 2, 2))
                .duration(100)
                .build();

        Errors errors = processErrors(wrongFilm);
        ValidationException exception = assertThrows(
                ValidationException.class, () -> filmController.addFilm(wrongFilm, errors));

        assertEquals("Film validation didn't pass - wrong name.", exception.getMessage());
        assertFalse(filmController.getFilms().size() > 1);
        assertNotEquals(null, filmController.getFilms().getFirst().getName());
    }

    @Test
    void shouldNotAddFilmWhenDescriptionOverMaxLength() {
        String wrongDescription = "_".repeat(201);
        Film wrongFilm = Film.builder()
                .name("film")
                .description(wrongDescription)
                .releaseDate(LocalDate.of(1999, 2, 2))
                .duration(100)
                .build();

        Errors errors = processErrors(wrongFilm);
        ValidationException exception = assertThrows(
                ValidationException.class, () -> filmController.addFilm(wrongFilm, errors));

        assertEquals("Film validation didn't pass - wrong description.", exception.getMessage());
        assertFalse(filmController.getFilms().size() > 1);
        assertNotEquals(wrongDescription, filmController.getFilms().getFirst().getDescription());
    }

    @Test
    void shouldNotAddFilmWhenDescriptionIsNull() {
        Film wrongFilm = Film.builder()
                .name("film")
                .description(null)
                .releaseDate(LocalDate.of(1999, 2, 2))
                .duration(100)
                .build();

        Errors errors = processErrors(wrongFilm);
        ValidationException exception = assertThrows(
                ValidationException.class, () -> filmController.addFilm(wrongFilm, errors));

        assertEquals("Film validation didn't pass - wrong description.", exception.getMessage());
        assertFalse(filmController.getFilms().size() > 1);
        assertNotEquals(null, filmController.getFilms().getFirst().getDescription());
    }

    @Test
    void shouldNotAddFilmWhenReleaseDateBeforeMinDate() {
        LocalDate wrongReleaseDate = LocalDate.of(1895, 12, 27);
        Film wrongFilm = Film.builder()
                .name("film")
                .description("description")
                .releaseDate(wrongReleaseDate)
                .duration(100)
                .build();

        Errors errors = processErrors(wrongFilm);
        ValidationException exception = assertThrows(
                ValidationException.class, () -> filmController.addFilm(wrongFilm, errors));

        assertEquals("Film validation didn't pass - wrong release date.", exception.getMessage());
        assertFalse(filmController.getFilms().size() > 1);
        assertNotEquals(filmController.getFilms().getFirst().getReleaseDate(), wrongReleaseDate);
    }

    @Test
    void shouldNotAddFilmWhenReleaseDateIsNull() {
        Film wrongFilm = Film.builder()
                .name("film")
                .description("description")
                .releaseDate(null)
                .duration(100)
                .build();

        Errors errors = processErrors(wrongFilm);
        ValidationException exception = assertThrows(
                ValidationException.class, () -> filmController.addFilm(wrongFilm, errors));

        assertEquals("Film validation didn't pass - wrong release date.", exception.getMessage());
        assertFalse(filmController.getFilms().size() > 1);
        assertNotEquals(null, filmController.getFilms().getFirst().getReleaseDate());
    }

    @Test
    void shouldNotAddFilmWhenDurationIsZero() {
        Integer wrongDuration = 0;
        Film wrongFilm = Film.builder()
                .name("film")
                .description("description")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(wrongDuration)
                .build();

        Errors errors = processErrors(wrongFilm);
        ValidationException exception = assertThrows(
                ValidationException.class, () -> filmController.addFilm(wrongFilm, errors));

        assertEquals("Film validation didn't pass - wrong duration.", exception.getMessage());
        assertFalse(filmController.getFilms().size() > 1);
        assertNotEquals(wrongDuration, filmController.getFilms().getFirst().getDuration());
    }

    @Test
    void shouldNotAddFilmWhenDurationIsNegative() {
        Integer wrongDuration = -100;
        Film wrongFilm = Film.builder()
                .name("film")
                .description("description")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(wrongDuration)
                .build();

        Errors errors = processErrors(wrongFilm);
        ValidationException exception = assertThrows(
                ValidationException.class, () -> filmController.addFilm(wrongFilm, errors));

        assertEquals("Film validation didn't pass - wrong duration.", exception.getMessage());
        assertFalse(filmController.getFilms().size() > 1);
        assertNotEquals(wrongDuration, filmController.getFilms().getFirst().getDuration());
    }

    @Test
    void shouldNotAddFilmWhenDurationIsNull() {
        Film wrongFilm = Film.builder()
                .name("film")
                .description("description")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(null)
                .build();

        Errors errors = processErrors(wrongFilm);
        ValidationException exception = assertThrows(
                ValidationException.class, () -> filmController.addFilm(wrongFilm, errors));

        assertEquals("Film validation didn't pass - wrong duration.", exception.getMessage());
        assertFalse(filmController.getFilms().size() > 1);
        assertNotEquals(null, filmController.getFilms().getFirst().getDuration());
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

        Errors errors = new BeanPropertyBindingResult(validFilm, "film");
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        Film film = filmController.updateFilm(validFilm, errors);

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

        Errors errors = processErrors(wrongFilm);
        NotFoundException exception = assertThrows(
                NotFoundException.class, () -> filmController.updateFilm(wrongFilm, errors));

        assertEquals("Film with id = 9999 not found.", exception.getMessage());
        assertFalse(filmController.getFilms().size() > 1);
        assertFalse(filmController.getFilms().getFirst().getName().isEmpty());
        assertNotEquals(wrongFilm, filmController.getFilms().getFirst());
    }

    @Test
    void shouldNotUpdateFilmWhenFilmNameEmpty() {
        String wrongName = "";
        Film wrongFilm = Film.builder()
                .name(wrongName)
                .description("description")
                .releaseDate(LocalDate.of(1999, 2, 2))
                .duration(100)
                .build();

        Errors errors = processErrors(wrongFilm);
        ValidationException exception = assertThrows(
                ValidationException.class, () -> filmController.updateFilm(wrongFilm, errors));

        assertEquals("Film validation didn't pass - wrong name.", exception.getMessage());
        assertFalse(filmController.getFilms().size() > 1);
        assertFalse(filmController.getFilms().getFirst().getName().isEmpty());
        assertNotEquals(wrongName, filmController.getFilms().getFirst().getName());
    }

    @Test
    void shouldNotUpdateFilmWhenDescriptionOverMaxLength() {
        String wrongDescription = "_".repeat(201);
        Film wrongFilm = Film.builder()
                .name("film")
                .description(wrongDescription)
                .releaseDate(LocalDate.of(1999, 2, 2))
                .duration(100)
                .build();

        Errors errors = processErrors(wrongFilm);
        ValidationException exception = assertThrows(
                ValidationException.class, () -> filmController.updateFilm(wrongFilm, errors));

        assertEquals("Film validation didn't pass - wrong description.", exception.getMessage());
        assertFalse(filmController.getFilms().size() > 1);
        assertNotEquals(filmController.getFilms().getFirst().getDescription(), wrongDescription);
    }

    @Test
    void shouldNotUpdateFilmWhenReleaseDateBeforeMinDate() {
        LocalDate wrongReleaseDate = LocalDate.of(1895, 12, 27);
        Film wrongFilm = Film.builder()
                .name("film")
                .description("description")
                .releaseDate(wrongReleaseDate)
                .duration(100)
                .build();

        Errors errors = processErrors(wrongFilm);
        ValidationException exception = assertThrows(
                ValidationException.class, () -> filmController.updateFilm(wrongFilm, errors));

        assertEquals("Film validation didn't pass - wrong release date.", exception.getMessage());
        assertFalse(filmController.getFilms().size() > 1);
        assertNotEquals(filmController.getFilms().getFirst().getReleaseDate(), wrongReleaseDate);
    }

    @Test
    void shouldNotUpdateFilmWhenDurationNotPositive() {
        Integer wrongDuration = 0;
        Film wrongFilm = Film.builder()
                .name("film")
                .description("description")
                .releaseDate(LocalDate.of(1999, 2, 2))
                .duration(wrongDuration)
                .build();

        Errors errors = processErrors(wrongFilm);
        ValidationException exception = assertThrows(
                ValidationException.class, () -> filmController.updateFilm(wrongFilm, errors));

        assertEquals("Film validation didn't pass - wrong duration.", exception.getMessage());
        assertFalse(filmController.getFilms().size() > 1);
        assertNotEquals(wrongDuration, filmController.getFilms().getFirst().getDuration());
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
        assertEquals("default-film",films.get(2).getName());
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

        assertEquals("Film with id = " + 9999L + " not found.", exception.getMessage());
    }

    @Test
    void shouldThrowNotFoundWhenUserNotFoundWhileAddingLike() {
        NotFoundException exception = assertThrows(
                NotFoundException.class, () -> filmController.addLike(1L, 9999L));

        assertEquals("User with id = " + 9999L + " not found.", exception.getMessage());
    }

    @Test
    void shouldThrowNotFoundWhenFilmNotFoundWhileDeletingLike() {
        setUpUsers();

        NotFoundException exception = assertThrows(
                NotFoundException.class, () -> filmController.deleteLike(9999L, 1L));

        assertEquals("Film with id = " + 9999L + " not found.", exception.getMessage());
    }

    @Test
    void shouldThrowNotFoundWhenUserNotFoundWhileDeletingLike() {
        NotFoundException exception = assertThrows(
                NotFoundException.class, () -> filmController.deleteLike(1L, 9999L));

        assertEquals("User with id = " + 9999L + " not found.", exception.getMessage());
    }


    Errors processErrors(Film wrongFilm) {
        Errors errors = new BeanPropertyBindingResult(wrongFilm, "film");
        for (ConstraintViolation<Film> v : validator.validate(wrongFilm)) {
            errors.rejectValue(v.getPropertyPath().toString(), "invalid", v.getMessage());
        }
        return errors;
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
        Errors errors1 = new BeanPropertyBindingResult(user1, "user");
        Errors errors2 = new BeanPropertyBindingResult(user2, "user");
        Errors errors3 = new BeanPropertyBindingResult(user3, "user");
        userService.addUser(user1, errors1);
        userService.addUser(user2, errors2);
        userService.addUser(user3, errors3);
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
        Errors errors2 = new BeanPropertyBindingResult(film2, "film");
        Errors errors3 = new BeanPropertyBindingResult(film3, "film");
        filmController.addFilm(film2, errors2);
        filmController.addFilm(film3, errors3);
    }
}
