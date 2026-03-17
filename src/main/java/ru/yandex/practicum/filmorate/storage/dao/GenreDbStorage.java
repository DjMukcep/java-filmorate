package ru.yandex.practicum.filmorate.storage.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.dao.mapper.GenreRowMapper;

import java.util.List;
import java.util.Optional;

@Repository
public class GenreDbStorage extends BaseDbStorage<Genre> implements GenreStorage {


    public GenreDbStorage(JdbcTemplate jdbc, GenreRowMapper mapper) {
        super(jdbc, mapper);
    }

    @Override
    public List<Genre> getGenres() {
        String sql = "SELECT * FROM genres";
        return findMany(sql);
    }

    @Override
    public Optional<Genre> getGenreById(Long genreId) {
        String sql = """
                SELECT * FROM genres
                WHERE genre_id=?
                """;
        return findOne(sql, genreId);
    }
}
