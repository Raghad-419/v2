package com.example.capston3.Service;
import com.example.capston3.ApiResponse.ApiException;
import com.example.capston3.DTO.CourseDTO;
import com.example.capston3.Model.Course;
import com.example.capston3.Model.Owner;
import com.example.capston3.Repository.CourseRepository;
import com.example.capston3.Repository.OwnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

        private final CourseRepository courseRepository;
        private final OwnerRepository ownerRepository;


        public List<CourseDTO> getAllCourses(){

            List<Course> courses = courseRepository.findAll();

            List<CourseDTO> courseDTOS = new ArrayList<>();

            for(Course course : courses){
                CourseDTO courseDTO = new CourseDTO(course.getName(), course.getDescription(), course.getPrice(), course.getDuration());

                courseDTOS.add(courseDTO);
            }
            return courseDTOS;
        }

        public void addCourse(Integer owner_id, Course course) {

            Owner owner = ownerRepository.findOwnerById(owner_id);

            if (owner == null)
                throw new ApiException("Owner not found!");

            //assign course to one owner
            course.setOwner(owner);
            courseRepository.save(course);
        }


        public void updateCourse(Integer id, Course course) {

            Course c = courseRepository.findCourseById(id);
            if (c == null)
                throw new ApiException("Course not found!");

            c.setName(course.getName());
            c.setDescription(c.getDescription());
            c.setPrice(course.getPrice());
            c.setDuration(course.getDuration());
            courseRepository.save(c);
        }

        public void deleteCourse(Integer id){

            Course course = courseRepository.findCourseById(id);
            if(course == null)
                throw new ApiException("Course not found!");

            courseRepository.delete(course);

        }




//Raghad
    public List<CourseDTO> filterCourses(Double minPrice, Double maxPrice, Integer minDuration, Integer maxDuration) {
        // Fetch filtered courses from the repository
        List<Course> courses = courseRepository.filterCourses(minPrice, maxPrice, minDuration, maxDuration);

        // Map courses to CourseDTOs
        return courses.stream().map(course -> new CourseDTO(
                course.getName(),
                course.getDescription(),
                course.getPrice(),
                course.getDuration() // Include trainer name in the DTO
        )).collect(Collectors.toList());
    }
}



