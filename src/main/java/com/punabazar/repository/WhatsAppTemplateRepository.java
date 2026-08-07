package com.punabazar.repository;

import com.punabazar.model.WhatsAppTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WhatsAppTemplateRepository extends JpaRepository<WhatsAppTemplate, Long> {
    Optional<WhatsAppTemplate> findByIsDefaultTrue();
    Optional<WhatsAppTemplate> findByTemplateName(String templateName);
}
