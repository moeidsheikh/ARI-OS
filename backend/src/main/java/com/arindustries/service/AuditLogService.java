package com.arindustries.service;

import com.arindustries.dto.response.AuditLogResponse;
import com.arindustries.entity.AuditLog;
import com.arindustries.mapper.EntityDtoMapper;
import com.arindustries.pagination.PageResponse;
import com.arindustries.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final EntityDtoMapper mapper;

    public AuditLogService(AuditLogRepository auditLogRepository, EntityDtoMapper mapper) {
        this.auditLogRepository = auditLogRepository;
        this.mapper = mapper;
    }

    @Transactional
    public void log(String actor, String action, String module, String description, String entityType, String entityId) {
        try {
            AuditLog log = new AuditLog();
            log.setActor(actor != null && !actor.isBlank() ? actor : "system");
            log.setAction(action);
            log.setModule(module);
            log.setDescription(description);
            log.setEntityType(entityType);
            log.setEntityId(entityId);
            auditLogRepository.save(log);
        } catch (Exception e) {
            // Logging failure should not break the business transaction
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> getAll(int page, int size) {
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 10;

        Page<AuditLog> pageResult = auditLogRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp")));
        List<AuditLogResponse> dtos = pageResult.getContent().stream()
            .map(mapper::toAuditLogResponse)
            .collect(Collectors.toList());

        return new PageResponse<>(dtos, pageResult.getTotalElements(), pageResult.getNumber(), pageResult.getSize());
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getRecent(int count) {
        Page<AuditLog> pageResult = auditLogRepository.findAll(PageRequest.of(0, count, Sort.by(Sort.Direction.DESC, "timestamp")));
        return pageResult.getContent().stream()
            .map(mapper::toAuditLogResponse)
            .collect(Collectors.toList());
    }
}
