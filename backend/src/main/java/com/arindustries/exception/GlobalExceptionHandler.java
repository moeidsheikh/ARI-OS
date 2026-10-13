package com.arindustries.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private Map<String, Object> buildError(int status, String error, String message, String path) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status);
        body.put("error", error);
        body.put("message", message);
        body.put("path", path);
        return body;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildError(404, "Not Found", ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<?> handleDuplicate(DuplicateResourceException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildError(409, "Conflict", ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<?> handleBusiness(BusinessRuleException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildError(400, "Bad Request", ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest req) {
        String rawMsg = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        String userFriendlyMsg = "The operation could not be completed because it conflicts with existing data.";

        if (rawMsg != null) {
            String lower = rawMsg.toLowerCase();
            if (lower.contains("duplicate entry")) {
                Matcher m = Pattern.compile("Duplicate entry '([^']+)' for key '([^']+)'").matcher(rawMsg);
                String dupVal = "";
                String keyName = "";
                if (m.find()) {
                    dupVal = m.group(1);
                    keyName = m.group(2).toLowerCase();
                }

                if (keyName.contains("price") || lower.contains("price_records")) {
                    userFriendlyMsg = "A price master record with these exact specifications (Thickness, Clip Type, and Size) already exists. Please edit the existing record instead of adding a duplicate.";
                } else if (keyName.contains("gstin") || lower.contains("gstin")) {
                    userFriendlyMsg = "A customer with GSTIN '" + dupVal + "' already exists in the system.";
                } else if (keyName.contains("customer_code") || lower.contains("customer_code")) {
                    userFriendlyMsg = "A customer with code '" + dupVal + "' already exists.";
                } else if (keyName.contains("item_code") || lower.contains("item_code")) {
                    userFriendlyMsg = "An inventory item with code '" + dupVal + "' already exists.";
                } else if (keyName.contains("invoice_number") || lower.contains("invoice_number")) {
                    userFriendlyMsg = "An invoice with number '" + dupVal + "' already exists.";
                } else if (keyName.contains("employee_code") || lower.contains("employee_code")) {
                    userFriendlyMsg = "An employee with code '" + dupVal + "' already exists.";
                } else if (keyName.contains("uk_emp_att_date") || lower.contains("attendance")) {
                    userFriendlyMsg = "Attendance has already been logged for this employee on this date.";
                } else if (keyName.contains("phone") || lower.contains("phone")) {
                    userFriendlyMsg = "A record with phone number '" + dupVal + "' already exists.";
                } else if (keyName.contains("email") || lower.contains("email")) {
                    userFriendlyMsg = "A record with email address '" + dupVal + "' already exists.";
                } else if (!dupVal.isEmpty()) {
                    userFriendlyMsg = "Duplicate entry: value '" + dupVal + "' is already registered in the system.";
                } else {
                    userFriendlyMsg = "This record already exists in the system. Duplicate entries are not permitted.";
                }
            } else if (lower.contains("foreign key constraint fails") || lower.contains("cannot delete or update a parent row")) {
                userFriendlyMsg = "Cannot delete or modify this record because it is currently linked to other transactions, invoices, or records in the system.";
            } else if (lower.contains("cannot be null")) {
                userFriendlyMsg = "A required field was missing or left blank. Please check all required form inputs.";
            } else if (lower.contains("data too long")) {
                userFriendlyMsg = "One or more input values exceed the maximum allowed character length.";
            }
        }

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildError(409, "Conflict", userFriendlyMsg, req.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(e -> {
            String field = e instanceof FieldError ? ((FieldError) e).getField() : e.getObjectName();
            errors.put(field, e.getDefaultMessage());
        });
        String summary = errors.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining("; "));

        Map<String, Object> body = buildError(400, "Validation Failed", summary.isEmpty() ? "Input validation failed" : summary, req.getRequestURI());
        body.put("fieldErrors", errors);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        String msg = ex.getConstraintViolations().stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildError(400, "Validation Error", msg.isEmpty() ? "Validation constraint violated" : msg, req.getRequestURI()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildError(400, "Malformed Request", "Request body is invalid or contains improperly formatted values (e.g. invalid date, number, or JSON structure).", req.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        String name = ex.getName();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildError(400, "Invalid Parameter", "Parameter '" + name + "' has an invalid value format.", req.getRequestURI()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<?> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(buildError(405, "Method Not Allowed", "HTTP " + ex.getMethod() + " is not supported on this endpoint.", req.getRequestURI()));
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<?> handleIllegalArgument(RuntimeException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildError(400, "Bad Request", ex.getMessage() != null ? ex.getMessage() : "Invalid argument provided.", req.getRequestURI()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> handleAuth(AuthenticationException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(buildError(401, "Unauthorized", ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccess(AccessDeniedException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(buildError(403, "Forbidden", "You do not have permission to perform this action", req.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneral(Exception ex, HttpServletRequest req) {
        ex.printStackTrace();
        String msg = ex.getMessage();
        if (msg == null || msg.contains("could not execute statement") || msg.contains("SQL [") || msg.contains("constraint [") || msg.contains("HibernateException")) {
            msg = "An internal server error occurred while processing your request. Please verify inputs or contact administrator.";
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildError(500, "Internal Server Error", msg, req.getRequestURI()));
    }
}
