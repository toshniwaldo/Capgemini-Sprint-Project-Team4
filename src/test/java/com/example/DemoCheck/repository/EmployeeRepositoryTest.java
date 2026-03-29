package com.example.DemoCheck.repository;

import com.example.DemoCheck.entity.Employee;
import com.example.DemoCheck.entity.Office;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
// import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class EmployeeRepositoryTest {


    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OfficeRepository officeRepository; // ✅ REQUIRED

    private Office createOffice() {
        Office office = new Office();
        office.setOfficeCode("1");
        office.setCity("Nagpur");
        office.setPhone("1234567890");
        office.setAddressLine1("IT Park");
        office.setCountry("India");
        office.setPostalCode("440022");
        office.setTerritory("APAC");
        return officeRepository.save(office);
    }

    // ✅ Helper: Create Employee
    private Employee createEmployee(int id, Office office,Employee manager) {
        Employee emp = new Employee();
        emp.setEmployeeNumber(id);
        emp.setFirstName("Test" + id);
        emp.setLastName("User");
        emp.setEmail("test" + id + "@gmail.com");
        emp.setExtension("x1234");
        emp.setJobTitle("Developer");
        emp.setOffice(office); // ✅ CORRECT

        emp.setManager(manager); // ✅ IMPORTANT

        return emp;
    }

    // ✅ Test: Get All Employees
    @Test
    @DisplayName("Test: Get All Employees")
    public void getAllEmployees() {

        Office office = createOffice();

        int employeesOld = employeeRepository.findAll().size();

        employeeRepository.save(createEmployee(9999, office,null));

        int employeesNew = employeeRepository.findAll().size();

        assertEquals(employeesOld + 1, employeesNew);
    }

    // ✅ Pagination Test - First Page
    @Test
    @DisplayName("Test: Pagination - first page")
    public void testPagination() {

        Office office = createOffice();

        for (int i = 0; i < 50; i++) {
            employeeRepository.save(createEmployee(1000 + i, office,null));
        }

        Pageable pageable = PageRequest.of(0, 5);

        Page<Employee> page = employeeRepository.findAll(pageable);

        assertEquals(5, page.getContent().size());
        assertEquals(0, page.getNumber());
        assertTrue(page.getTotalPages() >= 10);
    }

    // ✅ Pagination Test - Second Page
    @Test
    @DisplayName("Test: Pagination - second page")
    public void testSecondPage() {

        Office office = createOffice();

        for (int i = 0; i < 20; i++) {
            employeeRepository.save(createEmployee(2000 + i, office,null));
        }

        Pageable pageable = PageRequest.of(1, 5);

        Page<Employee> page = employeeRepository.findAll(pageable);

        assertEquals(5, page.getContent().size());
        assertEquals(1, page.getNumber());
    }

    @Test
    @DisplayName("Test: Save Employee With Manager")
    public void testSaveEmployeeWithManager() {

        // 1. Arrange
        Office office = createOffice();

        // Create manager first
        Employee manager = createEmployee(9002, office, null);
        employeeRepository.save(manager);

        // Create employee under manager
        Employee emp = createEmployee(9003, office, manager);

        // 2. Act
        Employee savedEmployee = employeeRepository.save(emp);

        // 3. Assert
        assertNotNull(savedEmployee);
        assertEquals(9003, savedEmployee.getEmployeeNumber());

        assertNotNull(savedEmployee.getManager());
        assertEquals(9002, savedEmployee.getManager().getEmployeeNumber());
    }

    @Test
    @DisplayName("Test: Save Employee FAIL - Missing First Name")
    public void testSaveEmployeeFail_MissingFirstName() {

        // 1. Arrange
        Office office = createOffice();

        Employee emp = new Employee();
        emp.setEmployeeNumber(9101);
        emp.setLastName("User");
        emp.setEmail("fail@gmail.com");
        emp.setExtension("x1234");
        emp.setJobTitle("Developer");
        emp.setOffice(office);

        // ❌ firstName NOT set

        // 2. Act + Assert
        assertThrows(Exception.class, () -> {
            employeeRepository.save(emp);
            employeeRepository.flush(); // 🔥 IMPORTANT
        });
    }

    @Test
    @DisplayName("Test: Save Employee FAIL - Missing Office")
    public void testSaveEmployeeFail_MissingOffice() {

        // 1. Arrange
        Employee emp = new Employee();
        emp.setEmployeeNumber(9401);
        emp.setFirstName("Test");
        emp.setLastName("User");
        emp.setEmail("fail@test.com");

        // ❌ office not set

        // 2. Act + Assert
        assertThrows(Exception.class, () -> {
            employeeRepository.save(emp);
            employeeRepository.flush();
        });
    }

    @Test
    @DisplayName("Test: Update Employee Office")
    public void testUpdateEmployeeOffice() {

        // Arrange
        Office office1 = createOffice();

        Office office2 = new Office();
        office2.setOfficeCode("2");
        office2.setCity("Pune");
        office2.setPhone("8888888888");
        office2.setAddressLine1("Tech Park");
        office2.setCountry("India");
        office2.setPostalCode("411001");
        office2.setTerritory("APAC");
        officeRepository.save(office2);

        Employee emp = createEmployee(12001, office1, null);
        employeeRepository.save(emp);

        // Act
        Employee existing = employeeRepository.findById(12001).get();
        existing.setOffice(office2);
        employeeRepository.save(existing);

        // Assert
        Employee updated = employeeRepository.findById(12001).get();
        assertEquals("2", updated.getOffice().getOfficeCode());
    }

    @Test
    @DisplayName("Test: Update Employee Manager")
    public void testUpdateEmployeeManager() {

        // Arrange
        Office office = createOffice();

        Employee manager = createEmployee(12002, office, null);
        employeeRepository.save(manager);

        Employee emp = createEmployee(12003, office, null);
        employeeRepository.save(emp);

        // Act
        Employee existing = employeeRepository.findById(12003).get();
        existing.setManager(manager);
        employeeRepository.save(existing);

        // Assert
        Employee updated = employeeRepository.findById(12003).get();
        assertNotNull(updated.getManager());
        assertEquals(12002, updated.getManager().getEmployeeNumber());
    }

    @Test
    @DisplayName("Test: Update Employee Job Title")
    public void testUpdateEmployeeJobTitle() {

        // Arrange
        Office office = createOffice();

        Employee emp = createEmployee(12004, office, null);
        emp.setJobTitle("Developer");
        employeeRepository.save(emp);

        // Act
        Employee existing = employeeRepository.findById(12004).get();
        existing.setJobTitle("Senior Developer");
        employeeRepository.save(existing);

        // Assert
        Employee updated = employeeRepository.findById(12004).get();
        assertEquals("Senior Developer", updated.getJobTitle());
    }
}
