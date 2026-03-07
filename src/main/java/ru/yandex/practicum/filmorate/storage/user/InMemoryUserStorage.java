package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.user.User;

import java.util.*;

@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();
    private final HashMap<Long, Set<Long>> friendIds = new HashMap<>();


    @Override
    public List<User> getUsers() {
        return List.copyOf(users.values());
    }

    @Override
    public Optional<User> getUserById(Long filmId) {
        return Optional.ofNullable(users.get(filmId));
    }

    @Override
    public List<Long> findExistentIds(Set<Long> ids) {
        return users.values()
                .stream()
                .map(User::getId)
                .filter(ids::contains)
                .toList();
    }

    @Override
    public void saveUser(User user) {
        friendIds.putIfAbsent(user.getId(), new HashSet<>());
        users.put(user.getId(), user);
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        friendIds.get(userId).add(friendId);
        friendIds.get(friendId).add(userId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        friendIds.get(userId).remove(friendId);
        friendIds.get(friendId).remove(userId);
    }

    @Override
    public List<User> getFriends(User user) {
        return friendIds.get(user.getId())
                .stream()
                .map(users::get)
                .toList();
    }
}
