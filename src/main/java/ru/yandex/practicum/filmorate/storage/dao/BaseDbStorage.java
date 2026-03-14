package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.storage.dao.query.QueryHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class BaseDbStorage<T> {
    protected final JdbcTemplate jdbc;
    protected final RowMapper<T> mapper;
    protected final QueryHandler queryHandler;

    protected Optional<T> findOne(String query, Object... args) {
        try {
            T obj = jdbc.queryForObject(query, mapper, args);
            return Optional.ofNullable(obj);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    protected List<T> findMany(String query, Object... args) {
        return jdbc.query(query, mapper, args);
    }

    protected void update(String query, Object... args) {
        int rowsUpdated = jdbc.update(query, args);
        if (rowsUpdated == 0) {
            throw new InternalServerException("Не удалось обновить данные");
        }
    }

    protected long insert(String query, Object... args) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> getPreparedStatement(con, query, args), keyHolder);
        Long id = keyHolder.getKeyAs(Long.class);

        if (id == null) {
            throw new InternalServerException("Не удалось сохранить данные");
        }
        return id;
    }

    private static PreparedStatement getPreparedStatement(
            Connection connection, String query, Object... args) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

        for (int i = 0; i < args.length; i++) {
            statement.setObject(i + 1, args[i]);
        }
        return statement;
    }
}
