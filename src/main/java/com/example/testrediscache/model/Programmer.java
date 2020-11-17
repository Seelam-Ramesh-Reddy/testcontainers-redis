package com.example.testrediscache.model;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "programmers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Programmer implements Serializable {

  @Id
  private String nick;
  private String mainLanguage;
}
