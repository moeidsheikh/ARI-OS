package com.arindustries.service;

import com.arindustries.dto.response.DashboardResponse;
import com.arindustries.dto.response.InvoiceResponse;
import com.arindustries.dto.response.PaymentResponse;
import com.arindustries.entity.Customer;
import com.arindustries.entity.Invoice;
import com.arindustries.entity.Payment;
import com.arindustries.mapper.EntityDtoMapper;
import com.arindustries.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final InventoryRepository inventoryRepository;
    private final AttendanceRepository attendanceRepository;
    private final PaymentRepository paymentRepository;
    private final AttendanceService attendanceService;
    private final AuditLogService auditLogService;
    private final EntityDtoMapper mapper;

    public DashboardService(InvoiceRepository invoiceRepository,
                            CustomerRepository customerRepository,
                            InventoryRepository inventoryRepository,
                            AttendanceRepository attendanceRepository,
                            PaymentRepository paymentRepository,
                            AttendanceService attendanceService,
                            AuditLogService auditLogService,
                            EntityDtoMapper mapper) {
        this.invoiceRepository = invoiceRepository;
        this.customerRepository = customerRepository;
        this.inventoryRepository = inventoryRepository;
        this.attendanceRepository = attendanceRepository;
        this.paymentRepository = paymentRepository;
        this.attendanceService = attendanceService;
        this.auditLogService = auditLogService;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getMetrics() {
        DashboardResponse res = new DashboardResponse();
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());

        // Sales aggregations
        BigDecimal todaySales = invoiceRepository.sumTotalBetween(today, today);
        BigDecimal monthlySales = invoiceRepository.sumTotalBetween(startOfMonth, endOfMonth);
        res.setTodaySales(todaySales != null ? todaySales : BigDecimal.ZERO);
        res.setMonthlySales(monthlySales != null ? monthlySales : BigDecimal.ZERO);

        // Outstanding receivables
        List<Customer> allCustomers = customerRepository.findAll();
        BigDecimal totalOutstanding = allCustomers.stream()
            .map(c -> c.getOutstandingBalance() != null ? c.getOutstandingBalance() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        res.setOutstandingReceivables(totalOutstanding);

        // Counts
        res.setPendingInvoicesCount(invoiceRepository.countPendingInvoices());
        res.setTotalCustomers(customerRepository.count());
        res.setTotalProducts(inventoryRepository.count());
        res.setLowStockCount(inventoryRepository.findLowStockItems().size());
        res.setEmployeesPresentToday(attendanceRepository.countPresentOnDate(today));

        // Recent Invoices (top 5)
        List<Invoice> recentInvoices = invoiceRepository.findAll(
            PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "invoiceDate", "id"))).getContent();
        List<InvoiceResponse> invoiceResponses = recentInvoices.stream()
            .map(mapper::toInvoiceResponse)
            .collect(Collectors.toList());
        res.setRecentInvoices(invoiceResponses);

        // Recent Payments (top 5)
        List<Payment> recentPayments = paymentRepository.findAll(
            PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "paymentDate", "id"))).getContent();
        List<PaymentResponse> paymentResponses = recentPayments.stream()
            .map(mapper::toPaymentResponse)
            .collect(Collectors.toList());
        res.setRecentPayments(paymentResponses);

        // Attendance summary
        res.setAttendanceSummary(attendanceService.getTodaySummary());

        // Recent Audit logs (top 5)
        res.setRecentAuditLogs(auditLogService.getRecent(5));

        return res;
    }
}
