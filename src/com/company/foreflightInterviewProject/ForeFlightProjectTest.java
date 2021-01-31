package com.company.foreflightInterviewProject;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ForeFlightProjectTest {


    @Test
    void convertSpeed() {
        double result = ForeFlightProject.convertSpeed(10.00);
        assertEquals(result, 11.5);
    }

    @Test
    void convertTemp() {
        double result = ForeFlightProject.convertTemp(10.00);
        assertEquals(result, 50);
    }

    @Test
    void convertDegrees() {
        String result = ForeFlightProject.convertDegrees(360);
        assertEquals(result, "North");
    }
}