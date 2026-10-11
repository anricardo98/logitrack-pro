API do **LogiTrack Pro**: agendamento de manutenção, cadastro de veículos e dashboard da frota.

**Como testar aqui:** chame `POST /api/auth/login` com `admin@logitrack.com` / `admin123`, copie o `token` da resposta, clique em **Authorize** e cole o token. As demais rotas passam a responder.

Erros seguem o padrão RFC 7807 (`application/problem+json`): `detail` traz a mensagem e, em erros de validação, `campos` traz a mensagem de cada campo.

### Arquitetura

```mermaid
flowchart LR
    UI["React (SPA)"] -->|"HTTP + JWT"| C["Controllers<br/>rotas e validação"]
    C --> S["Services<br/>regras de negócio"]
    S --> R["Repositories<br/>JPA no CRUD · SQL no dashboard"]
    R --> DB[("PostgreSQL<br/>Flyway V1 a V4")]
```

### Modelo de dados

```mermaid
erDiagram
    VEICULOS ||--o{ VIAGENS : realiza
    VEICULOS ||--o{ MANUTENCOES : recebe
    VEICULOS {
        int id PK
        varchar placa UK "ABC-1234 ou ABC1D23"
        varchar modelo
        varchar tipo "LEVE | PESADO"
        int ano "1950 a 2100"
    }
    VIAGENS {
        int id PK
        int veiculo_id FK
        timestamp data_saida
        timestamp data_chegada
        varchar origem
        varchar destino
        decimal km_percorrida
    }
    MANUTENCOES {
        int id PK
        int veiculo_id FK
        date data_inicio
        date data_finalizacao
        varchar tipo_servico
        decimal custo_estimado
        varchar status "PENDENTE | EM_REALIZACAO | CONCLUIDA"
    }
    USUARIOS {
        int id PK
        varchar email UK
        varchar senha_hash "BCrypt"
        varchar perfil "ADMIN | GESTOR"
    }
```

As chaves estrangeiras usam `ON DELETE RESTRICT` (migração V4): um veículo com viagens ou manutenções não pode ser excluído.
