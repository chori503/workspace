package com.workspace.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workspace.client.PaymentClient;
import com.workspace.dto.payment.PaymentGatewayResponse;
import com.workspace.dto.payment.PaymentGatewayStatus;
import com.workspace.entity.AppUser;
import com.workspace.entity.Role;
import com.workspace.entity.Space;
import com.workspace.entity.SpaceStatus;
import com.workspace.entity.SpaceType;
import com.workspace.repository.SpaceRepository;
import com.workspace.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ReservationFlowIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SpaceRepository spaceRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private PaymentClient paymentClient;

    @Test
    void creaUnaReservaConfirmadaDeExtremoAExtremo() throws Exception {
        when(paymentClient.charge(any()))
                .thenReturn(new PaymentGatewayResponse(PaymentGatewayStatus.APPROVED, "ref-integracion", null));

        AppUser user = new AppUser();
        user.setEmail("flujo@test.com");
        user.setPassword(passwordEncoder.encode("12345678"));
        user.setFullName("Usuario Flujo");
        user.setRole(Role.USER);
        user.setCreatedAt(OffsetDateTime.now());
        userRepository.save(user);

        Space space = new Space();
        space.setName("Sala de prueba");
        space.setType(SpaceType.MEETING_ROOM);
        space.setCapacity(4);
        space.setHourlyRate(new BigDecimal("10.00"));
        space.setStatus(SpaceStatus.ACTIVE);
        spaceRepository.save(space);

        String authBody = """
                {"email":"flujo@test.com","password":"12345678"}
                """;
        String authResponse = mockMvc.perform(post("/api/users/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(authBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(authResponse).get("token").asText();

        String reservationBody = """
                {
                  "userId": %d,
                  "spaceId": %d,
                  "cardDetails": {"cardNumber":"4111111111111111","expiration":"03/29","cvc":"029"},
                  "reservationDate": "%s",
                  "startTime": 10,
                  "endTime": 12
                }
                """.formatted(user.getId(), space.getId(), LocalDate.now().plusDays(2));

        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservationBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }
}
