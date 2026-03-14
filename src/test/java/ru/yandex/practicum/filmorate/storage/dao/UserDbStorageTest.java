package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.user.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.storage.dao.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.storage.dao.query.QueryHandler;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserDbStorage.class, QueryHandler.class, UserRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserDbStorageTest {

    private final UserDbStorage storage;

    @Test
    void addUserTest() {
        User user = User.builder()
                .email("user@email.com")
                .login("user-login")
                .name("user-name")
                .birthday(LocalDate.of(2002, 12, 12))
                .build();

        User savedUser = storage.addUser(user);
        Optional<User> userOptional = storage.getUserById(savedUser.getId());

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(u -> {
                    assertThat(u).isEqualTo(savedUser);
                    assertThat(user).hasFieldOrPropertyWithValue("id", 1L);
                });
    }

    @Test
    void testGetUserByInvalidId() {
        Optional<User> userOptional = storage.getUserById(999L);
        assertThat(userOptional).isEmpty();
    }

    @Test
    void updateUserTest() {
        User user = User.builder()
                .email("old@email.com")
                .login("old-login")
                .name("Old Name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
        User savedUser = storage.addUser(user);
        Long userId = savedUser.getId();

        User updatedUser = User.builder()
                .id(userId) // тот же ID!
                .email("new@email.com")
                .login("new-login")
                .name("New Name")
                .birthday(LocalDate.of(1990, 5, 5))
                .build();

        storage.updateUser(updatedUser);
        Optional<User> userOptional = storage.getUserById(userId);

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(u -> {
                    assertThat(u.getEmail()).isEqualTo("new@email.com");
                    assertThat(u.getId()).isEqualTo(userId);
                    assertThat(u).isEqualTo(updatedUser);
                });
    }

    @Test
    void setFriendStatusTest() {
        User user = User.builder()
                .email("user@email.com")
                .login("user-login")
                .name("user name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
        User friend = User.builder()
                .email("friend@email.com")
                .login("friend-login")
                .name("friend name")
                .birthday(LocalDate.of(1990, 5, 5))
                .build();
        User savedUser = storage.addUser(user);
        User savedFriend = storage.addUser(friend);

        storage.setFriendStatus(savedUser, savedFriend.getId(), FriendshipStatus.CONFIRMED);

        List<User> friends = storage.getFriends(savedUser);

        assertThat(friends).contains(savedFriend);
    }

    @Test
    void removeFriendTest() {
        User user = User.builder()
                .email("user@email.com")
                .login("user-login")
                .name("user name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
        User friend = User.builder()
                .email("friend@email.com")
                .login("friend-login")
                .name("friend name")
                .birthday(LocalDate.of(1990, 5, 5))
                .build();
        User savedUser = storage.addUser(user);
        User savedFriend = storage.addUser(friend);

        storage.setFriendStatus(savedUser, savedFriend.getId(), FriendshipStatus.CONFIRMED);

        assertThat(storage.getFriends(savedUser)).hasSize(1);

        storage.removeFriend(savedUser.getId(), savedFriend.getId());

        // список друзей пуст
        List<User> friendsAfter = storage.getFriends(savedUser);
        assertThat(friendsAfter).isEmpty();

        // сами пользователи не удалились из таблицы USERS
        assertThat(storage.getUserById(savedUser.getId())).isPresent();
        assertThat(storage.getUserById(savedFriend.getId())).isPresent();
    }

    @Test
    void getUsersTest() {
        User user1 = User.builder()
                .email("first@mail.com")
                .login("first")
                .name("First")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        User user2 = User.builder()
                .email("second@mail.com")
                .login("second")
                .name("Second")
                .birthday(LocalDate.of(1995, 5, 5))
                .build();
        storage.addUser(user1);
        storage.addUser(user2);

        List<User> users = storage.getUsers();

        // количество и содержимое
        assertThat(users)
                .isNotNull()
                .hasSize(2)
                .contains(user1, user2);
    }

    @Test
    void getFriendsTest() {
        User user = User.builder()
                .email("owner@yandex.ru")
                .login("owner_login")
                .name("Owner Name")
                .birthday(LocalDate.of(1995, 5, 20))
                .build();
        User savedUser = storage.addUser(user);

        User friend = User.builder()
                .email("friend@yandex.ru")
                .login("friend_login")
                .name("Friend Name")
                .birthday(LocalDate.of(2000, 10, 10))
                .build();
        User savedFriend = storage.addUser(friend);

        User stranger = User.builder()
                .email("stranger@yandex.ru")
                .login("stranger_login")
                .name("Stranger Name")
                .birthday(LocalDate.of(1988, 3, 15))
                .build();
        storage.addUser(stranger);

        // User добавляет Friend
        storage.setFriendStatus(savedUser, savedFriend.getId(), FriendshipStatus.CONFIRMED);

        List<User> friends = storage.getFriends(savedUser);

        // Должен быть ровно один друг savedFriend, но не stranger.
        assertThat(friends)
                .hasSize(1)
                .contains(savedFriend)
                .doesNotContain(stranger);

        assertThat(friends.getFirst())
                .hasFieldOrPropertyWithValue("email", "friend@yandex.ru")
                .hasFieldOrPropertyWithValue("login", "friend_login");
    }
}
