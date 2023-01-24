package com.kemalbeyaz.dynamic.datasource.person;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.apache.commons.lang3.RandomStringUtils;
import java.util.List;

@RestController
@RequestMapping("person")
public class PersonController {

    private final PersonRepository personRepository;

    @Autowired
    public PersonController(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @GetMapping()
    public List<Person> listAll() {
        return personRepository.findAll();
    }

    @GetMapping("/add-random")
    public Person generateRandomPerson() {
        Person person = new Person(RandomStringUtils.randomAlphabetic(15));
        personRepository.save(person);
        return person;
    }
}
