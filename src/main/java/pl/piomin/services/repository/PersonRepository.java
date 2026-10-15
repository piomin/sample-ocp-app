package pl.piomin.services.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.piomin.services.domain.Person;

import java.util.List;

public interface PersonRepository extends JpaRepository<Person, Long> {

    List<Person> findByAgeGreaterThan(int age);

    List<Person> findByAgeLessThan(int age);

    List<Person> findByNationality(String nationality);

    List<Person> findByAgeGreaterThanAndNationality(int age, String nationality);

    List<Person> findByAgeLessThanAndNationality(int age, String nationality);
}
