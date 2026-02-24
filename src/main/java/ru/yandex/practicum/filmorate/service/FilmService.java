package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserService userService;

    public FilmService(FilmStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }


    public List<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film addFilm(Film film, Errors errors) {
        validateFilm(film, errors);

        Long id = (long) (getFilms().size() + 1);
        film.setId(id);
        filmStorage.saveFilm(film);
        log.info("Added film: {}", film.getName());
        return film;
    }

    public Film updateFilm(Film newFilm, Errors errors) {
        validateFilm(newFilm, errors);

        return filmStorage.getFilmById(newFilm.getId())
                .map(film -> processUpdateFilm(newFilm))
                .orElseThrow(() -> {
                    log.error("Error updating film: {}", newFilm.getName());
                    return new NotFoundException("Film with id = " + newFilm.getId() + " not found.");
                });
    }

    public void addLike(Long filmId, Long userId) {
        checkPresence(filmId, userId);
        filmStorage.addLike(filmId, userId);
    }

    public void deleteLike(Long filmId, Long userId) {
        checkPresence(filmId, userId);
        filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getMostPopularFilms(Integer count) {
        if (count == null) {
            count = 10;
        }
        return filmStorage.getMostPopularFilms(count);
    }

    public Set<Long> getLikes(Long filmId) {
        return filmStorage.getLikes(filmId);
    }

    private void checkPresence(Long filmId, Long userId) {
        checkFilmPresence(filmId);
        userService.checkUserPresence(userId);
    }

    private void checkFilmPresence(Long filmId) {
        filmStorage.getFilmById(filmId).orElseThrow(() -> {
            log.error("Error checking film with id: {}", filmId);
            return new NotFoundException("Film with id = " + filmId + " not found.");
        });
    }

    private Film processUpdateFilm(Film newFilm) {
        filmStorage.saveFilm(newFilm);
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
