# Painel Administrativo — PediuPatrão

## 1. Objetivo

O Painel permite que ADMIN e GERENTE consultem as vendas e a situação dos pedidos da pizzaria. Os dados são obtidos de uma API REST feita em Spring Boot, que consulta o MongoDB. A tela utiliza Thymeleaf, JavaScript e Chart.js.

## 2. Requisitos funcionais

| Código | Requisito | O que foi desenvolvido |
| --- | --- | --- |
| RF01 | Indicadores de vendas | Faturamento, pedidos finalizados, ticket médio e cancelamentos. |
| RF02 | Análise de vendas | Gráfico de linhas **Histórico de vendas**, por dia. |
| RF03 | Situação dos pedidos | Gráfico de pizza **Situação atual dos pedidos**, por status. |
| RF04 | Produtos mais vendidos | Gráfico de colunas **Mais populares**, por quantidade vendida. |
| RF05 | Filtros | Data inicial, data final, status e produto. |
| RF06 | Integração com API | A tela consulta os endpoints REST com `fetch()`. |
| RF07 | Atualização e erros | Botões Atualizar e Limpar, estado de carregamento e mensagens de erro. |

## 3. Requisitos não funcionais

- **RNF01 — Segurança:** somente ADMIN e GERENTE acessam `/painel` e `/api/painel/**`, com verificação no Spring Security.
- **RNF02 — Integridade:** valores de venda são calculados no servidor a partir dos pedidos armazenados no MongoDB.
- **RNF03 — Usabilidade:** interface responsiva e exibição de mensagens quando não há dados ou quando uma consulta falha.
- **RNF04 — Manutenção:** o Controller recebe as requisições, o Service calcula os indicadores e o Repository consulta o banco.
- **RNF05 — Validação:** datas e filtros inválidos são rejeitados; o intervalo máximo permitido é de 366 dias.

## 4. Endpoints da API

Todos usam **GET**, retornam **JSON** e exigem login com perfil ADMIN ou GERENTE.

Parâmetros comuns opcionais: `inicio` e `fim` (`AAAA-MM-DD`), `status` e `produtoId`. Sem datas, a consulta considera os últimos 30 dias, incluindo o dia atual. Valores de `status`: `RECEBIDO`, `EM_PREPARACAO`, `PRONTO`, `SAIU_PARA_ENTREGA`, `RETIRADO`, `FINALIZADO` e `CANCELADO`.

### 4.1. Resumo de vendas

`GET /api/painel/resumo?inicio=2026-11-01&fim=2026-11-10`

```json
{"faturamento":190.00,"pedidosFinalizados":3,"ticketMedio":63.33,"cancelamentos":1,"pedidosRegistrados":5}
```

Retorna os indicadores. O faturamento utiliza somente pedidos finalizados, com os descontos já aplicados.

### 4.2. Histórico de vendas

`GET /api/painel/historico?inicio=2026-11-09&fim=2026-11-10`

```json
[{"data":"2026-11-09","total":100.00,"pedidos":2},{"data":"2026-11-10","total":90.00,"pedidos":1}]
```

Retorna as vendas agrupadas pela data de finalização. Dias sem vendas aparecem com total zero.

### 4.3. Situação atual dos pedidos

`GET /api/painel/situacao?status=RECEBIDO`

```json
[{"status":"RECEBIDO","descricao":"Recebido","quantidade":2},{"status":"EM_PREPARACAO","descricao":"Em preparação","quantidade":0}]
```

Retorna a quantidade de pedidos por status. A resposta completa contém os sete status.

### 4.4. Produtos mais populares

`GET /api/painel/populares?inicio=2026-11-01&fim=2026-11-10&limite=5`

```json
[{"produtoId":"p1","nome":"Pizza Calabresa","quantidade":8},{"produtoId":"p2","nome":"Suco","quantidade":5}]
```

Conta as unidades vendidas em pedidos finalizados. O parâmetro `limite` é opcional (padrão: 5; permitido: 1 a 20).

### 4.5. Produtos para o filtro

`GET /api/painel/produtos`

```json
[{"id":"p1","nome":"Pizza Calabresa"},{"id":"p2","nome":"Suco"}]
```

Lista as opções de produto do filtro, incluindo os produtos inativos.

### Possíveis erros

| Situação | Resposta esperada |
| --- | --- |
| Usuário sem login | Redirecionamento para `/login`. |
| ATENDENTE acessando a API | HTTP `403` (acesso negado). |
| Data inicial depois da final, período acima do limite ou status inválido | HTTP `400`, com mensagem de erro. |
| `limite` menor que 1 ou maior que 20 | HTTP `400`. |
| Falha de conexão ou erro inesperado | Erro no servidor; a interface informa a falha. |

Exemplo de erro de filtro:

```json
{"erro":"A data inicial deve ser anterior ou igual à data final."}
```
