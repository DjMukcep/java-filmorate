package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.*;


import java.time.LocalDate;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Integer id;
    @NotBlank
    @Email
    private String email;
    @NotBlank
    @Pattern(regexp = "\\S+")
    private String login;
    private String name;
    @Past
    @NotNull
    private LocalDate birthday;
}
