package com.waveinformatica.demo.api;

import com.waveinformatica.demo.dto.EditablePersonDTO;
import com.waveinformatica.demo.entities.Person;
import com.waveinformatica.demo.services.PeopleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class PeopleApiController {

    @Autowired
    private PeopleService peopleService;

    @PostMapping("/people")
    @ResponseStatus(HttpStatus.CREATED)
    public void addPerson(@RequestBody Person p) {
        if (p.getId() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID must be empty");
        }

        peopleService.addPerson(p);
    }

    @GetMapping("/people")
    public List<Person> findPeople(@RequestParam(value = "lastName", required = false) String lastName) {
        return peopleService.findPeople(lastName);
    }

    @GetMapping("/people/{id}")
    public Person getPerson(@PathVariable("id") long id) {
        Person p = peopleService.getPerson(id);
        if (p == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Person not found");
        }
        return p;
    }

    @DeleteMapping("/people/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePerson(@PathVariable("id") long id) {
        if (!peopleService.deletePerson(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Person not found");
        }
    }

    @PutMapping("/people/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updatePerson(@PathVariable("id") long id, @RequestBody Person p) {
        if (p.getId() != null && p.getId() != id) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID must be empty");
        }

        p.setId(id);
        if (!peopleService.update(p)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Person not found");
        }
    }

    @PatchMapping("/people/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void patchPerson(@PathVariable("id") long id, @RequestBody EditablePersonDTO p) {
        if (!peopleService.partialUpdate(id, p)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Person not found");
        }
    }
}
