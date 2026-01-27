package com.project.passwordmanager.auth.controller;

import com.project.passwordmanager.PasswordManager.auth.domain.model.User;
import com.project.passwordmanager.PasswordManager.auth.domain.service.PasswordStrengthValidator;
import com.project.passwordmanager.PasswordManager.auth.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PasswordResetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordStrengthValidator passwordStrengthValidator;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private final ByteArrayOutputStream consoleOut = new ByteArrayOutputStream();
    private PrintStream originalOut;

    @BeforeEach
    void setup() {
        originalOut = System.out;
        System.setOut(new PrintStream(consoleOut));
    }

    @AfterEach
    void teardown() {
        System.setOut(originalOut);
    }

    @Test
    void testResetPasswordSuccess() throws Exception {

        String email = "teste@email.com";
        String novaSenha = "NovaSenha123";

        User user = new User();
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(novaSenha)).thenReturn("HASH");
        doNothing().when(passwordStrengthValidator).validate(novaSenha);

        mockMvc.perform(post("/auth/forgot-password")
                        .param("email", email))
                .andExpect(status().isOk());

        String console = consoleOut.toString();
        assertFalse(console.isBlank(), "Nada foi impresso no console");
        assertTrue(console.contains("token="), "Nenhum token foi impresso no console");

        String token = extrairToken(console);

        assertNotNull(token, "Token extraído é nulo");
        assertFalse(token.isBlank(), "Token está vazio");
        assertTrue(token.length() > 10, "Token parece inválido");

        mockMvc.perform(post("/auth/reset-password")
                        .param("token", token)
                        .param("newPassword", novaSenha))
                .andExpect(status().isOk());

        verify(userRepository, times(1)).save(any(User.class));
    }

    private String extrairToken(String console) {
        int idx = console.indexOf("token=");
        if (idx == -1) return null;

        String restante = console.substring(idx + 6);
        int fim = restante.indexOf("\n");
        if (fim != -1) {
            return restante.substring(0, fim).trim();
        }
        return restante.trim();
    }
    @Test
    void testForgotPasswordEmailNotFound() throws Exception {

        String email = "naoExiste@email.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        mockMvc.perform(post("/auth/forgot-password")
                        .param("email", email))
                .andExpect(status().isBadRequest()); // ou 404, depende do seu controller

        verify(userRepository, never()).save(any());
    }
    @Test
    void testResetPasswordWeakPassword() throws Exception {

        String email = "teste@email.com";

        User user = new User();
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        doThrow(new RuntimeException("Senha fraca"))
                .when(passwordStrengthValidator).validate("fraca");

        mockMvc.perform(post("/auth/reset-password")
                        .param("token", "TOKEN123")
                        .param("newPassword", "fraca"))
                .andExpect(status().isBadRequest());

        verify(userRepository, never()).save(any());
    }
    @Test
    void testResetPasswordTokenInvalido() throws Exception {

        mockMvc.perform(post("/auth/reset-password")
                        .param("token", "TOKEN_INEXISTENTE")
                        .param("newPassword", "NovaSenha123"))
                .andExpect(status().isBadRequest()); // depende do seu controller
    }
    @Test
    void testResetPasswordSemToken() throws Exception {

        mockMvc.perform(post("/auth/reset-password")
                        .param("newPassword", "NovaSenha123"))
                .andExpect(status().isBadRequest()); // ou 400
    }
    @Test
    void testResetPasswordSemNovaSenha() throws Exception {

        mockMvc.perform(post("/auth/reset-password")
                        .param("token", "TOKEN123"))
                .andExpect(status().isBadRequest());
    }
    @Test
    void testForgotPasswordSemEmail() throws Exception {

        mockMvc.perform(post("/auth/forgot-password"))
                .andExpect(status().isBadRequest());
    }
    @Test
    void testForgotPasswordApenasGeraToken() throws Exception {

        String email = "teste@email.com";

        User user = new User();
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        mockMvc.perform(post("/auth/forgot-password")
                        .param("email", email))
                .andExpect(status().isOk());

        // não deve salvar o usuário aqui
        verify(userRepository, never()).save(any());
    }

}
