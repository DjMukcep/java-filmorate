package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.film.Film;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface FilmStorage {

    void saveFilm(Film film);

    void addLike(Long filmId, Long userId);

    void removeLike(Long filmId, Long userId);

    List<Film> getMostPopularFilms(int count);

    List<Film> getFilms();

    Optional<Film> getFilmById(Long filmId);

    Set<Long> getLikes(Long filmId);
}
