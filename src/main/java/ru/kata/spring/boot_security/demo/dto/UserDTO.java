package ru.kata.spring.boot_security.demo.dto;

import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;

public class UserDTO {
    private Long id;
    @NotEmpty(message = "Name should be not empty") @Size(min = 2, max = 30, message = "Name must be between 2 and 30 characters") private String firstName;
    @NotEmpty(message = "Last name should be not empty") @Size(min = 2, max = 30, message = "Last name must be between 2 and 30 characters") private String lastName;
    @Min(value = 18, message = "Age > 18") @Max(value = 150, message = "Age < 150") private int age;
    @NotEmpty(message = "Email should be not empty") @Size(min = 2, max = 100) private String email;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    @NotEmpty(message = "Не выбрана ни одна роль") private Set<RoleDTO> roles;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Set<RoleDTO> getRoles() { return roles; }
    public void setRoles(Set<RoleDTO> roles) { this.roles = roles; }
}
