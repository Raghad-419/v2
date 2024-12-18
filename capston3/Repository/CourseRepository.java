package com.example.capston3.Repository;

import com.example.capston3.Model.Course;
import com.example.capston3.Model.Owner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course,Integer> {
    Course findCourseById(Integer id);
    List<Course> findCoursesByOwnerId(Integer id);
}
