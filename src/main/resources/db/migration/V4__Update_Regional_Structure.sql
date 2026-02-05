-- Alterar a tabela regional para suportar histórico
-- 1. Criar uma tabela temporária com a nova estrutura
CREATE TABLE regional_new (
    internal_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id INTEGER,
    nome VARCHAR(200),
    ativo BOOLEAN
);

-- 2. Copiar os dados existentes (se houver)
INSERT INTO regional_new (id, nome, ativo)
SELECT id, nome, ativo FROM regional;

-- 3. Remover a tabela antiga
DROP TABLE regional;

-- 4. Renomear a nova tabela
ALTER TABLE regional_new RENAME TO regional;
