package com.example.testrediscache.controller;

import com.example.testrediscache.model.Programmer;
import com.example.testrediscache.service.ProgrammerService;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController(value = "/programmers")
@Slf4j
public class ProgrammerController {

  @Autowired
  private ProgrammerService programmerService;

  @GetMapping
  public List<Programmer> findProgrammerByMainLanguage(
      @RequestParam(required = false) String mainLanguage) {
    log.info("findProgrammersByMainLanguage-" + mainLanguage);
    return Optional.ofNullable(mainLanguage)
        .map(programmerService::findProgrammersByMainLanguage)
        .orElse(programmerService.findAllProgrammers());
  }

  @PostMapping
  public ResponseEntity<Object> saveProgrammer(@RequestBody Programmer programmer) {
    log.info("saveProgrammer-" + programmer);
    return Optional.ofNullable(programmer)
        .map(programmerService::saveProgrammer)
        .map(saved -> ResponseEntity.noContent().build())
        .orElse(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Error saving programmer" + programmer));
  }
}
