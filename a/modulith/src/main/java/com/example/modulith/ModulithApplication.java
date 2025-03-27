package com.example.modulith;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.integration.util.CheckedRunnable;
import org.springframework.modulith.events.IncompleteEventPublications;

@SpringBootApplication
public class ModulithApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModulithApplication.class, args);
    }

}

class YouIncompleteMeApplicationRunner implements ApplicationRunner {

    private final IncompleteEventPublications publications;

    YouIncompleteMeApplicationRunner(IncompleteEventPublications publications) {
        this.publications = publications;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        LockRegistry lockRegistry = null; // todo
        try {
            lockRegistry.executeLocked("myInitializations", (CheckedRunnable<Throwable>) () -> publications.resubmitIncompletePublications(eventPublication ->
                    eventPublication.getIdentifier().clockSequence() > 0));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        ;
//		this.publications
//				.resubmitIncompletePublications(eventPublication ->
//						eventPublication.getIdentifier());
    }
}
