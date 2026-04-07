package ru.learning.java.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserXml {
  private String name;
  private String email;
}
