CRUD do agendamento de manutenção. O veículo pode ser um **já cadastrado** (`veiculoId`) ou um **novo** (`novoVeiculo`), nunca os dois.

**Regras de negócio**
- A data de finalização não pode ser anterior à de início.
- Para concluir, a data de finalização é obrigatória (`PATCH /status` preenche com hoje se faltar).
- O mesmo veículo não pode ter duas manutenções não concluídas com períodos sobrepostos.

### Ciclo de vida do status

```mermaid
stateDiagram-v2
    [*] --> PENDENTE: agendamento criado
    PENDENTE --> EM_REALIZACAO: serviço iniciado
    EM_REALIZACAO --> CONCLUIDA: serviço finalizado
    PENDENTE --> CONCLUIDA: registro histórico
    CONCLUIDA --> [*]
```

### Agendar com um veículo novo

O veículo e a manutenção são gravados na mesma transação: se a manutenção for recusada, o veículo também não é salvo.

```mermaid
sequenceDiagram
    participant F as Frontend
    participant C as ManutencaoController
    participant M as ManutencaoService
    participant V as VeiculoService
    participant DB as PostgreSQL
    F->>C: POST /api/manutencoes {novoVeiculo, dataInicio, ...}
    C->>C: valida o DTO (veiculoId OU novoVeiculo)
    C->>M: criar(req)
    Note over M,DB: início da transação
    M->>V: cadastrar(novoVeiculo)
    V->>DB: placa já existe?
    alt placa repetida
        V-->>F: 422 "Já existe um veículo com a placa ..."
    else placa livre
        V->>DB: INSERT veiculos
        M->>M: valida datas e status
        M->>DB: INSERT manutencoes
        Note over M,DB: commit
        M-->>F: 201 Created + Location
    end
```
