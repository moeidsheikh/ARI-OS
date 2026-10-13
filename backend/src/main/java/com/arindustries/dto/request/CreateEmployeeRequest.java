package com.arindustries.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class CreateEmployeeRequest {
    @NotBlank private String firstName;
    @NotBlank private String lastName;
    private String email;
    @NotBlank private String phone;
    private LocalDate dateOfBirth;
    private String address;
    @NotNull private LocalDate joiningDate;
    @NotBlank private String department;
    @NotBlank private String designation;
    private String employmentType = "FULL_TIME";
    private String workLocation;
    private String shift;
    @PositiveOrZero private BigDecimal basicSalary = BigDecimal.ZERO;

    public String getFirstName() { return firstName; }
    public void setFirstName(String v) { this.firstName = v; }
    public String getLastName() { return lastName; }
    public void setLastName(String v) { this.lastName = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { this.email = v; }
    public String getPhone() { return phone; }
    public void setPhone(String v) { this.phone = v; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate v) { this.dateOfBirth = v; }
    public String getAddress() { return address; }
    public void setAddress(String v) { this.address = v; }
    public LocalDate getJoiningDate() { return joiningDate; }
    public void setJoiningDate(LocalDate v) { this.joiningDate = v; }
    public String getDepartment() { return department; }
    public void setDepartment(String v) { this.department = v; }
    public String getDesignation() { return designation; }
    public void setDesignation(String v) { this.designation = v; }
    public String getEmploymentType() { return employmentType; }
    public void setEmploymentType(String v) { this.employmentType = v; }
    public String getWorkLocation() { return workLocation; }
    public void setWorkLocation(String v) { this.workLocation = v; }
    public String getShift() { return shift; }
    public void setShift(String v) { this.shift = v; }
    public BigDecimal getBasicSalary() { return basicSalary; }
    public void setBasicSalary(BigDecimal v) { this.basicSalary = v; }
}
