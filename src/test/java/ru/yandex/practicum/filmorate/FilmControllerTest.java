package ru.yandex.practicum.filmorate;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

@WebMvcTest(FilmController.class)
public class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FilmService filmService;

    @Autowired
    private ObjectMapper objectMapper;

    /** Создаёт образец фильма для тестов */
    private Film createSampleFilm() {
        Film film = new Film();
        film.setId(1L);
        film.setName("Пример фильма");
        film.setDescription("Описание фильма");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        return film;
    }

    /** Успешное получение фильма по ID */
    @Test
    void getFilmById_ShouldReturnFilm() throws Exception {
        Film film = createSampleFilm();
        when(filmService.getFilmById(1L)).thenReturn(film);

        mockMvc.perform(get("/films/1"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(film)));
    }

    /** Создание фильма - успех */
    @Test
    void createFilm_ShouldReturnCreated() throws Exception {
        Film film = createSampleFilm();
        when(filmService.createFilm(any(Film.class))).thenReturn(film);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(film)));
    }

    /** Обновление фильма - успех */
    @Test
    void updateFilm_ShouldReturnOk() throws Exception {
        Film film = createSampleFilm();
        when(filmService.updateFilm(any(Film.class))).thenReturn(film);

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(film)));
    }

    /** Получение всех фильмов */
    @Test
    void getAllFilms_ShouldReturnList() throws Exception {
        List<Film> films = Arrays.asList(createSampleFilm());
        when(filmService.getAllFilms()).thenReturn(films);

        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(films)));
    }

    /** Лайк фильму — успех */
    @Test
    void putLike_ShouldReturnOk() throws Exception {
        doNothing().when(filmService).putLike(1L, 2L);

        mockMvc.perform(put("/films/1/like/2"))
                .andExpect(status().isOk());
        verify(filmService).putLike(1L, 2L);
    }

    /** Удаление лайка — успех */
    @Test
    void removeLike_ShouldReturnOk() throws Exception {
        doNothing().when(filmService).removeLike(1L, 2L);

        mockMvc.perform(delete("/films/1/like/2"))
                .andExpect(status().isOk());
        verify(filmService).removeLike(1L, 2L);
    }

    /** Получить список популярных фильмов */
    @Test
    void getPopularFilms_ShouldReturnList() throws Exception {
        List<Film> popularFilms = Arrays.asList(createSampleFilm());
        when(filmService.getPopularFilms(10)).thenReturn(popularFilms);

        mockMvc.perform(get("/films/popular"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(popularFilms)));
    }

    /** Валидация: пустое название фильма */
    @Test
    void createFilm_BlankName_ShouldReturnBadRequest() throws Exception {
        Film invalidFilm = createSampleFilm();
        invalidFilm.setName(" "); // пустое имя

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidFilm)))
                .andExpect(status().isBadRequest());
    }

    /** Валидация: описание длиннее 200 символов */
    @Test
    void createFilm_TooLongDescription_ShouldReturnBadRequest() throws Exception {
        Film invalidFilm = createSampleFilm();
        String longDescription = "a".repeat(201);
        invalidFilm.setDescription(longDescription);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidFilm)))
                .andExpect(status().isBadRequest());
    }

    /** Валидация: null дата релиза */
    @Test
    void createFilm_NullReleaseDate_ShouldReturnBadRequest() throws Exception {
        Film invalidFilm = createSampleFilm();
        invalidFilm.setReleaseDate(null);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidFilm)))
                .andExpect(status().isBadRequest());
    }

    /** Валидация: отрицательная продолжительность */
    @Test
    void createFilm_NegativeDuration_ShouldReturnBadRequest() throws Exception {
        Film invalidFilm = createSampleFilm();
        invalidFilm.setDuration(-100);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidFilm)))
                .andExpect(status().isBadRequest());
    }

}