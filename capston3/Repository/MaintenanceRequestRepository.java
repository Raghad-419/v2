package com.example.capston3.Repository;

import com.example.capston3.Model.MaintenanceRequest;
import com.example.capston3.Model.Motorcycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaintenanceRequestRepository extends JpaRepository<MaintenanceRequest,Integer> {
    MaintenanceRequest findMaintenanceRequestById(Integer id);

    List<MaintenanceRequest> findMaintenanceRequestByStatus(String status);

  //  List<MaintenanceRequest> findMaintenanceRequestByMotorcycle_id(Integer id);

}
