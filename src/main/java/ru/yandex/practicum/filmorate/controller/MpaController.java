package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.film.Rating;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mpa")
public class MpaController {

    private final FilmService filmService;

    @GetMapping
    public List<Rating> getAllFilmRatings() {
        return filmService.getAllFilmRatings();
    }

    @GetMapping("/{mpaId}")
    public Rating getFilmRatingById(@PathVariable Long mpaId) {
        return filmService.getFilmRatingById(mpaId);
    }
}
