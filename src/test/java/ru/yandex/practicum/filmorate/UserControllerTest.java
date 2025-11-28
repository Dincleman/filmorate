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

import jakarta.validation.Validator;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    // Воспроизвести валидатор для использования в тесте
    @Autowired
    private Validator validator;

    private User createSampleUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("john@example.com");
        user.setLogin("john");
        user.setName("John");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    /** Тесты успешных сценариев **/

    @Test
    void getUserById_ShouldReturnUser() throws Exception {
        User user = createSampleUser();
        when(userService.getUserById(1L)).thenReturn(user);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(user)));
    }

    @Test
    void createUser_ShouldReturnCreatedUser() throws Exception {
        User user = createSampleUser();
        when(userService.createUser(any(User.class))).thenReturn(user);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(user)));
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser() throws Exception {
        User user = createSampleUser();
        when(userService.updateUser(any(User.class))).thenReturn(user);

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(user)));
    }

    @Test
    void getAllUsers_ShouldReturnUserList() throws Exception {
        List<User> users = Arrays.asList(createSampleUser());
        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(users)));
    }

    @Test
    void addFriend_ShouldReturnOk() throws Exception {
        doNothing().when(userService).addFriend(1L, 2L);
        mockMvc.perform(put("/users/1/friends/2"))
                .andExpect(status().isOk());
        verify(userService).addFriend(1L,2L);
    }

    @Test
    void removeFriend_ShouldReturnOk() throws Exception {
        doNothing().when(userService).removeFriend(1L, 2L);
        mockMvc.perform(delete("/users/1/friends/2"))
                .andExpect(status().isOk());
        verify(userService).removeFriend(1L,2L);
    }

    @Test
    void getAllFriends_ShouldReturnFriendList() throws Exception {
        List<User> friends = Arrays.asList(createSampleUser());
        when(userService.getAllFriends(1L)).thenReturn(friends);

        mockMvc.perform(get("/users/1/friends"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(friends)));
    }

    @Test
    void getCommonFriends_ShouldReturnCommonFriends() throws Exception {
        List<User> commonFriends = Arrays.asList(createSampleUser());
        when(userService.getCommonFriends(1L, 2L)).thenReturn(commonFriends);

        mockMvc.perform(get("/users/1/friends/common/2"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(commonFriends)));
    }

    /** Тесты на валидацию **/

    @Test
    void createUser_InvalidEmail_ShouldReturn400() throws Exception {
        User invalidUser = createSampleUser();
        invalidUser.setEmail("invalidEmail"); // без @

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest()); // Предполагается, что есть глобальный обработчик ошибок
    }

    @Test
    void createUser_BlankLogin_ShouldReturn400() throws Exception {
        User invalidUser = createSampleUser();
        invalidUser.setLogin(" "); // пустой логин

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_FutureBirthday_ShouldReturn400() throws Exception {
        User invalidUser = createSampleUser();
        invalidUser.setBirthday(LocalDate.now().plusDays(1)); // дата в будущем

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_BlankEmail_ShouldReturn400() throws Exception {
        User invalidUser = createSampleUser();
        invalidUser.setEmail("");// пустая строка

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());
    }
}