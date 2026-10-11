-- =====================================================================
-- V2 - Ajustes de modelagem (justificativas no README)
-- =====================================================================

-- 1. Integridade: status só aceita os 3 valores do domínio
UPDATE manutencoes SET status = 'PENDENTE' WHERE status IS NULL;
ALTER TABLE manutencoes ALTER COLUMN status SET NOT NULL;
ALTER TABLE manutencoes
    ADD CONSTRAINT ck_manutencoes_status
    CHECK (status IN ('PENDENTE', 'EM_REALIZACAO', 'CONCLUIDA'));

-- 2. Integridade: toda manutenção pertence a um veículo
ALTER TABLE manutencoes ALTER COLUMN veiculo_id SET NOT NULL;
ALTER TABLE manutencoes ALTER COLUMN tipo_servico SET NOT NULL;

-- 3. Regras de negócio no banco (última linha de defesa)
ALTER TABLE manutencoes
    ADD CONSTRAINT ck_manutencoes_datas
    CHECK (data_finalizacao IS NULL OR data_finalizacao >= data_inicio);
ALTER TABLE manutencoes
    ADD CONSTRAINT ck_manutencoes_custo
    CHECK (custo_estimado IS NULL OR custo_estimado >= 0);
ALTER TABLE viagens
    ADD CONSTRAINT ck_viagens_km CHECK (km_percorrida IS NULL OR km_percorrida >= 0);

-- 4. Índices para as consultas do dashboard e do CRUD
--    (o PostgreSQL NÃO cria índice automaticamente em chaves estrangeiras)
CREATE INDEX idx_viagens_veiculo        ON viagens (veiculo_id);
CREATE INDEX idx_manutencoes_veiculo    ON manutencoes (veiculo_id);
CREATE INDEX idx_manutencoes_data_inicio ON manutencoes (data_inicio);
CREATE INDEX idx_manutencoes_status_data ON manutencoes (status, data_inicio);

-- 5. Tabela de usuários para autenticação (diferencial: login)
CREATE TABLE usuarios (
    id         SERIAL PRIMARY KEY,
    nome       VARCHAR(100) NOT NULL,
    email      VARCHAR(120) NOT NULL UNIQUE,
    senha_hash VARCHAR(100) NOT NULL,
    perfil     VARCHAR(20)  NOT NULL DEFAULT 'GESTOR' CHECK (perfil IN ('ADMIN', 'GESTOR')),
    criado_em  TIMESTAMP    NOT NULL DEFAULT NOW()
);
