package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.dao.mapper.MpaRowMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@AutoConfigureTestDatabase
@Import({MpaDbStorage.class, MpaRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MpaDbStorageTest {

    private final MpaDbStorage storage;

    @Test
    void getFilmRatingByIdTest() {
        // Проверяем существующий рейтинг
        Optional<Rating> optionalRating = storage.getFilmRatingById(1L);

        assertThat(optionalRating).isPresent();

        Rating rating = optionalRating.get();

        assertThat(rating)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", 1L)
                .hasFieldOrPropertyWithValue("name", "G");

        // Проверяем обработку ошибки на несуществующий рейтинг
        Optional<Rating> invalidRating = storage.getFilmRatingById(999L);
        assertThat(invalidRating).isNotPresent();
    }

    @Test
    void getFilmRatingsTest() {
        List<Rating> ratings = storage.getFilmRatings();

        assertThat(ratings)
                .hasSize(5)
                .extracting(Rating::getName)
                .containsExactlyInAnyOrder("G", "PG", "PG-13", "R", "NC-17");
    }
}
