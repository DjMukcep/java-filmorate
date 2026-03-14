package ru.yandex.practicum.filmorate.model.film;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

import java.util.Map;


@Getter
@RequiredArgsConstructor
public enum Genre {
    COMEDY(1,"Комедия"),
    DRAMA(2,"Драма"),
    CARTOON(3,"Мультфильм"),
    THRILLER(4,"Триллер"),
    DOCUMENTARY(5,"Документальный"),
    ACTION(6,"Боевик");

    private final long id;
    private final String name;

    @JsonCreator
    public static Genre fromObject(Map<String, Object> node) {
        if (node.containsKey("id")) {
            Object idValue = node.get("id");
            long id = Long.parseLong(idValue.toString());
            return fromId(id);
        }
        return null;
    }

    public static Genre fromId(long id) {
        for (Genre genre : Genre.values()) {
            if (genre.id == id) {
                return genre;
            }
        }
        throw new NotFoundException("Genre id not found");
    }

    @JsonValue
    public Map<String, Object> toJson() {
        return  Map.of("id", this.id, "name", this.name);
    }
}
