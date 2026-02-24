package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Film {
    private Long id;
    @NotBlank
    @NotBlank
    private String name;
    @NotBlank
    @Size(max = 200)
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
}
