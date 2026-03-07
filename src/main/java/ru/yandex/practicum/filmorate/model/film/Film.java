package ru.yandex.practicum.filmorate.model.film;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Film {
    private Long id;
    @NotBlank(message = "Название фильма не может быть пустым.")
    private String name;
    @NotBlank(message = "Описание обязательно к заполнению.")
    @Size(max = 200, message = "Описание не должно превышать 200 символов.")
    private String description;
    private LocalDate releaseDate;
    @NotNull(message = "Длительность фильма должна быть указана.")
    @Positive(message = "Продолжительность фильма должна быть положительной.")
    private Integer duration;
    @JsonIgnore //пока нет postman тестов игнорим в json
    private List<Genre> genres;
    @JsonIgnore //пока нет postman тестов игнорим в json
    private Rating rating;
}
