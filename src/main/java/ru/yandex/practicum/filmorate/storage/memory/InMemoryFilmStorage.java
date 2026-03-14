package ru.yandex.practicum.filmorate.storage.memory;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.model.film.Rating;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.*;

@Component
@Qualifier("MEM")
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();
    private long lastId;

    @Override
    public List<Film> getFilms() {
        return List.copyOf(films.values());
    }

    @Override
    public Optional<Film> getFilmById(Long filmId) {
        return Optional.ofNullable(films.get(filmId));
    }

    @Override
    public Film addFilm(Film film) {
        Long id = ++lastId;
        film.setId(id);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public void addLike(Film film, Long userId) {
        film.addLike(userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        films.get(filmId).removeLike(userId);
    }

    @Override
    public Set<Long> getLikes(Long filmId) {
        return films.get(filmId).getUserIds();
    }

    @Override
    public Rating getFilmRatingById(Long mpaId) {
        return Rating.findById(mpaId);
    }

    @Override
    public List<Rating> getFilmRatings() {
        return Arrays.asList(Rating.values());
    }

    @Override
    public List<Genre> getGenres() {
        return Arrays.asList(Genre.values());
    }

    @Override
    public Genre getGenreById(Long genreId) {
        return Genre.fromId(genreId);
    }

    @Override
    public List<Film> getMostPopularFilms(int count) {
        return films.values()
                .stream()
                .sorted(Comparator.comparingInt((Film f) -> f.getUserIds().size()).reversed())
                .limit(count)
                .toList();
    }

}
