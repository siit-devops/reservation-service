package com.devops.reservation_service;

import com.devops.reservation_service.repository.ReservationRepository;
import com.devops.reservation_service.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TestReservationService {

    @InjectMocks
    private ReservationService reservationService;

    @Mock
    private ReservationRepository reservationRepository;

    @BeforeEach
    void setUp() {

    }

    @Test
    void testGetUsersReservations() {

    }

}
