package ru.yandex.practicum.filmorate.model.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    @NotBlank(message = "Поле email обязательно к заполнению")
    @Email(message = "Обнаружен некорректный email.")
    private String email;
    @Pattern(regexp = "\\S+", message = "В имени пользователя пробелы не допускаются.")
    @NotBlank(message = "Имя пользователя обязательно к заполнению.")
    private String login;
    private String name;
    @Past(message = "Дата дня рожденья должна быть в прошлом.")
    @NotNull(message = "Необходимо указать свой день рожденья в формате год-месяц-день.")
    private LocalDate birthday;
    @JsonIgnore
    @Builder.Default
    private Map<Long, FriendshipStatus> friends = new HashMap<>();

    public void addFriend(Long friendId, FriendshipStatus status) {
        friends.put(friendId, status);
    }

    public void removeFriend(Long friendId) {
        friends.remove(friendId);
    }
}
