package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserService userService;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;

    public List<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film getFilmById(Long id) {
        return findFilmOrThrow(id);
    }

    public Film addFilm(Film film) {
        Rating rating = validateFilm(film);

        Film inStorageFilm = filmStorage.addFilm(film);
        inStorageFilm.setMpa(rating);
        log.info("New film: {}", film);
        return inStorageFilm;
    }

    public Film updateFilm(Film newFilm) {
        Rating rating = validateFilm(newFilm);
        findFilmOrThrow(newFilm.getId());

        Film updatedFilm = filmStorage.updateFilm(newFilm);
        updatedFilm.setMpa(rating);
        log.info("Update film: {}", newFilm);
        return updatedFilm;
    }

    public void addLike(Long filmId, Long userId) {
        Film film = findFilmOrThrow(filmId);
        User user = userService.getUserById(userId);
        Set<Long> filmLikes = filmStorage.getLikes(filmId);

        if (filmLikes.contains(user.getId())) {
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
        return findRatingOrThrow(mpaId);
    }

    public List<Rating> getAllFilmRatings() {
        return mpaStorage.getFilmRatings();
    }

    public Genre getGenreById(Long genreId) {
        return findGenreOrThrow(genreId);
    }

    public List<Genre> getAllGenres() {
        return genreStorage.getGenres();
    }


    private Film findFilmOrThrow(Long filmId) {
        return filmStorage.getFilmById(filmId).orElseThrow(() -> {
            log.error("Error checking film with id: {}", filmId);
            return new NotFoundException("Фильм с id = " + filmId + " не найден.");
        });
    }

    private Rating findRatingOrThrow(Long ratingId) {
        return mpaStorage.getFilmRatingById(ratingId).orElseThrow(() -> {
            log.error("Error checking rating with id: {}", ratingId);
            return new NotFoundException("Рейтинг с id = " + ratingId + " не найден.");
        });
    }

    private Genre findGenreOrThrow(Long genreId) {
        return genreStorage.getGenreById(genreId).orElseThrow(() -> {
            log.error("Error checking genre with id: {}", genreId);
            return new NotFoundException("Жанр с id = " + genreId + " не найден.");
        });
    }

    private void checkPresence(Long filmId, Long userId) {
        findFilmOrThrow(filmId);
        userService.getUserById(userId);
    }

    private Rating validateFilm(Film film) {
        checkReleaseDate(film);
        validateGenres(film);
        return findRatingOrThrow(film.getMpa().getId());
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

    private void validateGenres(Film film) {
        List<Genre> genres = genreStorage.getGenres();

        Set<Long> incomeGenreIds = film.getGenres()
                .stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());

        Set<Long> existsGenreIds = genres.stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());

        Set<Long> missingIds = incomeGenreIds.stream()
                .filter(id -> !existsGenreIds.contains(id))
                .collect(Collectors.toSet());

        if (!missingIds.isEmpty()) {
            log.error("Genres not found with ids: {}", missingIds);
            throw new NotFoundException("Жанры c id: " + missingIds + " не найдены.");
        }
    }
}
