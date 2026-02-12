package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private final List<Film> films = new ArrayList<>();

    @GetMapping
    public List<Film> getFilms() {
        return List.copyOf(films);
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film, Errors errors) {
        validateFilm(film, errors);

        int id = films.size() + 1;
        film.setId(id);
        films.add(film);
        log.info("Added film: {}", film.getName());
        return film;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film newFilm, Errors errors) {
        validateFilm(newFilm, errors);

        return films.stream()
                .filter(film -> film.getId().equals(newFilm.getId()))
                .findFirst()
                .map(film -> processUpdateFilm(film, newFilm))
                .orElseThrow(() -> new ValidationException("Film not found."));
    }

    private Film processUpdateFilm(Film oldFilm, Film newFilm) {
        int index = films.indexOf(oldFilm);
        newFilm.setId(oldFilm.getId());
        films.set(index, newFilm);
        log.info("Updated film with id: {}", newFilm.getId());
        return newFilm;
    }

    private void validateFilm(Film film, Errors errors) {
        checkTitle(errors);
        checkDescription(errors);
        checkReleaseDate(film);
        checkDuration(film);
    }

    private void checkDuration(Film film) {
        if (isWrongDuration(film)) {
            processError("Film validation didn't pass - wrong duration.");
        }
    }

    private boolean isWrongDuration(Film film) {
        return film.getDuration() == null
                || film.getDuration() <= 0;
    }

    private void checkReleaseDate(Film film) {
        if (isWrongDate(film)) {
            processError("Film validation didn't pass - wrong release date.");
        }
    }

    private boolean isWrongDate(Film film) {
        return film.getReleaseDate() == null
                || film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28));
    }

    private void checkDescription(Errors errors) {
        if (errors.hasFieldErrors("description")) {
            processError("Film validation didn't pass - wrong description.");
        }
    }

    private void checkTitle(Errors errors) {
        if (errors.hasFieldErrors("name")) {
            processError("Film validation didn't pass - wrong name.");
        }
    }

    private void processError(String message) {
        log.error(message);
        throw new ValidationException(message);
    }
}
