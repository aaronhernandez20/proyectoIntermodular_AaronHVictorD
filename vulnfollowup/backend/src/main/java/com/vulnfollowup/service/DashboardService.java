package com.vulnfollowup.service;

import com.vulnfollowup.cvss.Severity;
import com.vulnfollowup.entity.VulnStatus;
import com.vulnfollowup.repository.VulnerabilityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Métricas del dashboard (enunciado: "número de vulnerabilidades por severidad y estado").
// Los conteos los hace PostgreSQL con GROUP BY: no se cargan todas las vulnerabilidades en
// memoria para contarlas en Java (más rápido y más sostenible).
@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final VulnerabilityRepository repository;

    public DashboardService(VulnerabilityRepository repository) {
        this.repository = repository;
    }

    public record DashboardSummary(long total,
                                   Map<Severity, Long> bySeverity,
                                   Map<VulnStatus, Long> byStatus,
                                   long pending,      // OPEN + PENDING_REMEDIATION: riesgo que sigue abierto
                                   long overdue) {    // pendientes con la fecha objetivo ya pasada
    }

    public DashboardSummary summary() {
        // Se rellenan todas las claves con 0 y en orden de gravedad: así la web y el móvil
        // siempre reciben las mismas claves, aunque no haya ninguna de un tipo.
        Map<Severity, Long> bySeverity = new LinkedHashMap<>();
        for (Severity s : List.of(Severity.CRITICAL, Severity.HIGH, Severity.MEDIUM, Severity.LOW, Severity.NONE)) {
            bySeverity.put(s, 0L);
        }
        repository.countBySeverity().forEach(row -> bySeverity.put((Severity) row[0], (Long) row[1]));

        Map<VulnStatus, Long> byStatus = new LinkedHashMap<>();
        for (VulnStatus s : VulnStatus.values()) {
            byStatus.put(s, 0L);
        }
        repository.countByStatus().forEach(row -> byStatus.put((VulnStatus) row[0], (Long) row[1]));

        long total = byStatus.values().stream().mapToLong(Long::longValue).sum();
        long pending = byStatus.get(VulnStatus.OPEN) + byStatus.get(VulnStatus.PENDING_REMEDIATION);
        return new DashboardSummary(total, bySeverity, byStatus, pending, repository.countOverdue(LocalDate.now()));
    }
}
