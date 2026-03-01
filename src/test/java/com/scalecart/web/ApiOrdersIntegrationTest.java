package com.scalecart.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scalecart.dto.OrderCreateDto;
import com.scalecart.dto.OrderItemDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class ApiOrdersIntegrationTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createOrder_withoutAuth_returns401() throws Exception {
        OrderCreateDto dto = new OrderCreateDto(1L, List.of(new OrderItemDto(1L, 1)));
        mvc.perform(post("/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void createOrder_withValidJwt_returns201() throws Exception {
        String token = loginAndGetToken();
        OrderCreateDto dto = new OrderCreateDto(1L, List.of(new OrderItemDto(1L, 1)));
        mvc.perform(post("/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.customerId").value(1))
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void getOrder_withValidJwt_returns200() throws Exception {
        String token = loginAndGetToken();
        // Create an order first
        OrderCreateDto create = new OrderCreateDto(1L, List.of(new OrderItemDto(1L, 1)));
        ResultActions createResult = mvc.perform(post("/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(create)));
        createResult.andExpect(status().isCreated());
        Long orderId = objectMapper.readTree(createResult.andReturn().getResponse().getContentAsString()).get("id").asLong();

        mvc.perform(get("/v1/orders/" + orderId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(orderId))
            .andExpect(jsonPath("$.customerId").value(1));
    }

    private String loginAndGetToken() throws Exception {
        String body = mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"user\",\"password\":\"password\"}"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        return objectMapper.readTree(body).get("token").asText();
    }
}
