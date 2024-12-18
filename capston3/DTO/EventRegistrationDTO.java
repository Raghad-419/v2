package com.example.capston3.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EventRegistrationDTO {

    private Integer event_id;

    private Integer owner_id;

    private Integer user_id;
}