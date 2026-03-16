package ru.yandex.practicum.filmorate.storage.memory;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.*;

@Repository
@Qualifier("MEM")
public class InMemoryMpaStorage implements MpaStorage {

    private final Map<Long, Rating> ratings = new HashMap<>();

    public InMemoryMpaStorage() {
        ratings.put(1L, new Rating(1L, "G"));
        ratings.put(2L, new Rating(2L, "PG"));
        ratings.put(3L, new Rating(3L, "PG-13"));
        ratings.put(4L, new Rating(4L, "R"));
        ratings.put(5L, new Rating(5L, "NC-17"));
    }

    @Override
    public Optional<Rating> getFilmRatingById(Long mpaId) {
        return Optional.ofNullable(ratings.get(mpaId));
    }

    @Override
    public List<Rating> getFilmRatings() {
        return new ArrayList<>(ratings.values());
    }
}
