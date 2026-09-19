package pl.piomin.services.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pl.piomin.services.domain.Person;
import pl.piomin.services.repository.PersonRepository;

import java.util.List;

@Service
public class PersonService {

    private static final Logger log = LoggerFactory.getLogger(PersonService.class);
    private final PersonRepository repository;

    public PersonService(PersonRepository repository) {
        this.repository = repository;
    }

    public List<Person> findAll() {
        return repository.findAll();
    }

    public Person findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public Person add(Person person) {
        person.setId(null);
        Person saved = repository.save(person);
        log.info("Added: {}", saved);
        return saved;
    }

    public void delete(Long id) {
        repository.deleteById(id);
        log.info("Removed: {}", id);
    }

    public Person update(Person person) {
        Person updated = repository.save(person);
        log.info("Updated: {}", updated.getId());
        return updated;
    }
}
