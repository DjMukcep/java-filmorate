package ru.yandex.practicum.filmorate.model.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FriendshipStatus {
    UNCONFIRMED(1), CONFIRMED(2);

    private final long id;
}
