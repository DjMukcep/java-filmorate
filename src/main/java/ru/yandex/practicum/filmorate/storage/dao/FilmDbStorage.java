package ru.yandex.practicum.filmorate.storage.dao;

import jakarta.annotation.Nonnull;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.dao.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.dao.mapper.GenreRowMapper;


import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;


@Repository
public class FilmDbStorage extends BaseDbStorage<Film> implements FilmStorage {

    private final GenreRowMapper genreRowMapper;

    public FilmDbStorage(JdbcTemplate jdbc, FilmRowMapper mapper, GenreRowMapper genreRowMapper) {
        super(jdbc, mapper);
        this.genreRowMapper = genreRowMapper;
    }


    @Override
    public Film addFilm(Film film) {
        String sql = """
                INSERT INTO films(rating_id, title, description, release_date, duration)
                VALUES (?,?,?,?,?);
                """;
        long id = insert(sql,
                film.getMpa().getId(),
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration()
        );

        film.setId(id);
        saveGenres(film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        String sql = """
                UPDATE films SET rating_id=?,
                title=?,
                description=?,
                release_date=?,
                duration=?
                WHERE film_id=?
                """;

        update(sql,
                film.getMpa().getId(),
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getId()
        );

        removeFilmGenres(film.getId());
        saveGenres(film);
        return film;
    }

    @Override
    public void addLike(Film film, Long userId) {
        String sql = """
                MERGE INTO favorite_films (user_id, film_id)
                KEY(user_id, film_id) VALUES (?, ?)
                """;

        update(sql, userId, film.getId());
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        String sql = """
                DELETE FROM favorite_films
                WHERE film_id=? AND user_id=?
                """;

        update(sql, filmId, userId);
    }

    @Override
    public List<Film> getMostPopularFilms(int count) {
        String sql = """
                SELECT f.*, r.rating_id, r.rating FROM films f
                JOIN ratings r ON f.rating_id = r.rating_id
                LEFT JOIN favorite_films ff ON f.film_id=ff.film_id
                GROUP BY f.film_id
                ORDER BY COUNT(ff.user_id)
                DESC LIMIT ?
                """;

        List<Film> films = findMany(sql, count);
        loadGenres(films);
        return films;
    }

    @Override
    public List<Film> getFilms() {
        String sql = """
                SELECT f.*, r.rating_id, r.rating
                FROM films f
                LEFT JOIN ratings r ON f.rating_id = r.rating_id
                """;

        List<Film> films = findMany(sql);
        loadGenres(films);
        return films;
    }

    @Override
    public Optional<Film> getFilmById(Long filmId) {
        String sql = """
                SELECT f.*, r.rating_id, r.rating
                FROM films f
                JOIN ratings r ON f.rating_id = r.rating_id
                WHERE f.film_id = ?
                """;

        Optional<Film> film = findOne(sql, filmId);
        film.ifPresent(f -> f.setGenres(getGenresByFilmId(filmId)));
        return film;
    }

    @Override
    public Set<Long> getLikes(Long filmId) {
        String sql = """
                SELECT user_id FROM favorite_films
                WHERE film_id=?
                """;

        List<Long> likes = jdbc.query(
                sql,
                (rs, rowNum) -> rs.getLong("user_id"),
                filmId
        );
        return new HashSet<>(likes);
    }

    private void removeFilmGenres(long filmId) {
        String sql = """
                DELETE FROM film_genres
                WHERE film_id=?
                """;

        jdbc.update(sql, filmId);
    }

    private Set<Genre> getGenresByFilmId(Long filmId) {
        String sql = """
                SELECT g.*
                FROM genres g
                JOIN film_genres f ON g.genre_id=f.genre_id
                WHERE f.film_id=?
                """;

        return new HashSet<>(jdbc.query(sql, genreRowMapper, filmId));
    }

    private void saveGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }

        String sql = """
                INSERT INTO film_genres (film_id, genre_id)
                VALUES (?,?)
                """;

        List<Genre> genres = new ArrayList<>(film.getGenres());
        jdbc.batchUpdate(sql, getBatchSetter(film.getId(), genres));
    }

    private void loadGenres(List<Film> films) {
        String getGenresSql = """
                SELECT fg.film_id, g.genre_id, g.genre
                FROM film_genres fg
                JOIN genres g ON fg.genre_id = g.genre_id
                """;

        Map<Long, Set<Genre>> genresByFilmId = new HashMap<>();

        jdbc.query(getGenresSql, rs -> {
            long filmId = rs.getLong("film_id");

            Genre genre = Genre.builder()
                    .id(rs.getLong("genre_id"))
                    .name(rs.getString("genre"))
                    .build();
            genresByFilmId.computeIfAbsent(filmId, k -> new HashSet<>()).add(genre);
        });

        for (Film film : films) {
            film.setGenres(genresByFilmId.getOrDefault(film.getId(), Collections.emptySet()));
        }
    }

    private BatchPreparedStatementSetter getBatchSetter(Long filmId, List<Genre> genres) {
        return new BatchPreparedStatementSetter() {
            @Override
            public void setValues(@Nonnull PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, filmId);
                ps.setLong(2, genres.get(i).getId());
            }

            @Override
            public int getBatchSize() {
                return genres.size();
            }
        };
    }
}
