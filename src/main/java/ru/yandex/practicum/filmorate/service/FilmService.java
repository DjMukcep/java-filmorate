package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.model.film.Rating;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserService userService;


    public FilmService(@Qualifier("DB") FilmStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public List<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film getFilmById(Long id) {
        return findFilmOrThrow(id);
    }

    public Film addFilm(Film film) {
        validateFilm(film);

        log.info("Added film: {}", film.getName());
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film newFilm) {
        validateFilm(newFilm);
        findFilmOrThrow(newFilm.getId());

        log.info("Updated film with id: {}", newFilm.getId());
        return filmStorage.updateFilm(newFilm);
    }

    public void addLike(Long filmId, Long userId) {
        Film film = findFilmOrThrow(filmId);
        User user = userService.findUserOrThrow(userId);

        if (film.getUserIds().contains(user.getId())) {
            throw new ValidationException(
                    "Лайк с id: " + userId + ", уже имеется. Разрешен только 1 лайк от пользователя");
        }

        filmStorage.addLike(film, userId);
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

    public Rating getFilmRatingById(Long mpaId) {
        return filmStorage.getFilmRatingById(mpaId);
    }

    public List<Rating> getAllFilmRatings() {
        return filmStorage.getFilmRatings();
    }

    public Genre getGenreById(Long genreId) {
        return filmStorage.getGenreById(genreId);
    }

    public List<Genre> getAllGenres() {
        return filmStorage.getGenres();
    }

    private void checkPresence(Long filmId, Long userId) {
        findFilmOrThrow(filmId);
        userService.findUserOrThrow(userId);
    }

    private Film findFilmOrThrow(Long filmId) {
        return filmStorage.getFilmById(filmId).orElseThrow(() -> {
            log.error("Error checking film with id: {}", filmId);
            return new NotFoundException("Фильм с id = " + filmId + " не найден.");
        });
    }

    private void validateFilm(Film film) {
        checkReleaseDate(film);
    }

    private void checkReleaseDate(Film film) {
        if (isWrongDate(film)) {
            log.error("Film with id = {}. Wrong release date: {}", film.getId(), film.getReleaseDate());
            throw new ValidationException("Самая ранняя разрешенная дата фильма: 1895-12-28");
        }
    }

    private boolean isWrongDate(Film film) {
        return film.getReleaseDate() == null
                || film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28));
    }
}
