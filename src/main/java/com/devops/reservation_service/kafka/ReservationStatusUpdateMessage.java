package com.devops.reservation_service.kafka;

import com.devops.reservation_service.model.enumerations.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservationStatusUpdateMessage implements Serializable{
    private UUID senderId;
    private UUID receiverId;
    private UUID reservationId;
    private ReservationStatus status;
}
