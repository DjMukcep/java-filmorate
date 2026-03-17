package ru.yandex.practicum.filmorate.storage.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.MpaStorage;
import ru.yandex.practicum.filmorate.storage.dao.mapper.MpaRowMapper;

import java.util.List;
import java.util.Optional;

@Repository
public class MpaDbStorage extends BaseDbStorage<Rating> implements MpaStorage {

    public MpaDbStorage(JdbcTemplate jdbc, MpaRowMapper mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Optional<Rating> getFilmRatingById(Long mpaId) {
        String sql = """
                SELECT * FROM ratings
                WHERE rating_id=?
                """;
        return findOne(sql, mpaId);
    }

    @Override
    public List<Rating> getFilmRatings() {
        String sql = "SELECT * FROM ratings";
        return findMany(sql);
    }
}
