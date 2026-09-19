package pl.piomin.services;

import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestClient;
import pl.piomin.services.domain.Person;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PersonControllerTests {

    private static final String API_PATH = "/api";

    @Value("${local.server.port}")
    private int port;

    private RestClient restClient;

    @BeforeEach
    void setUp() {
        restClient = RestClient.create("http://localhost:" + port);
    }

    @Test
    @Order(1)
    void add() {
        Person obj = restClient.post()
                .uri(API_PATH)
                .body(Instancio.create(Person.class))
                .retrieve()
                .body(Person.class);
        assertNotNull(obj);
        assertEquals(1, obj.getId());
    }

    @Test
    @Order(2)
    void findAll() {
        Person[] objs = restClient.get()
                .uri(API_PATH)
                .retrieve()
                .body(Person[].class);
        assertTrue(objs.length > 0);
    }

    @Test
    @Order(2)
    void findById() {
        Person obj = restClient.get()
                .uri(API_PATH + "/{id}", 1L)
                .retrieve()
                .body(Person.class);
        assertNotNull(obj);
        assertEquals(1, obj.getId());
    }

    @Test
    @Order(3)
    void delete() {
        restClient.delete()
                .uri(API_PATH + "/{id}", 1L)
                .retrieve()
                .toBodilessEntity();
        Person obj = restClient.get()
                .uri(API_PATH + "/{id}", 1L)
                .retrieve()
                .body(Person.class);
        assertNull(obj);
    }

}
