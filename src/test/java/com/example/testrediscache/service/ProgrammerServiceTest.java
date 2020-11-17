package com.example.testrediscache.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.testrediscache.TestRedisCacheApplication;
import com.example.testrediscache.model.Programmer;
import com.example.testrediscache.repository.ProgrammerRepository;
import java.util.Arrays;
import java.util.List;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.GenericContainer;

@RunWith(SpringRunner.class)
@ContextConfiguration(initializers = ProgrammerServiceTest.Initializer.class,
    classes = TestRedisCacheApplication.class)
public class ProgrammerServiceTest {

  private static final String PROGRAMMER_NICK = "thenick";
  private static final String PROGRAMMER_MAIN_LANGUAGE = "themainlanguage";

  private static final String NEW_PROGRAMMER_NICK = "thenewprogrammernick";
  private static final String NEW_PROGRAMMER_MAIN_LANGUAGE = "thenewprogrammermainlanguage";

  private static final String PROGRAMMERS_CACHE_NAME = "programmers";
  @ClassRule
  public static GenericContainer redis =
      new GenericContainer("redis").withExposedPorts(6379);
  @Autowired
  private ProgrammerService programmerService;
  @Autowired
  private ProgrammerRepository programmerRepository;
  @Autowired
  private CacheManager cacheManager;

  @Test
  public void shouldCacheProgrammersByMainLanguage() {
    //Given
    List<Programmer> programmers = Arrays.asList(
        Programmer.builder().nick(PROGRAMMER_NICK).mainLanguage(PROGRAMMER_MAIN_LANGUAGE).build());
    programmers.forEach(programmerRepository::save);

    // When
    List<Programmer> found = programmerService
        .findProgrammersByMainLanguage(PROGRAMMER_MAIN_LANGUAGE);

    //Then
    assertThat(found).isNotNull();
    assertThat(found).hasSize(programmers.size());
    assertThat(found.get(0)).extracting("nick", "mainLanguage")
        .contains(PROGRAMMER_NICK, PROGRAMMER_MAIN_LANGUAGE);

    Cache programmersCache = cacheManager.getCache(PROGRAMMERS_CACHE_NAME);
    assertThat(programmersCache).isNotNull();
    Cache.ValueWrapper cacheValue = programmersCache.get(PROGRAMMER_MAIN_LANGUAGE);
    assertThat(cacheValue).isNotNull();
    assertThat(cacheValue.get()).isEqualTo(programmers);
  }

  @Test
  public void shouldEvictProgrammersCache() {
    //Given
    List<Programmer> programmers = Arrays.asList(
        Programmer.builder().nick(PROGRAMMER_NICK).mainLanguage(NEW_PROGRAMMER_MAIN_LANGUAGE)
            .build());
    Cache programmersCache = cacheManager.getCache(PROGRAMMERS_CACHE_NAME);
    programmersCache.put(NEW_PROGRAMMER_MAIN_LANGUAGE, programmers);
    assertThat(programmersCache.get(NEW_PROGRAMMER_MAIN_LANGUAGE)).isNotNull();
    assertThat(programmersCache.get(NEW_PROGRAMMER_MAIN_LANGUAGE).get()).isEqualTo(programmers);

    Programmer toCreate = Programmer.builder().nick(NEW_PROGRAMMER_NICK)
        .mainLanguage(NEW_PROGRAMMER_MAIN_LANGUAGE).build();

    //When
    Programmer saved = programmerService.saveProgrammer(toCreate);

    //Then
    assertThat(saved).isNotNull();
    assertThat(saved).extracting("nick", "mainLanguage")
        .contains(NEW_PROGRAMMER_NICK, NEW_PROGRAMMER_MAIN_LANGUAGE);

    programmersCache = cacheManager.getCache(PROGRAMMERS_CACHE_NAME);
    assertThat(programmersCache).isNotNull();
    assertThat(programmersCache.get(NEW_PROGRAMMER_MAIN_LANGUAGE)).isNull();
  }

  public static class Initializer implements
      ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
      TestPropertyValues values = TestPropertyValues.of(
          "spring.cache.type=redis",
          "spring.redis.host=" + redis.getContainerIpAddress(),
          "spring.redis.port=" + redis.getMappedPort(6379));
      values.applyTo(configurableApplicationContext);
    }
  }
}