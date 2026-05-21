package com.servico_auditor_dlq.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.servico_auditor_dlq.entity.AuditEntity;
import com.servico_auditor_dlq.repository.AuditRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuditoriaService {

    private final AuditRepository repository;
    private final ObjectMapper objectMapper;

    public AuditoriaService(AuditRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public void processarMensagemFalha(String payloadOriginal, String nomeFila) {
        try {
            // 1. Navegar no JSON original para encontrar os itens (orderItems) e somar a quantidade (amount)
            JsonNode root = objectMapper.readTree(payloadOriginal);
            JsonNode orderItems = root.get("orderItems");

            int quantidadeTotal = 0;
            if (orderItems != null && orderItems.isArray()) {
                for (JsonNode item : orderItems) {
                    quantidadeTotal += item.get("amount").asInt();
                }
            }

            // 2. Regra de Negócio: Triagem de Severidade
            String severity = "LOW";
            if (quantidadeTotal > 100) {
                severity = "HIGH";
            } else if (quantidadeTotal >= 50) { // Cobre de 50 a 100 inclusive
                severity = "MEDIUM";
            }

            // 3. Montar a entidade de auditoria para gravar no banco
            AuditEntity audit = new AuditEntity();
            audit.setErrorId(UUID.randomUUID());
            audit.setQueueName(nomeFila);
            audit.setPayload(payloadOriginal); // Guardamos o texto bruto que falhou
            audit.setTimestamp(LocalDateTime.now());
            audit.setStatus("PENDING_ANALYSIS");
            audit.setSeverity(severity);

            // 4. Salvar na base de dados
            repository.save(audit);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar a auditoria do JSON da DLQ", e);
        }
    }
}