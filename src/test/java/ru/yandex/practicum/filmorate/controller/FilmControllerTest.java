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
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Set;


import static org.junit.jupiter.api.Assertions.*;

public class FilmControllerTest {

    private FilmController controller;
    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
        controller = new FilmController();
        Film film = Film.builder()
                .id(1)
                .name("default-film")
                .description("default-description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(100)
                .build();
        controller.getFilms().add(film);
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
        Film film = controller.addFilm(validFilm, errors);

        assertTrue(violations.isEmpty());
        assertNotNull(film.getId());
        assertEquals(film.getName(), validFilm.getName());
        assertEquals(film.getDescription(), validFilm.getDescription());
        assertEquals(film.getReleaseDate(), validFilm.getReleaseDate());
        assertEquals(film.getDuration(), validFilm.getDuration());
        assertTrue(controller.getFilms().contains(validFilm));
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
                ValidationException.class, () -> controller.addFilm(wrongFilm, errors));

        assertEquals("Film validation didn't pass - wrong name.", exception.getMessage());
        assertFalse(controller.getFilms().size() > 1);
        assertNotEquals(wrongName, controller.getFilms().getFirst().getName());
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
                ValidationException.class, () -> controller.addFilm(wrongFilm, errors));

        assertEquals("Film validation didn't pass - wrong name.", exception.getMessage());
        assertFalse(controller.getFilms().size() > 1);
        assertNotEquals(null, controller.getFilms().getFirst().getName());
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
                ValidationException.class, () -> controller.addFilm(wrongFilm, errors));

        assertEquals("Film validation didn't pass - wrong description.", exception.getMessage());
        assertFalse(controller.getFilms().size() > 1);
        assertNotEquals(wrongDescription, controller.getFilms().getFirst().getDescription());
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
                ValidationException.class, () -> controller.addFilm(wrongFilm, errors));

        assertEquals("Film validation didn't pass - wrong description.", exception.getMessage());
        assertFalse(controller.getFilms().size() > 1);
        assertNotEquals(null, controller.getFilms().getFirst().getDescription());
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
                ValidationException.class, () -> controller.addFilm(wrongFilm, errors));

        assertEquals("Film validation didn't pass - wrong release date.", exception.getMessage());
        assertFalse(controller.getFilms().size() > 1);
        assertNotEquals(controller.getFilms().getFirst().getReleaseDate(), wrongReleaseDate);
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
                ValidationException.class, () -> controller.addFilm(wrongFilm, errors));

        assertEquals("Film validation didn't pass - wrong release date.", exception.getMessage());
        assertFalse(controller.getFilms().size() > 1);
        assertNotEquals(null, controller.getFilms().getFirst().getReleaseDate());
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
                ValidationException.class, () -> controller.addFilm(wrongFilm, errors));

        assertEquals("Film validation didn't pass - wrong duration.", exception.getMessage());
        assertFalse(controller.getFilms().size() > 1);
        assertNotEquals(wrongDuration, controller.getFilms().getFirst().getDuration());
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
                ValidationException.class, () -> controller.addFilm(wrongFilm, errors));

        assertEquals("Film validation didn't pass - wrong duration.", exception.getMessage());
        assertFalse(controller.getFilms().size() > 1);
        assertNotEquals(wrongDuration, controller.getFilms().getFirst().getDuration());
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
                ValidationException.class, () -> controller.addFilm(wrongFilm, errors));

        assertEquals("Film validation didn't pass - wrong duration.", exception.getMessage());
        assertFalse(controller.getFilms().size() > 1);
        assertNotEquals(null, controller.getFilms().getFirst().getDuration());
    }

    @Test
    void shouldUpdateFilmWhenFilmFound() {
        Film validFilm = Film.builder()
                .id(1)
                .name("film")
                .description("description")
                .releaseDate(LocalDate.of(2000, 12, 28))
                .duration(100)
                .build();

        Errors errors = new BeanPropertyBindingResult(validFilm, "film");
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        Film film = controller.updateFilm(validFilm, errors);

        assertNotNull(film);
        assertTrue(violations.isEmpty());
        assertTrue(controller.getFilms().contains(film));
        assertFalse(controller.getFilms().size() > 1);

        assertEquals(1, film.getId());
        assertEquals(film.getName(), validFilm.getName());
        assertEquals(film.getDescription(), validFilm.getDescription());
        assertEquals(film.getReleaseDate(), validFilm.getReleaseDate());
        assertEquals(film.getDuration(), validFilm.getDuration());
    }

    @Test
    void shouldThrowExceptionWhenFilmNotFound() {
        Film wrongFilm = Film.builder()
                .id(9999)
                .name("film")
                .description("description")
                .releaseDate(LocalDate.of(2000, 12, 28))
                .duration(100)
                .build();

        Errors errors = processErrors(wrongFilm);
        ValidationException exception = assertThrows(
                ValidationException.class, () -> controller.updateFilm(wrongFilm, errors));

        assertEquals("Film not found.", exception.getMessage());
        assertFalse(controller.getFilms().size() > 1);
        assertFalse(controller.getFilms().getFirst().getName().isEmpty());
        assertNotEquals(wrongFilm, controller.getFilms().getFirst());
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
                ValidationException.class, () -> controller.updateFilm(wrongFilm, errors));

        assertEquals("Film validation didn't pass - wrong name.", exception.getMessage());
        assertFalse(controller.getFilms().size() > 1);
        assertFalse(controller.getFilms().getFirst().getName().isEmpty());
        assertNotEquals(wrongName, controller.getFilms().getFirst().getName());
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
                ValidationException.class, () -> controller.updateFilm(wrongFilm, errors));

        assertEquals("Film validation didn't pass - wrong description.", exception.getMessage());
        assertFalse(controller.getFilms().size() > 1);
        assertNotEquals(controller.getFilms().getFirst().getDescription(), wrongDescription);
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
                ValidationException.class, () -> controller.updateFilm(wrongFilm, errors));

        assertEquals("Film validation didn't pass - wrong release date.", exception.getMessage());
        assertFalse(controller.getFilms().size() > 1);
        assertNotEquals(controller.getFilms().getFirst().getReleaseDate(), wrongReleaseDate);
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
                ValidationException.class, () -> controller.updateFilm(wrongFilm, errors));

        assertEquals("Film validation didn't pass - wrong duration.", exception.getMessage());
        assertFalse(controller.getFilms().size() > 1);
        assertNotEquals(controller.getFilms().getFirst().getDuration(), wrongDuration);
    }


    Errors processErrors(Film wrongFilm) {
        Errors errors = new BeanPropertyBindingResult(wrongFilm, "film");
        for (ConstraintViolation<Film> v : validator.validate(wrongFilm)) {
            errors.rejectValue(v.getPropertyPath().toString(), "invalid", v.getMessage());
        }
        return errors;
    }
}
