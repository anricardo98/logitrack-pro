-- =====================================================================
-- V4 - CRUD de veículos: protege o histórico
-- O script original usa ON DELETE CASCADE: excluir um veículo apagaria em
-- silêncio todas as viagens (KM do dashboard) e manutenções (custos) dele.
-- Com RESTRICT o banco recusa a exclusão; a API já verifica antes e devolve
-- uma mensagem amigável (VeiculoService.excluir).
-- =====================================================================

ALTER TABLE viagens DROP CONSTRAINT viagens_veiculo_id_fkey;
ALTER TABLE viagens
    ADD CONSTRAINT viagens_veiculo_id_fkey
    FOREIGN KEY (veiculo_id) REFERENCES veiculos (id) ON DELETE RESTRICT;

ALTER TABLE manutencoes DROP CONSTRAINT manutencoes_veiculo_id_fkey;
ALTER TABLE manutencoes
    ADD CONSTRAINT manutencoes_veiculo_id_fkey
    FOREIGN KEY (veiculo_id) REFERENCES veiculos (id) ON DELETE RESTRICT;

-- O ano informado no cadastro precisa ser plausível (a API valida o mesmo intervalo).
ALTER TABLE veiculos
    ADD CONSTRAINT ck_veiculos_ano CHECK (ano IS NULL OR ano BETWEEN 1950 AND 2100);

-- Tipo passa a ser obrigatório: todo veículo é LEVE ou PESADO (métrica "Volume por categoria").
ALTER TABLE veiculos ALTER COLUMN tipo SET NOT NULL;
