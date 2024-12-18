package com.example.capston3.Service;


import com.example.capston3.ApiResponse.ApiException;
import com.example.capston3.DTO.MaintenanceRequestDTO;
import com.example.capston3.DTO.MaintenanceRequestHistoryDTO;
import com.example.capston3.DTO.MotorcycleDTO;
import com.example.capston3.InDTO.MaintenanceRequestDTO_In;
import com.example.capston3.Model.MaintenanceExpert;
import com.example.capston3.Model.MaintenanceRequest;
import com.example.capston3.Model.Motorcycle;
import com.example.capston3.Model.Owner;
import com.example.capston3.Repository.MaintenanceExpertRepository;
import com.example.capston3.Repository.MaintenanceRequestRepository;
import com.example.capston3.Repository.MotorcycleRepository;
import com.example.capston3.Repository.OwnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MaintenanceRequestService {

    private final MaintenanceRequestRepository maintenanceRequestRepository;
    private final OwnerRepository ownerRepository;
    private final MotorcycleRepository motorcycleRepository;
    private final MaintenanceExpertRepository maintenanceExpertRepository;



//    public List<MaintenanceRequestDTO> getAllMaintenanceRequest(){
//
//        List<MaintenanceRequest> maintenanceRequests = maintenanceRequestRepository.findAll();
//
//        List<MaintenanceRequestDTO> maintenanceRequestDTOS = new ArrayList<>();
//
//        for(MaintenanceRequest maintenanceRequest : maintenanceRequests){
//            Motorcycle motorcycle = motorcycleRepository.findMotorcycleById(maintenanceRequest.getMotorcycle_id());
//
//            MaintenanceRequestDTO motorcycleDTOS = new MaintenanceRequestDTO(maintenanceRequest.getRequestDate(),maintenanceRequest.getTotalPrice(),maintenanceRequest.getExpert_name(),maintenanceRequest.getStatus(),maintenanceRequest.getPickupDate(),maintenanceRequest.);
//            maintenanceRequestDTOS.add(motorcycleDTOS);
//        }
//        return maintenanceRequestDTOS;
//    }

    public List<MaintenanceRequestDTO> getAllMaintenanceRequest() {
        // Step 1: Fetch all maintenance requests
        List<MaintenanceRequest> maintenanceRequests = maintenanceRequestRepository.findAll();

        // Step 2: Map each maintenance request to a DTO with its motorcycle details
        return maintenanceRequests.stream().map(maintenanceRequest -> {
            // Fetch motorcycle details
            Motorcycle motorcycle = motorcycleRepository.findMotorcycleById(maintenanceRequest.getMotorcycle_id());
            if (motorcycle == null) {
                throw new ApiException("Motorcycle not found for MaintenanceRequest ID " + maintenanceRequest.getId());
            }

            // Map motorcycle to MotorcycleDTO
            MotorcycleDTO motorcycleDTO = new MotorcycleDTO(
                    motorcycle.getBrand(),
                    motorcycle.getModel(),
                    motorcycle.getYear(),
                    motorcycle.getPrice(),
                    motorcycle.getColor(),
                    motorcycle.getIsForSale(),
                    motorcycle.getIsAvailable()
            );

            // Map MaintenanceRequest to MaintenanceRequestDTO
            return new MaintenanceRequestDTO(
                    maintenanceRequest.getRequestDate(),
                    maintenanceRequest.getTotalPrice(),
                    maintenanceRequest.getExpert_name(),
                    maintenanceRequest.getStatus(),
                    maintenanceRequest.getPickupDate(),
                    motorcycleDTO
            );
        }).collect(Collectors.toList());

    }
    public void addMaintenanceRequest(MaintenanceRequestDTO_In maintenanceRequestDTO_in){

        Motorcycle motorcycle = motorcycleRepository.findMotorcycleById(maintenanceRequestDTO_in.getMotorcycle_id());
        if(motorcycle == null){
            throw new ApiException("Motorcycle not found");
        }

        Owner owner = ownerRepository.findOwnerById(maintenanceRequestDTO_in.getOwner_id());
        if(owner == null)
            throw new ApiException("Owner not found");


        if (maintenanceRequestDTO_in.getPickupDate().isBefore(LocalDate.now())) {
            throw new ApiException("Pickup date cannot be in the past!");
        }

        MaintenanceExpert expert = maintenanceExpertRepository.findMaintenanceExpertByName(maintenanceRequestDTO_in.getExpert_name());
        if (expert == null) {
            throw new ApiException("Expert not found!");
        }

        Double totalPrice = calculateTotalPrice(expert, maintenanceRequestDTO_in.getPickupDate());

        MaintenanceRequest maintenanceRequest = new MaintenanceRequest(maintenanceRequestDTO_in.getExpert_name(),maintenanceRequestDTO_in.getPickupDate(),owner,maintenanceRequestDTO_in.getMotorcycle_id());

        maintenanceRequest.setStatus("Pending");
        maintenanceRequest.setTotalPrice(totalPrice);

        maintenanceRequestRepository.save(maintenanceRequest);

    }

    //method to calculate total price
    private Double calculateTotalPrice(MaintenanceExpert expert, LocalDate pickupDate) {
        // calc price based on expert daily rate and number of days
        Double numberOfDays = (double) Duration.between(LocalDate.now().atStartOfDay(), pickupDate.atStartOfDay()).toDays();

        // Calculate total price as the daily rate times the number of days
        return expert.getMaintenancePricePerDay() * numberOfDays;
    }


    public void updateMaintenanceRequest(Integer maintenanceRequest_id, MaintenanceRequestDTO_In maintenanceRequestDTO_in){

        MaintenanceRequest maintenanceRequest = maintenanceRequestRepository.findMaintenanceRequestById(maintenanceRequest_id);

        if(maintenanceRequest ==null)
            throw new ApiException("MaintenanceRequest not found!");

        maintenanceRequest.setExpert_name(maintenanceRequestDTO_in.getExpert_name());
        maintenanceRequest.setPickupDate(maintenanceRequestDTO_in.getPickupDate());


        if (maintenanceRequestDTO_in.getOwner_id() != null) {
            Owner owner = ownerRepository.findOwnerById(maintenanceRequestDTO_in.getOwner_id());
            if(owner== null)
                throw new ApiException("Owner not found !");

            maintenanceRequest.setOwner(owner);
        }

        if (maintenanceRequestDTO_in.getMotorcycle_id() != null) {
            maintenanceRequest.setMotorcycle_id(maintenanceRequestDTO_in.getMotorcycle_id());
        }

        if (maintenanceRequestDTO_in.getPickupDate() != null) {
            MaintenanceExpert expert = maintenanceExpertRepository.findMaintenanceExpertByName(maintenanceRequestDTO_in.getExpert_name());
            if (expert == null) {
                throw new ApiException("Expert not found");
            }

            // calculate again the total price if the pickupdate is changed
            Double newTotalPrice = calculateTotalPrice(expert, maintenanceRequestDTO_in.getPickupDate());
            maintenanceRequest.setTotalPrice(newTotalPrice);
        }

        maintenanceRequestRepository.save(maintenanceRequest);
    }

    public void updateMaintenanceRequestStatusToCompleted(Integer maintenanceRequest_id, String expertName) {
        MaintenanceRequest maintenanceRequest = maintenanceRequestRepository.findMaintenanceRequestById(maintenanceRequest_id);

        if (maintenanceRequest == null)
            throw new ApiException("MaintenanceRequest not found!");

        // Check if the current expert is the one assigned to the request
        if (!maintenanceRequest.getExpert_name().equalsIgnoreCase(expertName)) {
            throw new ApiException("Only the expert can mark the maintenance request as completed!");
        }

        // Only allow the status to be updated if the request is in 'Pending' status
        if (!"Pending".equalsIgnoreCase(maintenanceRequest.getStatus())) {
            throw new ApiException("Maintenance request is not in a Pending status, it cannot be marked as completed!");
        }

        // Update status
        maintenanceRequest.setStatus("Completed");
        maintenanceRequestRepository.save(maintenanceRequest);
    }


    public void deleteMaintenanceRequest(Integer maintenanceRequest_id ){

        MaintenanceRequest maintenanceRequest = maintenanceRequestRepository.findMaintenanceRequestById(maintenanceRequest_id);

        if(maintenanceRequest == null)
            throw new ApiException("MaintenanceRequest not found!");

        // Check if the pickupDate is after the current date (meaning the expert has completed their work)
        if (maintenanceRequest.getPickupDate() != null && maintenanceRequest.getPickupDate().isAfter(LocalDate.now())) {
            throw new ApiException("Cannot delete this Maintenance Request !");
        }

        maintenanceRequestRepository.delete(maintenanceRequest);

    }

    //Raghad ahmad
    public List<MaintenanceRequestHistoryDTO> getMaintenanceHistory() {
        // Step 1: Fetch all maintenance requests
        List<MaintenanceRequest> maintenanceRequests = maintenanceRequestRepository.findAll();

        // Step 2: Map MaintenanceRequest to MaintenanceRequestDTO
        return maintenanceRequests.stream().map(request -> {
            // Fetch motorcycle information based on motorcycle_id
            Motorcycle motorcycle = motorcycleRepository.findById(request.getMotorcycle_id())
                    .orElseThrow(() -> new ApiException("Motorcycle not found for maintenance request"));

            // Map motorcycle to DTO
            MotorcycleDTO motorcycleDTO = new MotorcycleDTO(
                    motorcycle.getBrand(),
                    motorcycle.getModel(),
                    motorcycle.getYear(),
                    motorcycle.getPrice(),
                    motorcycle.getColor(),
                    motorcycle.getIsAvailable(),
                    motorcycle.getIsForSale()
            );

            // Map maintenance request to DTO
            MaintenanceRequestHistoryDTO dto = new MaintenanceRequestHistoryDTO();
            dto.setRequestDate(request.getRequestDate());
            dto.setTotalPrice(request.getTotalPrice());
            dto.setExpertName(request.getExpert_name());
            dto.setStatus(request.getStatus());
            dto.setPickupDate(request.getPickupDate());
            dto.setMotorcycleS(List.of(motorcycleDTO)); // Add the motorcycle information

            return dto;
        }).collect(Collectors.toList());
    }



}