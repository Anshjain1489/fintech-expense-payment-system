package com.ansh.fintech.controller;

import com.ansh.fintech.dto.HealthResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HealthControllerTest {

    @Test
    void testCheckHealthReturns200AndUpStatus() {
        HealthController controller = new HealthController();
        ResponseEntity<HealthResponse> response = controller.checkHealth();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("UP", response.getBody().getStatus());
        assertEquals("FinTech Expense & Payment API Service", response.getBody().getService());
    }
}
