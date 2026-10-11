-- =====================================================================
-- V3 - Dados de demonstração
-- As manutenções da carga inicial são de 2024; sem datas futuras o
-- "Cronograma" e a "Projeção do mês" ficariam vazios. Estas linhas usam
-- datas relativas a CURRENT_DATE para o dashboard sempre ter conteúdo.
-- =====================================================================

-- O usuário administrador é criado pela aplicação na inicialização (DataSeeder),
-- para que o hash BCrypt seja gerado pelo próprio PasswordEncoder.

INSERT INTO viagens (veiculo_id, data_saida, data_chegada, origem, destino, km_percorrida) VALUES
(3, NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days' + INTERVAL '6 hours', 'Natal', 'Recife', 286.00),
(4, NOW() - INTERVAL '8 days',  NOW() - INTERVAL '7 days', 'Fortaleza', 'Salvador', 1189.00),
(4, NOW() - INTERVAL '3 days',  NOW() - INTERVAL '2 days', 'Salvador', 'Natal', 1074.00),
(2, NOW() - INTERVAL '5 days',  NOW() - INTERVAL '4 days', 'Belo Horizonte', 'São Paulo', 586.00);

INSERT INTO manutencoes (veiculo_id, data_inicio, data_finalizacao, tipo_servico, custo_estimado, status) VALUES
(4, CURRENT_DATE + 2,  CURRENT_DATE + 3,  'Troca de Óleo',       480.00, 'PENDENTE'),
(1, CURRENT_DATE + 5,  CURRENT_DATE + 5,  'Alinhamento e Balanceamento', 260.00, 'PENDENTE'),
(3, CURRENT_DATE + 9,  CURRENT_DATE + 11, 'Revisão de Motor',   3200.00, 'PENDENTE'),
(2, CURRENT_DATE + 14, CURRENT_DATE + 15, 'Troca de Pneus',     5600.00, 'PENDENTE'),
(4, CURRENT_DATE + 21, CURRENT_DATE + 23, 'Revisão de Freios',  1900.00, 'PENDENTE'),
(1, CURRENT_DATE + 30, CURRENT_DATE + 30, 'Troca de Filtros',    190.00, 'PENDENTE'),
(2, CURRENT_DATE - 1,  CURRENT_DATE + 1,  'Sistema Elétrico',    950.00, 'EM_REALIZACAO');
