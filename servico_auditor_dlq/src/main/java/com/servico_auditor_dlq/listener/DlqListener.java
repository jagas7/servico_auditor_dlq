package com.servico_auditor_dlq.listener;

import com.servico_auditor_dlq.service.AuditoriaService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class DlqListener {

    private static final Logger log = LoggerFactory.getLogger(DlqListener.class);
    private final AuditoriaService auditoriaService;

    public DlqListener(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @SqsListener(value = "${queue.dlq-name}")
    public void receberDaDlq(String payload, @Header(name = "LogicalResourceId", required = false) String queueName) {
        log.info("Mensagem recebida da DLQ. A iniciar processo de auditoria...");

        String filaOrigem = queueName != null ? queueName : "FILA_DLQ_DESCONHECIDA";

        auditoriaService.processarMensagemFalha(payload, filaOrigem);

        log.info("Mensagem processada e guardada na base de dados de auditoria com sucesso.");
    }
}