package com.example.modulith;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

//@Import(TestcontainersConfiguration.class)
@SpringBootTest
class ModulithApplicationTests {

	@Test
	void contextLoads() {
		var am = ApplicationModules.of(ModulithApplication.class);
		am.verify() ;

		System.out.println(am);

		new Documenter(am).writeDocumentation();

	}

}
