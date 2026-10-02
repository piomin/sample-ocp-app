package pl.piomin.services.controller;

import org.springframework.web.bind.annotation.*;
import pl.piomin.services.domain.Person;
import pl.piomin.services.service.PersonService;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PersonController {

    private final String idPath = "/{id}";
    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping
    public List<Person> findAll() {
        return personService.findAll();
    }

    @GetMapping(idPath)
    public Person findById(@PathVariable("id") Long id) {
        return personService.findById(id);
    }

    @PostMapping
    public Person add(@RequestBody Person obj) {
        return personService.add(obj);
    }

    @DeleteMapping(idPath)
    public void delete(@PathVariable("id") Long id) {
        personService.delete(id);
    }

    @PutMapping
    public Person update(@RequestBody Person obj) {
        return personService.update(obj);
    }

    @GetMapping("/age/gt/{age}")
    public List<Person> findByAgeGreaterThan(@PathVariable("age") int age) {
        return personService.findByAgeGreaterThan(age);
    }

    @GetMapping("/age/lt/{age}")
    public List<Person> findByAgeLessThan(@PathVariable("age") int age) {
        return personService.findByAgeLessThan(age);
    }

    @GetMapping("/nationality/{nationality}")
    public List<Person> findByNationality(@PathVariable("nationality") String nationality) {
        return personService.findByNationality(nationality);
    }

    @GetMapping("/age/gt/{age}/nationality/{nationality}")
    public List<Person> findByAgeGreaterThanAndNationality(@PathVariable("age") int age,
                                                           @PathVariable("nationality") String nationality) {
        return personService.findByAgeGreaterThanAndNationality(age, nationality);
    }

    @GetMapping("/age/lt/{age}/nationality/{nationality}")
    public List<Person> findByAgeLessThanAndNationality(@PathVariable("age") int age,
                                                        @PathVariable("nationality") String nationality) {
        return personService.findByAgeLessThanAndNationality(age, nationality);
    }

}
