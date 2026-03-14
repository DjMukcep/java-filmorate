package ru.yandex.practicum.filmorate.storage.dao;

import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.model.film.Rating;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.dao.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.dao.query.QueryHandler;

import static ru.yandex.practicum.filmorate.storage.dao.query.Query.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;


@Repository
@Qualifier("DB")
public class FilmDbStorage extends BaseDbStorage<Film> implements FilmStorage {

    public FilmDbStorage(JdbcTemplate jdbc, FilmRowMapper mapper, QueryHandler queryHandler) {
        super(jdbc, mapper, queryHandler);
    }


    @Override
    public Film addFilm(Film film) {
        long id = insert(queryHandler.get(ADD_FILM),
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
        update(queryHandler.get(UPDATE_FILM),
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
        update(queryHandler.get(ADD_LIKE), userId, film.getId());
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        update(queryHandler.get(REMOVE_LIKE), filmId, userId);
    }

    @Override
    public List<Film> getMostPopularFilms(int count) {
        return findMany(queryHandler.get(GET_MOST_POPULAR_FILMS), count);
    }

    @Override
    public List<Film> getFilms() {
        return findMany(queryHandler.get(FIND_ALL_FILMS));
    }

    @Override
    public Optional<Film> getFilmById(Long filmId) {
        Optional<Film> film = findOne(queryHandler.get(FIND_FILM), filmId);
        film.ifPresent(f -> f.setGenres(getGenresByFilmId(f.getId())));
        return film;
    }

    @Override
    public Set<Long> getLikes(Long filmId) {
        List<Long> likes = jdbc.query(
                queryHandler.get(GET_FILM_LIKES),
                (rs, rowNum) -> rs.getLong("user_id"),
                filmId
        );
        return new HashSet<>(likes);
    }

    @Override
    public Rating getFilmRatingById(Long mpaId) {
        try {
            return jdbc.queryForObject(
                    queryHandler.get(FIND_RATING),
                    (rs, rowNum) -> Rating.findById(rs.getLong("rating_id")),
                    mpaId
            );
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Rating with id: " + mpaId + " not found");
        }
    }

    @Override
    public List<Rating> getFilmRatings() {
        return jdbc.query(
                queryHandler.get(FIND_ALL_RATINGS),
                (rs, rowNum) -> Rating.findById(rs.getLong("rating_id"))
        );
    }

    @Override
    public List<Genre> getGenres() {
        return jdbc.query(
                queryHandler.get(FIND_ALL_GENRES),
                (rs, rowNum) -> Genre.fromId(rs.getLong("genre_id"))
        );
    }

    @Override
    public Genre getGenreById(Long genreId) {
        try {
            return jdbc.queryForObject(
                    queryHandler.get(FIND_GENRE),
                    (rs, rowNum) -> Genre.fromId(rs.getLong("genre_id")),
                    genreId
            );
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Genre with id: " + genreId + " not found");
        }
    }

    private void removeFilmGenres(long filmId) {
        jdbc.update(queryHandler.get(REMOVE_FILM_GENRES), filmId);
    }

    private Set<Genre> getGenresByFilmId(Long filmId) {
        return new HashSet<>(jdbc.query(queryHandler.get(FIND_FILM_GENRES),
                (rs, rowNum) -> Genre.fromId(rs.getLong("genre_id")), filmId));
    }

    private void saveGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }

        List<Genre> genres = new ArrayList<>(film.getGenres());
        jdbc.batchUpdate(queryHandler.get(ADD_FILM_GENRE), getBatchSetter(film.getId(), genres));
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
