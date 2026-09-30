package pl.piomin.services.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.piomin.services.domain.Person;

public interface PersonRepository extends JpaRepository<Person, Long> {
}
