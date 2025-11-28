package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Slf4j
public class User {
    private Long id;  // ID генерируется в контроллере
    private Set<Long> friends = new HashSet<>();;  // друзьяшки

    @Email(message = "Email должен содержать символ @")
    @NotBlank(message = "Email не может быть пустым")
    private String email;

    @NotBlank(message = "Логин не может быть пустым")
    private String login;

    private String name;  // Может быть пустым, тогда используется login

    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;

    // Кастомная валидация для login (без пробелов)
    public boolean isValidLogin() {
        return login != null && !login.contains(" ");
    }
}
