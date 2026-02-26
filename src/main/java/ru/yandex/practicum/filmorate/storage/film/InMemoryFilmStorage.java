package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();
    private final Map<Long, Set<Long>> likeUserIds = new HashMap<>();

    @Override
    public List<Film> getFilms() {
        return List.copyOf(films.values());
    }

    @Override
    public Optional<Film> getFilmById(Long filmId) {
        return Optional.ofNullable(films.get(filmId));
    }

    @Override
    public void saveFilm(Film film) {
        likeUserIds.putIfAbsent(film.getId(), new HashSet<>());
        films.put(film.getId(), film);
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        likeUserIds.get(filmId).add(userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        likeUserIds.get(filmId).remove(userId);
    }

    @Override
    public List<Film> getMostPopularFilms(int count) {
        return films.values()
                .stream()
                .sorted(Comparator.comparingInt((Film f) -> likeUserIds.get(f.getId()).size()).reversed())
                .limit(count)
                .toList();
    }

    @Override
    public Set<Long> getLikes(Long filmId) {
        return likeUserIds.get(filmId);
    }
}
