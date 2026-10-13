package com.arindustries.mapper;

import com.arindustries.dto.response.*;
import com.arindustries.entity.*;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class EntityDtoMapper {

    public CustomerResponse toCustomerResponse(Customer c) {
        if (c == null) return null;
        CustomerResponse r = new CustomerResponse();
        r.setId(c.getId());
        r.setCustomerCode(c.getCustomerCode());
        r.setCustomerName(c.getCustomerName());
        r.setCompanyName(c.getCompanyName());
        r.setPhone(c.getPhone());
        r.setAlternatePhone(c.getAlternatePhone());
        r.setEmail(c.getEmail());
        r.setGstin(c.getGstin());
        r.setPan(c.getPan());
        r.setBillingAddress(c.getBillingAddress());
        r.setShippingAddress(c.getShippingAddress());
        r.setCity(c.getCity());
        r.setState(c.getState());
        r.setStateCode(c.getStateCode());
        r.setPincode(c.getPincode());
        r.setCreditLimit(c.getCreditLimit());
        r.setPaymentTerms(c.getPaymentTerms());
        r.setOpeningBalance(c.getOpeningBalance());
        r.setOutstandingBalance(c.getOutstandingBalance());
        r.setActive(c.isActive());
        r.setCreatedAt(c.getCreatedAt());
        r.setUpdatedAt(c.getUpdatedAt());
        return r;
    }

    public PriceRecordResponse toPriceRecordResponse(PriceRecord p) {
        if (p == null) return null;
        PriceRecordResponse r = new PriceRecordResponse();
        r.setId(p.getId());
        r.setPriceCode(p.getPriceCode());
        r.setCategory(p.getCategory());
        r.setThickness(p.getThickness());
        r.setProductType(p.getProductType());
        r.setSize(p.getSize());
        r.setPackaging(p.getPackaging());
        r.setPcsPerBox(p.getPcsPerBox());
        r.setWholesaleRate(p.getWholesaleRate());
        r.setRetailRate(p.getRetailRate());
        r.setWholesaleBoxRate(p.getWholesaleBoxRate());
        r.setRetailBoxRate(p.getRetailBoxRate());
        r.setEffectiveFrom(p.getEffectiveFrom());
        r.setEffectiveTo(p.getEffectiveTo());
        r.setActive(p.isActive());
        r.setNotes(p.getNotes());
        return r;
    }

    public InventoryResponse toInventoryResponse(Inventory i) {
        if (i == null) return null;
        InventoryResponse r = new InventoryResponse();
        r.setId(i.getId());
        r.setItemCode(i.getItemCode());
        r.setItemName(i.getItemName());
        r.setCategory(i.getCategory());
        r.setThickness(i.getThickness());
        r.setProductType(i.getProductType());
        r.setSize(i.getSize());
        r.setPackaging(i.getPackaging());
        r.setPcsPerBox(i.getPcsPerBox());
        r.setCurrentStock(i.getCurrentStock());
        r.setMinimumStock(i.getMinimumStock());
        r.setUnit(i.getUnit());
        r.setActive(i.isActive());
        r.setLowStock(i.isLowStock());
        r.setCurrentBoxes(i.getPcsPerBox() > 0 ? (double) i.getCurrentStock() / i.getPcsPerBox() : 0.0);
        return r;
    }

    public InvoiceItemResponse toInvoiceItemResponse(InvoiceItem item) {
        if (item == null) return null;
        InvoiceItemResponse r = new InvoiceItemResponse();
        r.setId(item.getId());
        r.setCategory(item.getCategory());
        r.setThickness(item.getThickness());
        r.setProductType(item.getProductType());
        r.setSize(item.getSize());
        r.setPackaging(item.getPackaging());
        r.setPcsPerBox(item.getPcsPerBox());
        r.setQuantity(item.getQuantity());
        r.setBillingUnit(item.getBillingUnit());
        r.setRatePerPc(item.getRatePerPc());
        r.setRatePerBox(item.getRatePerBox());
        r.setDiscountPercent(item.getDiscountPercent());
        r.setTaxableAmount(item.getTaxableAmount());
        r.setCgstPercent(item.getCgstPercent());
        r.setSgstPercent(item.getSgstPercent());
        r.setIgstPercent(item.getIgstPercent());
        r.setCgstAmount(item.getCgstAmount());
        r.setSgstAmount(item.getSgstAmount());
        r.setIgstAmount(item.getIgstAmount());
        r.setTotalAmount(item.getTotalAmount());
        return r;
    }

    public InvoiceResponse toInvoiceResponse(Invoice inv) {
        if (inv == null) return null;
        InvoiceResponse r = new InvoiceResponse();
        r.setId(inv.getId());
        r.setInvoiceNumber(inv.getInvoiceNumber());
        r.setInvoiceDate(inv.getInvoiceDate());
        r.setDueDate(inv.getDueDate());
        if (inv.getCustomer() != null) {
            r.setCustomerId(inv.getCustomer().getId());
            r.setCustomerName(inv.getCustomer().getCustomerName());
            r.setCustomerGstin(inv.getCustomer().getGstin());
        }
        r.setBillingAddress(inv.getBillingAddress());
        r.setShippingAddress(inv.getShippingAddress());
        r.setPlaceOfSupply(inv.getPlaceOfSupply());
        r.setInterstate(inv.isInterstate());
        r.setSubtotal(inv.getSubtotal());
        r.setCgst(inv.getCgst());
        r.setSgst(inv.getSgst());
        r.setIgst(inv.getIgst());
        r.setRoundOff(inv.getRoundOff());
        r.setGrandTotal(inv.getGrandTotal());
        r.setAmountPaid(inv.getAmountPaid());
        r.setBalanceDue(inv.getBalanceDue());
        r.setPaymentStatus(inv.getPaymentStatus() != null ? inv.getPaymentStatus().name() : null);
        r.setInvoiceStatus(inv.getInvoiceStatus() != null ? inv.getInvoiceStatus().name() : null);
        r.setPaymentMode(inv.getPaymentMode() != null ? inv.getPaymentMode().name() : null);
        r.setNotes(inv.getNotes());
        r.setTermsAndConditions(inv.getTermsAndConditions());
        r.setCreatedBy(inv.getCreatedBy());
        r.setCreatedAt(inv.getCreatedAt());
        r.setUpdatedAt(inv.getUpdatedAt());

        if (inv.getItems() != null) {
            r.setItems(inv.getItems().stream()
                .map(this::toInvoiceItemResponse)
                .collect(Collectors.toList()));
        }
        return r;
    }

    public PaymentResponse toPaymentResponse(Payment pay) {
        if (pay == null) return null;
        PaymentResponse r = new PaymentResponse();
        r.setId(pay.getId());
        if (pay.getCustomer() != null) {
            r.setCustomerId(pay.getCustomer().getId());
            r.setCustomerName(pay.getCustomer().getCustomerName());
        }
        if (pay.getInvoice() != null) {
            r.setInvoiceId(pay.getInvoice().getId());
            r.setInvoiceNumber(pay.getInvoice().getInvoiceNumber());
        }
        r.setPaymentDate(pay.getPaymentDate());
        r.setAmount(pay.getAmount());
        r.setPaymentMode(pay.getPaymentMode() != null ? pay.getPaymentMode().name() : null);
        r.setReferenceNumber(pay.getReferenceNumber());
        r.setNotes(pay.getNotes());
        r.setReversed(pay.isReversed());
        r.setRecordedBy(pay.getRecordedBy());
        r.setCreatedAt(pay.getCreatedAt());
        return r;
    }

    public EmployeeResponse toEmployeeResponse(Employee emp) {
        if (emp == null) return null;
        EmployeeResponse r = new EmployeeResponse();
        r.setId(emp.getId());
        r.setEmployeeCode(emp.getEmployeeCode());
        r.setFirstName(emp.getFirstName());
        r.setLastName(emp.getLastName());
        String fullName = (emp.getFirstName() != null ? emp.getFirstName() : "") + 
            (emp.getLastName() != null && !emp.getLastName().isBlank() ? " " + emp.getLastName() : "");
        r.setFullName(fullName.trim());
        r.setEmail(emp.getEmail());
        r.setPhone(emp.getPhone());
        r.setDateOfBirth(emp.getDateOfBirth());
        r.setAddress(emp.getAddress());
        r.setJoiningDate(emp.getJoiningDate());
        r.setDepartment(emp.getDepartment());
        r.setDesignation(emp.getDesignation());
        r.setEmploymentType(emp.getEmploymentType());
        r.setWorkLocation(emp.getWorkLocation());
        r.setShift(emp.getShift());
        r.setBasicSalary(emp.getBasicSalary());
        r.setActive(emp.isActive());
        r.setCreatedAt(emp.getCreatedAt());
        r.setUpdatedAt(emp.getUpdatedAt());
        return r;
    }

    public AttendanceResponse toAttendanceResponse(Attendance att) {
        if (att == null) return null;
        AttendanceResponse r = new AttendanceResponse();
        r.setId(att.getId());
        if (att.getEmployee() != null) {
            r.setEmployeeId(att.getEmployee().getId());
            r.setEmployeeCode(att.getEmployee().getEmployeeCode());
            String fullName = (att.getEmployee().getFirstName() != null ? att.getEmployee().getFirstName() : "") + 
                (att.getEmployee().getLastName() != null && !att.getEmployee().getLastName().isBlank() ? " " + att.getEmployee().getLastName() : "");
            r.setEmployeeName(fullName.trim());
            r.setDepartment(att.getEmployee().getDepartment());
        }
        r.setAttendanceDate(att.getAttendanceDate());
        r.setCheckIn(att.getCheckIn());
        r.setCheckOut(att.getCheckOut());
        r.setStatus(att.getStatus() != null ? att.getStatus().name() : null);
        r.setWorkingHours(att.getWorkingHours());
        r.setNotes(att.getNotes());
        r.setMarkedBy(att.getMarkedBy());
        r.setCreatedAt(att.getCreatedAt());
        return r;
    }

    public StockMovementResponse toStockMovementResponse(StockMovement sm) {
        if (sm == null) return null;
        StockMovementResponse r = new StockMovementResponse();
        r.setId(sm.getId());
        if (sm.getInventory() != null) {
            r.setInventoryId(sm.getInventory().getId());
            r.setItemCode(sm.getInventory().getItemCode());
            r.setItemName(sm.getInventory().getItemName());
        }
        r.setMovementType(sm.getMovementType() != null ? sm.getMovementType().name() : null);
        r.setQuantity(sm.getQuantity());
        r.setStockBefore(sm.getStockBefore());
        r.setStockAfter(sm.getStockAfter());
        r.setReferenceType(sm.getReferenceType());
        r.setReferenceId(sm.getReferenceId());
        r.setNotes(sm.getNotes());
        r.setPerformedBy(sm.getPerformedBy());
        r.setCreatedAt(sm.getCreatedAt());
        return r;
    }

    public AuditLogResponse toAuditLogResponse(AuditLog al) {
        if (al == null) return null;
        AuditLogResponse r = new AuditLogResponse();
        r.setId(al.getId());
        r.setActor(al.getActor());
        r.setAction(al.getAction());
        r.setModule(al.getModule());
        r.setDescription(al.getDescription());
        r.setEntityType(al.getEntityType());
        r.setEntityId(al.getEntityId());
        r.setTimestamp(al.getTimestamp());
        return r;
    }
}
