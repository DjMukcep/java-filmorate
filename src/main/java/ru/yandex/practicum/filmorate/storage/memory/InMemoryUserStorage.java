package ru.yandex.practicum.filmorate.storage.memory;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;

@Component
@Qualifier("MEM")
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();
    private long lastId;

    @Override
    public List<User> getUsers() {
        return List.copyOf(users.values());
    }

    @Override
    public Optional<User> getUserById(Long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public User addUser(User user) {
        Long id = ++lastId;

        user.setId(id);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public void setFriendStatus(User user, Long friendId, FriendshipStatus status) {
        user.addFriend(friendId, status);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        users.get(userId).removeFriend(friendId);
    }

    @Override
    public List<User> getFriends(User user) {
        return user.getFriends()
                .keySet()
                .stream()
                .map(users::get)
                .toList();
    }
}
