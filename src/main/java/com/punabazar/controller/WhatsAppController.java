package com.punabazar.controller;

import com.punabazar.dto.WhatsAppStatementDTO;
import com.punabazar.service.WhatsAppService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/whatsapp")
public class WhatsAppController {

    private final WhatsAppService whatsappService;

    public WhatsAppController(WhatsAppService whatsappService) {
        this.whatsappService = whatsappService;
    }

    @GetMapping("/generate/{customerId}")
    public WhatsAppStatementDTO generateStatement(@PathVariable Long customerId,
                                                @RequestParam(required = false) String style,
                                                @RequestParam(required = false) java.math.BigDecimal sellPo,
                                                @RequestParam(required = false) java.math.BigDecimal sellPc,
                                                @RequestParam(required = false) java.math.BigDecimal payPo,
                                                @RequestParam(required = false) java.math.BigDecimal payPc,
                                                @RequestParam(required = false) java.math.BigDecimal farak,
                                                @RequestParam(required = false) java.math.BigDecimal pagar,
                                                @RequestParam(required = false) java.math.BigDecimal yene,
                                                @RequestParam(required = false) java.math.BigDecimal dene) {
        return whatsappService.generateStatement(customerId, style, sellPo, sellPc, payPo, payPc, farak, pagar, yene, dene);
    }

    @GetMapping("/today-statements")
    public java.util.List<WhatsAppStatementDTO> getTodayStatements() {
        return whatsappService.getTodayStatements();
    }
}
