package com.example.modulith.adoptions;

import com.example.modulith.adoptions.validation.Validations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@ResponseBody
class DogAdoptionHttpController {

    private final DogAdoptionService dogAdoptionService;

    DogAdoptionHttpController(DogAdoptionService dogAdoptionService) {
        this.dogAdoptionService = dogAdoptionService;
    }

    @PostMapping("/dogs/{dogId}/adoptions")
    void adopt(@PathVariable int dogId, @RequestParam String owner) {
        this.dogAdoptionService.adopt(dogId, owner);
    }
}

@Service
@Transactional
class DogAdoptionService {

    private final Validations validations;
    private final DogRepository repository;

    private final ApplicationEventPublisher publisher;

    DogAdoptionService(Validations validations, DogRepository repository, ApplicationEventPublisher publisher) {
        this.validations = validations;
        this.repository = repository;
        this.publisher = publisher;
    }

    void adopt(int dogId, String owner) {
        this.repository.findById(dogId).ifPresent(dog -> {
            var updated = this.repository.save(new Dog(dog.id(), dog.name(), dog.description(), owner));
            System.out.println("updated [" + updated + "]");
            this.publisher.publishEvent(new DogAdoptionEvent(dogId));
        });
    }

}

interface DogRepository extends ListCrudRepository<Dog, Integer> {
}

record Dog(@Id int id, String name, String description, String owner) {
}