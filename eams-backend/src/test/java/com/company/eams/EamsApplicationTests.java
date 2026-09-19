package com.company.eams;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = EamsApplication.class)
@ActiveProfiles("test")
class EamsApplicationTests {

    @Test
    void contextLoads() {
    }
}
