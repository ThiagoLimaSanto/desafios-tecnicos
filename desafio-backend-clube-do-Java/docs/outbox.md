# Publicação de eventos com outbox

Os sete tipos de eventos de propostas, negociação, aceite, rejeição, conclusão,
finalização e correções são gravados em `outbox_events` na mesma transação JPA
que altera o projeto/proposta. `OutboxService.enqueue` exige uma transação existente
(`MANDATORY`). Se a gravação do evento falhar, a alteração de negócio também é desfeita.
As requisições não acessam mais o RabbitMQ diretamente.

## Banco e execução

Com `JPA_DDL_AUTO=update`, o Hibernate cria a tabela e seu índice. Para bancos
administrados por scripts (`validate`/`none`), aplique `db/outbox.sql` antes de iniciar
a versão atualizada. O projeto não executa esse script automaticamente.

O agendador roda a cada segundo, processando até 50 eventos por rodada, em
transações individuais. A consulta PostgreSQL usa `FOR UPDATE SKIP LOCKED` para
que publicadores concorrentes não enviem a mesma linha ao mesmo tempo.
A conexão/transação do banco permanece aberta durante a espera pela confirmação
(até 5 segundos por evento, além dos tempos de conexão/envio do cliente RabbitMQ).

Configurações opcionais:

- `outbox.enabled=false`: desativa o agendador, mantendo os eventos pendentes.
- `outbox.poll-delay-ms`: intervalo entre rodadas; padrão 1000.
- `outbox.confirm-timeout-ms`: espera por confirmação; padrão 5000.

O RabbitMQ usa publisher confirms correlacionados, publisher returns e envio
mandatory. Somente ACK sem retorno por falta de rota preenche `published_at`.
Falhas, NACKs e timeouts preservam o evento, incrementam `attempts`, registram
`last_error` e agendam outra tentativa para 30 segundos depois. Uma queda do
processo antes de confirmar a transação deixa o evento disponível para reenvio.
Os registros publicados são mantidos para auditoria; a retenção deve ser definida
conforme o volume. Monitore a quantidade/idade dos pendentes e `last_error`.

## Entrega e consumidores

A entrega é pelo menos uma vez: uma queda após o ACK e antes do commit da outbox
pode causar reenvio. O UUID do evento permanece igual nos campos AMQP `messageId`
e `eventId` em todas as tentativas. Os listeners atuais apenas imprimem mensagens;
logs duplicados são possíveis e não alteram o estado de negócio.

Ao adicionar efeitos de negócio aos consumidores, use o `eventId` para deduplicar
com uma restrição única e registre o processamento na mesma transação das alterações.
Para efeitos externos, como pagamentos ou email, use também a idempotência suportada
pelo serviço de destino. Não há garantia de ordem global entre filas/publicadores.

## Validação

`./mvnw -Dtest=OutboxIntegrationTest,ProposalServiceTest,ProjectCorrectionControllerTest,ProposalRequestTest test`

Os testes da outbox usam H2 e RabbitTemplate simulado, cobrindo o ciclo dos serviços,
commit/rollback, transação obrigatória, visibilidade após commit, concorrência,
serialização, ACK, NACK, falta de rota, timeout e recuperação após falha.
Uma validação de infraestrutura ainda precisa ser feita com PostgreSQL e RabbitMQ reais.
