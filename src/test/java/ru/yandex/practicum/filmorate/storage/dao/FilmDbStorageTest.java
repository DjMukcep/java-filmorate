package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dao.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.dao.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.dao.mapper.UserRowMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, FilmRowMapper.class,
        UserDbStorage.class, UserRowMapper.class, GenreRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmDbStorageTest {

    private final FilmDbStorage storage;
    private final UserDbStorage userStorage;

    @Test
    void addFilmTest() {
        Genre action = new Genre(6L, "Боевик");
        Genre comedy = new Genre(1L, "Комедия");
        Film film = Film.builder()
                .name("Побег из Шоушенка")
                .description("История одного заключения")
                .releaseDate(LocalDate.of(1994, 9, 10))
                .duration(142)
                .mpa(new Rating(1L, ""))
                .genres(Set.of(action, comedy))
                .build();

        Film savedFilm = storage.addFilm(film);
        Optional<Film> filmOptional = storage.getFilmById(savedFilm.getId());

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(f -> {
                    assertThat(f.getName()).isEqualTo("Побег из Шоушенка");
                    assertThat(f.getMpa().getId()).isEqualTo(1L);
                    assertThat(f.getDuration()).isEqualTo(142);
                    assertThat(f.getReleaseDate()).isEqualTo(LocalDate.of(1994, 9, 10));
                    assertThat(f.getGenres()).containsExactly(comedy, action);
                });
    }

    @Test
    void updateFilmTest() {
        Genre action = new Genre(6L, "Боевик");
        Genre comedy = new Genre(1L, "Комедия");
        Genre drama = new Genre(2L, "Драма");
        Film film = Film.builder()
                .name("Старое название")
                .description("Старое описание")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(100)
                .mpa(new Rating(1L, "G"))
                .genres(Set.of(action))
                .build();
        Film savedFilm = storage.addFilm(film);
        Long filmId = savedFilm.getId();

        Film updatedFilm = Film.builder()
                .id(filmId) // Тот же ID
                .name("Новое название")
                .description("Новое описание")
                .releaseDate(LocalDate.of(2020, 12, 12))
                .duration(180)
                .mpa(new Rating(2L, "")) // Другой рейтинг
                .genres(Set.of(comedy, drama))
                .build();

        storage.updateFilm(updatedFilm);
        Optional<Film> filmOptional = storage.getFilmById(filmId);

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(f -> {
                    assertThat(f.getName()).isEqualTo("Новое название");
                    assertThat(f.getMpa().getId()).isEqualTo(2L);
                    assertThat(f.getDuration()).isEqualTo(180);
                    assertThat(f.getGenres())
                            .hasSize(2)
                            .containsExactlyInAnyOrder(comedy, drama)
                            .doesNotContain(action);
                });
    }

    @Test
    void addLikeAndCheckPopularityTest() {
        User user1 = userStorage.addUser(User.builder()
                .email("u1@mail.ru").login("login1").name("Name1")
                .birthday(LocalDate.of(2000, 1, 1)).build());
        User user2 = userStorage.addUser(User.builder()
                .email("u2@mail.ru").login("login2").name("Name2")
                .birthday(LocalDate.of(1995, 5, 5)).build());

        Film filmA = storage.addFilm(Film.builder()
                .name("Фильм А").description("Описание А").duration(100)
                .releaseDate(LocalDate.of(2020, 1, 1))
                .mpa(new Rating(1L, "G")).build());

        Film filmB = storage.addFilm(Film.builder()
                .name("Фильм Б").description("Описание Б").duration(120)
                .releaseDate(LocalDate.of(2021, 1, 1))
                .mpa(new Rating(1L, "G")).build());

        storage.addLike(filmA, user1.getId());
        storage.addLike(filmA, user2.getId());
        storage.addLike(filmB, user1.getId());

        List<Film> popular = storage.getMostPopularFilms(2);

        // Фильм А должен быть первым, так как у него больше лайков
        assertThat(popular)
                .hasSize(2)
                .containsExactly(filmA, filmB);
        assertThat(popular.getFirst().getName()).isEqualTo("Фильм А");
    }

    @Test
    void removeLikeTest() {
        User user = userStorage.addUser(User.builder()
                .email("u1@mail.ru").login("login1").name("Name1")
                .birthday(LocalDate.of(2000, 1, 1)).build());

        Film filmA = storage.addFilm(Film.builder()
                .name("Фильм А").description("Описание А").duration(100)
                .releaseDate(LocalDate.of(2020, 1, 1))
                .mpa(new Rating(1L, "G")).build());

        Film filmB = storage.addFilm(Film.builder()
                .name("Фильм Б").description("Описание Б").duration(120)
                .releaseDate(LocalDate.of(2021, 1, 1))
                .mpa(new Rating(1L, "G")).build());

        // обоим ставим по лайку
        storage.addLike(filmA, user.getId());
        storage.addLike(filmB, user.getId());

        // удаляем лайк у Фильма А
        storage.removeLike(filmA.getId(), user.getId());

        List<Film> popular = storage.getMostPopularFilms(2);

        // Фильм Б (1 лайк) должен быть выше Фильма А (0 лайков)
        assertThat(popular)
                .hasSize(2)
                .containsExactly(filmB, filmA);

        Set<Long> likes = storage.getLikes(popular.get(1).getId());

        // Проверяем, что у Фильма А лайков больше нет
        assertThat(likes.size()).isEqualTo(0);
    }

    @Test
    void getMostPopularFilmsTest() {
        Film film1 = storage.addFilm(Film.builder().name("Фильм 1").description("Д")
                .releaseDate(LocalDate.now()).duration(100).mpa(new Rating(1L, "G")).build());
        Film film2 = storage.addFilm(Film.builder().name("Фильм 2").description("Д")
                .releaseDate(LocalDate.now()).duration(100).mpa(new Rating(1L, "G")).build());
        Film film3 = storage.addFilm(Film.builder().name("Фильм 3").description("Д")
                .releaseDate(LocalDate.now()).duration(100).mpa(new Rating(1L, "G")).build());

        // Создаем пользователей для лайков
        User u1 = userStorage.addUser(User.builder().email("1@m.ru").login("l1")
                .name("N1").birthday(LocalDate.now()).build());
        User u2 = userStorage.addUser(User.builder().email("2@m.ru").login("l2")
                .name("N2").birthday(LocalDate.now()).build());

        // Распределяем лайки:
        // Фильм 2 -> 2 лайка (Лидер)
        // Фильм 3 -> 1 лайк  (Второе место)
        // Фильм 1 -> 0 лайков (Хвост)
        storage.addLike(film2, u1.getId());
        storage.addLike(film2, u2.getId());
        storage.addLike(film3, u1.getId());

        // Действие: просим ТОП-2
        List<Film> popular = storage.getMostPopularFilms(2);

        // Проверка лимита, ПОРЯДКА (film2 выше film3), то третий лишний не влез.
        assertThat(popular)
                .hasSize(2)
                .containsExactly(film2, film3)
                .doesNotContain(film1);
    }

    @Test
    void getFilmsTest() {
        Film film1 = storage.addFilm(Film.builder().name("Фильм 1").description("Д")
                .releaseDate(LocalDate.now()).duration(100).mpa(new Rating(1L, "G")).build());
        Film film2 = storage.addFilm(Film.builder().name("Фильм 2").description("Д")
                .releaseDate(LocalDate.now()).duration(100).mpa(new Rating(1L, "G")).build());
        Film film3 = storage.addFilm(Film.builder().name("Фильм 3").description("Д")
                .releaseDate(LocalDate.now()).duration(100).mpa(new Rating(1L, "G")).build());

        List<Film> films = storage.getFilms();

        assertThat(films)
                .isNotNull()
                .hasSize(3)
                .containsExactly(film1, film2, film3);
    }

    @Test
    void getFilmByIdTest() {
        Optional<Film> unfoundedFilm = storage.getFilmById(999L);

        assertThat(unfoundedFilm).isNotPresent();
    }

    @Test
    void getLikesTest() {
        Film film = storage.addFilm(Film.builder()
                .name("Начало").description("Сны").duration(148)
                .releaseDate(LocalDate.of(2010, 7, 8))
                .mpa(new Rating(1L, "G")).build());

        User user1 = userStorage.addUser(User.builder()
                .email("u1@mail.ru").login("l1").name("N1").birthday(LocalDate.now()).build());
        User user2 = userStorage.addUser(User.builder()
                .email("u2@mail.ru").login("l2").name("N2").birthday(LocalDate.now()).build());

        storage.addLike(film, user1.getId());
        storage.addLike(film, user2.getId());

        Set<Long> likes = storage.getLikes(film.getId());

        // должны быть лайки с id этих пользователей
        assertThat(likes)
                .hasSize(2)
                .containsExactlyInAnyOrder(user1.getId(), user2.getId());

        // Проверка "чужих" лайков: если у фильма нет лайков, должен быть пустой сет
        assertThat(storage.getLikes(999L)).isEmpty();
    }
}
