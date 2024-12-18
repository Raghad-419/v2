package com.example.capston3.Service;


import com.example.capston3.ApiResponse.ApiException;
import com.example.capston3.DTO.EventRegistrationDTO;
import com.example.capston3.Model.EventRegistration;
import com.example.capston3.Repository.EventRegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventRegistrationService {

    private final EventRegistrationRepository eventRegistrationRepository;

    public List<EventRegistrationDTO> getEventRegistrations() {
        List<EventRegistration> eventRegistrations = eventRegistrationRepository.findAll();
        List<EventRegistrationDTO> eventRegistrationOutDTOs = new ArrayList<>();
        for (EventRegistration eventRegistration : eventRegistrations) {
            eventRegistrationOutDTOs.add(new EventRegistrationDTO(eventRegistration.getEvent().getId(),eventRegistration.getOwner().getId(),eventRegistration.getUser().getId()));
        }
        return eventRegistrationOutDTOs;
    }
    public void addEventRegistration(EventRegistration eventRegistration) {
        eventRegistrationRepository.save(eventRegistration);
    }

    public void updateEventRegistration(Integer id,EventRegistration eventRegistration) {
        EventRegistration eventRegistration1 = eventRegistrationRepository.findEventRegistrationById(id);
        if (eventRegistration1 == null) {
            throw new ApiException("Event Registration Not Found");
        }
        eventRegistration1.setEvent(eventRegistration.getEvent());
        eventRegistration1.setOwner(eventRegistration.getOwner());
        eventRegistration1.setUser(eventRegistration.getUser());
        eventRegistrationRepository.save(eventRegistration1);
    }

    public void deleteEventRegistration(Integer id) {
        EventRegistration eventRegistration = eventRegistrationRepository.findEventRegistrationById(id);
        if (eventRegistration == null) {
            throw new ApiException("Event Registration Not Found");
        }
        eventRegistrationRepository.delete(eventRegistration);
    }







}