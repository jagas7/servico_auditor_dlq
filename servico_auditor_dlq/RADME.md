# Serviço Auditor de DLQ

Este microserviço é responsável por escutar ativamente a Dead Letter Queue (DLQ), aplicar regras de triagem de severidade em mensagens que falharam, e persistir o evento numa base de dados para futura análise.

## Decisão Arquitetural: Arquitetura em Camadas (Layered Architecture)

Para este serviço de Auditoria de DLQ, optei por utilizar a **Arquitetura em Camadas (Listener -> Service -> Repository)** em vez de padrões mais complexos como a Arquitetura Hexagonal (Ports and Adapters).

**Justificativa detalhada:**
1. **Responsabilidade Única e Simplicidade (Evitar Overengineering):** Este serviço possui um domínio de negócio extremamente enxuto e específico. A sua única função é capturar um JSON bruto da fila SQS, somar um valor numérico (`amount`), classificar a severidade e gravar os dados de auditoria. A criação de portas de entrada/saída, adaptadores primários/secundários e modelos de domínio puros (como faríamos numa Arquitetura Hexagonal) traria uma complexidade acidental desnecessária que não agrega valor a este contexto.
2. **Coesão e Fluxo Direto:** A Arquitetura em Camadas permite um fluxo vertical claro, direto e fácil de compreender. O `DlqListener` recebe a mensagem, o `AuditoriaService` aplica a triagem de severidade utilizando `ObjectMapper` (evitando a criação de DTOs desnecessários para preservar o payload original) e o `AuditRepository` persiste o registo usando o Spring Data JPA.
3. **Manutenibilidade:** Sendo um serviço de "apoio" (auditoria e observabilidade), manter o código estruturalmente simples garante que qualquer programador (ou analista de operações) compreenda rapidamente o fluxo, facilitando futuras manutenções ou a adição de novas lógicas de triagem sem ter de navegar por várias interfaces.

## Regra de Negócio (Severidade)
O serviço lê o atributo `amount` de cada item do payload original.
- Soma > 100: Prioridade `HIGH`
- Soma entre 50 e 100: Prioridade `MEDIUM`
- Soma < 50: Prioridade `LOW`