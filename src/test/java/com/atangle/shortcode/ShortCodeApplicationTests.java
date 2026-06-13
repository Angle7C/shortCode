package com.atangle.shortcode;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import javax.sql.DataSource;

@SpringBootTest
class ShortCodeApplicationTests {

    @MockitoBean
    private DataSource dataSource;

    @Test
    void contextLoads() {
    }

}
