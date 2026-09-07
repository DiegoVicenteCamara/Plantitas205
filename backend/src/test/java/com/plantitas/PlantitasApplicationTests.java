package com.plantitas;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:plantitasdb-app;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
class PlantitasApplicationTests {

	@Test
	void contextLoads() {
	}
}
