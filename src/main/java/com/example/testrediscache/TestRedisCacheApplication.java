package com.example.testrediscache;

import com.example.testrediscache.model.Programmer;
import com.example.testrediscache.repository.ProgrammerRepository;
import java.util.stream.IntStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Component;

@SpringBootApplication
@EnableCaching
public class TestRedisCacheApplication {

  public static void main(String[] args) {
    SpringApplication.run(TestRedisCacheApplication.class, args);
  }

  @Component
  class ProgrammersCreator implements CommandLineRunner {

    @Autowired
    private ProgrammerRepository programmerRepository;

    @Override
    public void run(final String... args) {
      IntStream.range(1, 4)
          .mapToObj(idx -> Programmer.builder().nick("nick-" + idx).mainLanguage("ruby").build())
          .forEach(programmerRepository::save);
    }

  }

}
