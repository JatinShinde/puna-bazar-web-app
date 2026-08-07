package com.punabazar.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "whatsapp_templates")
public class WhatsAppTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String templateName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String templateContent;

    private Boolean isDefault = false;

    private LocalDateTime createdAt = LocalDateTime.now();

    public WhatsAppTemplate() {}

    public WhatsAppTemplate(String templateName, String templateContent, Boolean isDefault) {
        this.templateName = templateName;
        this.templateContent = templateContent;
        this.isDefault = isDefault != null ? isDefault : false;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }

    public String getTemplateContent() { return templateContent; }
    public void setTemplateContent(String templateContent) { this.templateContent = templateContent; }

    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
