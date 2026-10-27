package com.delivery.identity.service;

import com.delivery.identity.dto.AuditCreateRequest;
import com.delivery.identity.dto.AuditEntryDto;
import com.delivery.identity.dto.PlatformSettingsDto;
import com.delivery.identity.model.AdminAuditEntry;
import com.delivery.identity.model.PlatformSettings;
import com.delivery.identity.repository.AdminAuditRepository;
import com.delivery.identity.repository.PlatformSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminConfigurationService {

    private final PlatformSettingsRepository settingsRepository;
    private final AdminAuditRepository auditRepository;

    @Transactional
    public PlatformSettingsDto getSettings() {
        PlatformSettings settings = settingsRepository.findById(1L).orElseGet(PlatformSettings::new);
        return PlatformSettingsDto.fromEntity(settingsRepository.save(settings));
    }

    @Transactional
    public PlatformSettingsDto updateSettings(PlatformSettingsDto request, UUID actorUserId, String actorEmail) {
        PlatformSettings settings = settingsRepository.findById(1L).orElseGet(PlatformSettings::new);
        request.applyTo(settings);
        PlatformSettingsDto saved = PlatformSettingsDto.fromEntity(settingsRepository.save(settings));
        saveAudit(actorUserId, actorEmail, "CONFIGURACOES_ATUALIZADAS", "Regras da plataforma", null);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<AuditEntryDto> getAuditEntries() {
        return auditRepository.findTop100ByOrderByCreatedAtDesc().stream().map(AuditEntryDto::fromEntity).toList();
    }

    @Transactional
    public AuditEntryDto createAuditEntry(AuditCreateRequest request, UUID actorUserId, String actorEmail) {
        return AuditEntryDto.fromEntity(saveAudit(actorUserId, actorEmail, request.getAction(), request.getTarget(), request.getDescription()));
    }

    @Transactional
    public void recordTeamChange(UUID actorUserId, String actorEmail, String action, String target) {
        saveAudit(actorUserId, actorEmail, action, target, null);
    }

    private AdminAuditEntry saveAudit(UUID actorUserId, String actorEmail, String action, String target, String description) {
        AdminAuditEntry entry = new AdminAuditEntry();
        entry.setActorUserId(actorUserId);
        entry.setActorEmail(actorEmail.trim().toLowerCase());
        entry.setAction(action.trim());
        entry.setTarget(target.trim());
        entry.setDescription(description == null ? null : description.trim());
        return auditRepository.save(entry);
    }
}
