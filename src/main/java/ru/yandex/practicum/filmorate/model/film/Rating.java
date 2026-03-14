package ru.yandex.practicum.filmorate.model.film;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum Rating {
    G(1, "G"),
    PG(2, "PG"),
    PG13(3, "PG-13"),
    R(4, "R"),
    NC17(5, "NC-17");

    private final long id;
    private final String name;

    @JsonCreator
    public static Rating fromJson(Map<String, Object> node) {
        if (node != null && node.containsKey("id")) {
            long id = Long.parseLong(node.get("id").toString());
            return findById(id);
        }
        return null;
    }

    public static Rating findById(long id) {
        for (Rating rating : Rating.values()) {
            if (rating.id == id) return rating;
        }
        throw new NotFoundException("Rating with id " + id + " not found");
    }

    @JsonValue
    public Map<String, Object> toJson() {
        return Map.of("id", id, "name", name);
    }
}
