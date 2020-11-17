package com.example.testrediscache.repository;

import com.example.testrediscache.model.Programmer;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgrammerRepository extends JpaRepository<Programmer, String> {

  List<Programmer> findByMainLanguage(String mainLanguage);
}
