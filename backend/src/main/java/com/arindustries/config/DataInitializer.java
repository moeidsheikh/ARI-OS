package com.arindustries.config;

import com.arindustries.entity.*;
import com.arindustries.enums.AttendanceStatus;
import com.arindustries.enums.UserRole;
import com.arindustries.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PriceRecordRepository priceRecordRepository;
    private final InventoryRepository inventoryRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           CustomerRepository customerRepository,
                           PriceRecordRepository priceRecordRepository,
                           InventoryRepository inventoryRepository,
                           EmployeeRepository employeeRepository,
                           AttendanceRepository attendanceRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.priceRecordRepository = priceRecordRepository;
        this.inventoryRepository = inventoryRepository;
        this.employeeRepository = employeeRepository;
        this.attendanceRepository = attendanceRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedCustomers();
        seedPriceMasterAndInventory();
        seedEmployeesAndAttendance();
    }

    private void seedUsers() {
        User admin = userRepository.findByUsername("admin").orElseGet(() -> {
            User u = new User();
            u.setUsername("admin");
            u.setEmail("admin@arindustries.in");
            u.setFullName("A. Rahman (Admin)");
            u.setRole(UserRole.SUPER_ADMIN);
            u.setActive(true);
            return u;
        });
        admin.setPasswordHash(passwordEncoder.encode("admin123"));
        userRepository.save(admin);
    }

    private void seedCustomers() {
        if (customerRepository.count() == 0) {
            createCustomer("CUST-01", "Vidarbha Electrical Traders", "Rajesh Agrawal", "9823011223", "sales@vidarbhaelec.com",
                "27AABCV1234F1Z1", "Ganjakhet Chowk, Itwari", "Nagpur", "Maharashtra", "27", "440002", new BigDecimal("32000"));

            createCustomer("CUST-02", "Mahalaxmi Electrical Hardware", "Ganesh Sharma", "9822455667", "mahalaxmi.elec@gmail.com",
                "27BAAPS5544K1ZY", "Central Avenue, Gandhibagh", "Nagpur", "Maharashtra", "27", "440018", new BigDecimal("14200"));

            createCustomer("CUST-03", "Raipur Power Switchgears", "Amit Patel", "9425211998", "raipur.power@outlook.com",
                "22AAACR9988M1Z0", "Pandri Cloth Market", "Raipur", "Chhattisgarh", "22", "492004", new BigDecimal("68500"));

            createCustomer("CUST-04", "Shree Balaji Enterprises", "Prakash Jain", "9890123456", "balaji.jain@gmail.com",
                "27AACFB7788R1ZM", "Station Road", "Amravati", "Maharashtra", "27", "444601", new BigDecimal("8500"));
        }
    }

    private void createCustomer(String code, String name, String company, String phone, String email,
                                String gstin, String addr, String city, String state, String stateCode, String pin, BigDecimal bal) {
        Customer c = new Customer();
        c.setCustomerCode(code);
        c.setCustomerName(name);
        c.setCompanyName(company);
        c.setPhone(phone);
        c.setEmail(email);
        c.setGstin(gstin);
        c.setBillingAddress(addr + ", " + city + ", " + state + " - " + pin);
        c.setShippingAddress(c.getBillingAddress());
        c.setCity(city);
        c.setState(state);
        c.setStateCode(stateCode);
        c.setPincode(pin);
        c.setOpeningBalance(bal);
        c.setOutstandingBalance(bal);
        c.setCreditLimit(new BigDecimal("100000"));
        c.setActive(true);
        customerRepository.save(c);
    }

    private void seedPriceMasterAndInventory() {
        if (priceRecordRepository.count() == 0) {
            // 0.8 mm Normal Clip
            addVariant("0.8 mm", "Normal Clip", "2M", "20 Pcs", 20, 21.50, 25.00, 520, 100);
            addVariant("0.8 mm", "Box Clip", "2M", "20 Pcs", 20, 23.50, 27.50, 340, 80);
            addVariant("0.8 mm", "Normal Clip", "3M", "20 Pcs", 20, 26.00, 30.50, 480, 100);
            addVariant("0.8 mm", "Box Clip", "3M", "20 Pcs", 20, 28.50, 33.00, 290, 80);
            addVariant("0.8 mm", "Normal Clip", "4M", "20 Pcs", 20, 32.00, 37.50, 610, 120);
            addVariant("0.8 mm", "Box Clip", "4M", "20 Pcs", 20, 35.00, 41.00, 400, 100);
            addVariant("0.8 mm", "Normal Clip", "6M", "10 Pcs", 10, 45.00, 52.00, 580, 100);
            addVariant("0.8 mm", "Box Clip", "6M", "10 Pcs", 10, 49.00, 57.00, 320, 80);
            addVariant("0.8 mm", "Normal Clip", "8M(H)", "10 Pcs", 10, 56.00, 65.00, 420, 80);
            addVariant("0.8 mm", "Box Clip", "8M(H)", "10 Pcs", 10, 61.00, 71.00, 250, 60);
            addVariant("0.8 mm", "Normal Clip", "8M(S)", "10 Pcs", 10, 58.00, 67.00, 380, 80);
            addVariant("0.8 mm", "Box Clip", "8M(S)", "10 Pcs", 10, 63.00, 73.00, 210, 50);
            addVariant("0.8 mm", "Normal Clip", "12M", "10 Pcs", 10, 78.00, 90.00, 290, 60);
            addVariant("0.8 mm", "Box Clip", "12M", "10 Pcs", 10, 84.00, 97.00, 180, 40);
            addVariant("0.8 mm", "Normal Clip", "16M", "6 Pcs", 6, 95.00, 110.00, 150, 40);
            addVariant("0.8 mm", "Box Clip", "16M", "6 Pcs", 6, 102.00, 118.00, 110, 30);
            addVariant("0.8 mm", "Normal Clip", "18M", "6 Pcs", 6, 108.00, 125.00, 130, 30);
            addVariant("0.8 mm", "Box Clip", "18M", "6 Pcs", 6, 116.00, 135.00, 95, 30);

            // 0.7 mm
            addVariant("0.7 mm", "Normal Clip", "2M", "20 Pcs", 20, 18.50, 22.00, 300, 80);
            addVariant("0.7 mm", "Box Clip", "2M", "20 Pcs", 20, 20.50, 24.00, 220, 60);
            addVariant("0.7 mm", "Normal Clip", "3M", "20 Pcs", 20, 22.50, 26.50, 310, 80);
            addVariant("0.7 mm", "Normal Clip", "4M", "20 Pcs", 20, 28.00, 33.00, 350, 90);
            addVariant("0.7 mm", "Normal Clip", "6M", "10 Pcs", 10, 39.00, 46.00, 280, 70);

            // 1.0 mm
            addVariant("1 mm", "Normal Clip", "2M", "20 Pcs", 20, 26.00, 30.50, 240, 60);
            addVariant("1 mm", "Box Clip", "2M", "20 Pcs", 20, 28.50, 33.00, 190, 50);
            addVariant("1 mm", "Normal Clip", "4M", "20 Pcs", 20, 38.00, 44.00, 260, 70);
            addVariant("1 mm", "Normal Clip", "6M", "10 Pcs", 10, 54.00, 63.00, 220, 60);
        }
    }

    private void addVariant(String thickness, String type, String size, String pack, int pcs, double wsRate, double rtRate, int stock, int minStock) {
        String cleanType = type.toLowerCase().contains("box") ? "B" : "S";
        String cleanThick = thickness.replace(" ", "").replace(".", "");
        String code = "ARI-" + cleanThick + "-" + cleanType + size;

        PriceRecord pr = new PriceRecord();
        pr.setPriceCode("PL-" + cleanThick + "-" + cleanType + size);
        pr.setCategory("Modular Box");
        pr.setThickness(thickness);
        pr.setProductType(type);
        pr.setSize(size);
        pr.setPackaging(pack);
        pr.setPcsPerBox(pcs);
        pr.setWholesaleRate(BigDecimal.valueOf(wsRate));
        pr.setRetailRate(BigDecimal.valueOf(rtRate));
        pr.setWholesaleBoxRate(BigDecimal.valueOf(wsRate * pcs));
        pr.setRetailBoxRate(BigDecimal.valueOf(rtRate * pcs));
        pr.setEffectiveFrom(LocalDate.of(2026, 1, 1));
        pr.setActive(true);
        priceRecordRepository.save(pr);

        Inventory inv = new Inventory();
        inv.setItemCode(code);
        inv.setItemName(thickness + " MS Modular Box (" + type + ") " + size);
        inv.setCategory("Modular Box");
        inv.setThickness(thickness);
        inv.setProductType(type);
        inv.setSize(size);
        inv.setPackaging(pack);
        inv.setPcsPerBox(pcs);
        inv.setCurrentStock(stock);
        inv.setMinimumStock(minStock);
        inv.setUnit("Pcs");
        inv.setActive(true);
        inventoryRepository.save(inv);
    }

    private void seedEmployeesAndAttendance() {
        if (employeeRepository.count() == 0) {
            Employee e1 = createEmployee("EMP-101", "Rameshwar", "Deshmukh", "Plant Supervisor", "Production", "9823400111", 28000);
            Employee e2 = createEmployee("EMP-102", "Sunil", "Wankhede", "Press Operator", "Press Shop", "9823400222", 19500);
            Employee e3 = createEmployee("EMP-103", "Pooja", "Verma", "GST Accountant", "Finance", "9823400333", 26000);
            Employee e4 = createEmployee("EMP-104", "Mohammad", "Irfan", "Tool Room Machinist", "Tooling", "9823400444", 23000);
            Employee e5 = createEmployee("EMP-105", "Dinesh", "Tiwari", "Dispatch Assistant", "Dispatch", "9823400555", 16500);

            // Today's attendance
            LocalDate today = LocalDate.now();
            markAttendance(e1, today, LocalDateTime.of(today, java.time.LocalTime.of(8, 52)), AttendanceStatus.PRESENT);
            markAttendance(e2, today, LocalDateTime.of(today, java.time.LocalTime.of(9, 5)), AttendanceStatus.PRESENT);
            markAttendance(e3, today, LocalDateTime.of(today, java.time.LocalTime.of(9, 25)), AttendanceStatus.LATE);
            markAttendance(e4, today, LocalDateTime.of(today, java.time.LocalTime.of(8, 48)), AttendanceStatus.PRESENT);
            markAttendance(e5, today, null, AttendanceStatus.ON_LEAVE);
        }
    }

    private Employee createEmployee(String code, String first, String last, String desig, String dept, String phone, double sal) {
        Employee e = new Employee();
        e.setEmployeeCode(code);
        e.setFirstName(first);
        e.setLastName(last);
        e.setEmail(first.toLowerCase() + "@arindustries.in");
        e.setPhone(phone);
        e.setJoiningDate(LocalDate.of(2025, 3, 1));
        e.setDepartment(dept);
        e.setDesignation(desig);
        e.setBasicSalary(BigDecimal.valueOf(sal));
        e.setActive(true);
        return employeeRepository.save(e);
    }

    private void markAttendance(Employee e, LocalDate date, LocalDateTime checkIn, AttendanceStatus status) {
        Attendance a = new Attendance();
        a.setEmployee(e);
        a.setAttendanceDate(date);
        a.setCheckIn(checkIn);
        a.setStatus(status);
        a.setMarkedBy("system");
        attendanceRepository.save(a);
    }
}
