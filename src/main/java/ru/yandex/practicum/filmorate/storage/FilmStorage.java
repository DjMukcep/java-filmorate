package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.model.film.Rating;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface FilmStorage {

    Film addFilm(Film film);

    Film updateFilm(Film film);

    void addLike(Film film, Long userId);

    void removeLike(Long filmId, Long userId);

    List<Film> getMostPopularFilms(int count);

    List<Film> getFilms();

    Optional<Film> getFilmById(Long filmId);

    Set<Long> getLikes(Long filmId);

    Rating getFilmRatingById(Long mpaId);

    List<Rating> getFilmRatings();

    List<Genre> getGenres();

    Genre getGenreById(Long genreId);
}
