package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest  // Интеграционный тест для всего приложения
@AutoConfigureMockMvc  // Для MockMvc
public class FilmorateApplicationTests {

	@Autowired
	private Validator validator;  // Для валидации моделей

	@Autowired
	private MockMvc mockMvc;  // Для тестирования контроллеров

	@Autowired
	private ObjectMapper objectMapper;  // Для сериализации JSON

	// === Тесты для модели Film ===

	@Test
	public void shouldPassValidationForValidFilm() {
		Film film = new Film();
		film.setId(1);
		film.setName("Valid Film");
		film.setDescription("Valid description");
		film.setReleaseDate(LocalDate.of(2000, 1, 1));
		film.setDuration(120);

		Set<ConstraintViolation<Film>> violations = validator.validate(film);
		assertTrue(violations.isEmpty(), "Valid film should pass validation");
	}

	@Test
	public void shouldFailValidationForEmptyName() {
		Film film = new Film();
		film.setDescription("Description");
		film.setReleaseDate(LocalDate.of(2000, 1, 1));
		film.setDuration(120);

		Set<ConstraintViolation<Film>> violations = validator.validate(film);
		assertEquals(1, violations.size(), "Empty name should fail validation");
		assertEquals("Название не может быть пустым", violations.iterator().next().getMessage());
	}

	@Test
	public void shouldFailValidationForDescriptionTooLong() {
		Film film = new Film();
		film.setName("Film");
		film.setDescription("A".repeat(201));  // 201 символ — превышает 200
		film.setReleaseDate(LocalDate.of(2000, 1, 1));
		film.setDuration(120);

		Set<ConstraintViolation<Film>> violations = validator.validate(film);
		assertEquals(1, violations.size(), "Description too long should fail validation");
	}

	@Test
	public void shouldFailValidationForReleaseDateTooEarly() {
		Film film = new Film();
		film.setName("Film");
		film.setDescription("Description");
		film.setReleaseDate(LocalDate.of(1895, 12, 27));  // До минимальной даты
		film.setDuration(120);

		// Проверяем кастомную валидацию (в isValidReleaseDate)
		assertTrue(!film.isValidReleaseDate(), "Release date before 1895-12-28 should be invalid");
	}

	@Test
	public void shouldFailValidationForNegativeDuration() {
		Film film = new Film();
		film.setName("Film");
		film.setDescription("Description");
		film.setReleaseDate(LocalDate.of(2000, 1, 1));
		film.setDuration(-1);

		Set<ConstraintViolation<Film>> violations = validator.validate(film);
		assertEquals(1, violations.size(), "Negative duration should fail validation");
	}

	// === Тесты для модели User ===

	@Test
	public void shouldPassValidationForValidUser() {
		User user = new User();
		user.setId(1L);
		user.setEmail("user@example.com");
		user.setLogin("validlogin");
		user.setName("User Name");
		user.setBirthday(LocalDate.of(1990, 1, 1));

		Set<ConstraintViolation<User>> violations = validator.validate(user);
		assertTrue(violations.isEmpty(), "Valid user should pass validation");
	}

	@Test
	public void shouldFailValidationForInvalidEmail() {
		User user = new User();
		user.setEmail("invalidemail");  // Без @
		user.setLogin("login");
		user.setName("Name");
		user.setBirthday(LocalDate.of(1990, 1, 1));

		Set<ConstraintViolation<User>> violations = validator.validate(user);
		assertEquals(1, violations.size(), "Invalid email should fail validation");
	}

	@Test
	public void shouldFailValidationForEmptyLogin() {
		User user = new User();
		user.setEmail("user@example.com");
		// login пустой
		user.setName("Name");
		user.setBirthday(LocalDate.of(1990, 1, 1));

		Set<ConstraintViolation<User>> violations = validator.validate(user);
		assertEquals(1, violations.size(), "Empty login should fail validation");
	}

	@Test
	public void shouldFailValidationForLoginWithSpaces() {
		User user = new User();
		user.setEmail("user@example.com");
		user.setLogin("login with spaces");
		user.setName("Name");
		user.setBirthday(LocalDate.of(1990, 1, 1));

		// Проверяем кастомную валидацию (в isValidLogin)
		assertTrue(!user.isValidLogin(), "Login with spaces should be invalid");
	}

	@Test
	public void shouldFailValidationForFutureBirthday() {
		User user = new User();
		user.setEmail("user@example.com");
		user.setLogin("login");
		user.setName("Name");
		user.setBirthday(LocalDate.now().plusDays(1));  // Будущая дата

		Set<ConstraintViolation<User>> violations = validator.validate(user);
		assertEquals(1, violations.size(), "Future birthday should fail validation");
	}

	// === Тесты для контроллеров (FilmController) ===

	@Test
	public void shouldReturn400ForEmptyFilmRequest() throws Exception {
		// Пустой JSON {}
		String emptyJson = "{}";

		mockMvc.perform(post("/films")
						.contentType(MediaType.APPLICATION_JSON)
						.content(emptyJson))
				.andExpect(status().isBadRequest());  // 400 Bad Request для пустых/неверных полей
	}

	@Test
	public void shouldReturn400ForInvalidFilmData() throws Exception {
		// Неверные данные: пустое name, дата до 1895
		String invalidJson = "{\"name\":\"\", \"description\":\"Desc\", \"releaseDate\":\"1800-01-01\", \"duration\":-1}";

		mockMvc.perform(post("/films")
						.contentType(MediaType.APPLICATION_JSON)
						.content(invalidJson))
				.andExpect(status().isBadRequest());
	}

	// === Тесты для контроллеров (UserController) ===

	@Test
	public void shouldReturn400ForEmptyUserRequest() throws Exception {
		String emptyJson = "{}";

		mockMvc.perform(post("/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content(emptyJson))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void shouldReturn400ForInvalidUserData() throws Exception {
		// Неверные данные: email без @, логин с пробелами, будущая дата рождения
		String invalidJson = "{\"email\":\"invalid\", \"login\":\"log in\", \"birthday\":\"2030-01-01\"}";

		mockMvc.perform(post("/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content(invalidJson))
				.andExpect(status().isBadRequest());
	}
}
