package ru.yandex.practicum.filmorate.storage.memory;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Qualifier("MEM")
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();
    private final InMemoryGenreStorage genreStorage;
    private final InMemoryMpaStorage mpaStorage;
    private long lastId;

    public InMemoryFilmStorage(InMemoryGenreStorage genreStorage, InMemoryMpaStorage mpaStorage) {
        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
    }

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
        setGenres(film);
        setRating(film);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        setGenres(film);
        setRating(film);
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
    public List<Film> getMostPopularFilms(int count) {
        return films.values()
                .stream()
                .sorted(Comparator.comparingInt((Film f) -> f.getUserIds().size()).reversed())
                .limit(count)
                .toList();
    }

    private void setGenres(Film film) {
        Set<Long> incomeGenreIds = film.getGenres()
                .stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());

        Set<Genre> genres = genreStorage.getGenres().stream()
                .filter(genre -> incomeGenreIds.contains(genre.getId()))
                .collect(Collectors.toSet());
        film.setGenres(genres);
    }

    private void setRating(Film film) {
        Long mpaId = film.getMpa().getId();
        Optional<Rating> rating = mpaStorage.getFilmRatingById(mpaId);
        rating.ifPresent(film::setMpa);
    }
}
