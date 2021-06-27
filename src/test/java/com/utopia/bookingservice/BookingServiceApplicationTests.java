package com.utopia.bookingservice;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.utopia.bookingservice.controller.BookingController;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@AutoConfigureMockMvc
class BookingServiceApplicationTests {
    @Autowired
    private BookingController bookingController;

    @Test
    void contextLoads() {
        assertThat(bookingController, is(notNullValue()));
    }
}
