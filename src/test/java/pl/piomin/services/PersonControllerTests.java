package pl.piomin.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import pl.piomin.services.domain.Person;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PersonControllerTests {

    private static final String API_PATH = "/api";

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @Order(1)
    void add() throws Exception {
        Person person = Instancio.create(Person.class);
        person.setId(null);

        mockMvc.perform(post(API_PATH)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(person)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    @Test
    @Order(2)
    void findAll() throws Exception {
        mockMvc.perform(get(API_PATH)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))));
    }

    @Test
    @Order(2)
    void findById() throws Exception {
        mockMvc.perform(get(API_PATH + "/{id}", 1L)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    @Order(3)
    void deleteById() throws Exception {
        mockMvc.perform(delete(API_PATH + "/{id}", 1L)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());

        mockMvc.perform(get(API_PATH + "/{id}", 1L)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    @Order(4)
    void addWithoutAuthorizationShouldReturn401() throws Exception {
        Person person = Instancio.create(Person.class);
        person.setId(null);

        mockMvc.perform(post(API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(person)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(4)
    void addWithUserRoleShouldReturn403() throws Exception {
        Person person = Instancio.create(Person.class);
        person.setId(null);

        mockMvc.perform(post(API_PATH)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(person)))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(4)
    void getWithoutAuthorizationShouldReturn401() throws Exception {
        mockMvc.perform(get(API_PATH))
                .andExpect(status().isUnauthorized());
    }

    private String toJson(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }

}
