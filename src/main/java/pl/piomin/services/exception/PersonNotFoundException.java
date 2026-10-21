package pl.piomin.services.exception;

public class PersonNotFoundException extends RuntimeException {

    public PersonNotFoundException(Long id) {
        super("Person not found: " + id);
    }
}
