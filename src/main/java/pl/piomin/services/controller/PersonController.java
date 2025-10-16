package pl.piomin.services.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import pl.piomin.services.domain.Person;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class PersonController {

    private final String idPath = "/{id}";
    private final Logger log = LoggerFactory.getLogger(PersonController.class);
    private final List<Person> objs = new ArrayList<>();

    @GetMapping
    public List<Person> findAll() {
        return objs;
    }

    @GetMapping(idPath)
    public Person findById(@PathVariable("id") Long id) {
        Person obj = objs.stream().filter(it -> it.getId().equals(id))
                .findFirst()
                .orElseThrow();
        log.info("Found: {}", obj.getId());
        return obj;
    }

    @PostMapping
    public Person add(@RequestBody Person obj) {
        obj.setId((long) (objs.size() + 1));
        log.info("Added: {}", obj);
        objs.add(obj);
        return obj;
    }

    @DeleteMapping(idPath)
    public void delete(@PathVariable("id") Long id) {
        Person obj = objs.stream().filter(it -> it.getId().equals(id)).findFirst().orElseThrow();
        objs.remove(obj);
        log.info("Removed: {}", id);
    }

    @PutMapping
    public void update(@RequestBody Person obj) {
        Person objTmp = objs.stream()
                .filter(it -> it.getId().equals(obj.getId()))
                .findFirst()
                .orElseThrow();
        objs.set(objs.indexOf(objTmp), obj);
        log.info("Updated: {}", obj.getId());
    }

}