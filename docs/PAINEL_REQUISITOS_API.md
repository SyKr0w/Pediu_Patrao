# Painel Administrativo — PediuPatrão

## Objetivo

Permitir ao ADMIN e ao GERENTE acompanhar vendas e pedidos por meio de indicadores e gráficos. O Painel consulta uma API REST implementada no próprio Spring Boot. O ATENDENTE não tem acesso.

## Requisitos funcionais

| Código | Funcionalidade | Implementação |
| --- | --- | --- |
| RF01 | Indicadores gerais de vendas | Faturamento, pedidos finalizados, ticket médio e cancelamentos |
| RF02 | Análise de vendas | Histórico de vendas diárias (gráfico de linhas) |
| RF03 | Situação dos pedidos | Quantidade por status (gráfico de pizza) |
| RF04 | Produtos mais vendidos | Mais populares por unidades vendidas (colunas) |
| RF05 | Filtros | Data inicial, data final, status e produto |
| RF06 | Integração com API | `fetch()` no navegador consumindo cinco endpoints JSON |
| RF07 | Atualização e erros | Botão Atualizar, indicador de carregamento, mensagens de erro e ausência de dados |

## Requisitos não funcionais

- **Segurança:** Spring Security protege tanto `/painel` quanto `/api/painel/**`, permitindo somente ADMIN e GERENTE. O serviço também usa `@PreAuthorize`.
- **Integridade:** os valores exibidos vêm dos pedidos persistidos no MongoDB; o navegador não calcula faturamento a partir de valores informados pelo usuário.
- **Usabilidade:** interface responsiva, legendas nos gráficos e filtros com botão Limpar.
- **Desempenho:** consultas por período nos campos `entradaEm` e `finalizadoEm`; intervalos limitados a 366 dias.
- **Confiabilidade:** filtros inválidos são rejeitados e não alteram registros no banco.
- **Manutenção:** `PainelController` recebe requisições, `PainelService` calcula métricas, `PedidoRepository` consulta o MongoDB.

## Regras de cálculo

- **Faturamento:** soma do campo `total` dos pedidos `FINALIZADO`, considerando sua data `finalizadoEm` dentro do período. O campo `total` já contém descontos.
- **Pedidos finalizados:** quantidade desses pedidos.
- **Ticket médio:** faturamento dividido pela quantidade de pedidos finalizados. Quando não há vendas, o valor é zero.
- **Cancelamentos:** pedidos que foram registrados no período de entrada (`entradaEm`) e atualmente possuem status `CANCELADO`.
- **Situação atual:** contagem pelo status atual dos pedidos registrados (`entradaEm`) no período.
- **Mais populares:** soma das quantidades de itens dos pedidos finalizados no período, usando `produtoId` para agrupar e o nome gravado no item para exibir.
- **Filtro por produto:** considera apenas pedidos que contêm o produto informado; no ranking, conta apenas as unidades do produto selecionado.
- **Filtro por status:** aplica-se a todos os indicadores. Se o status selecionado não for `FINALIZADO`, o faturamento e o ranking ficam zerados, pois não são vendas concluídas.
- **Período padrão:** últimos 30 dias, incluindo a data atual. Os horários são os horários locais configurados no servidor.

Como o MongoDB será inicializado vazio, não há necessidade de conversão de pedidos antigos. Para que o histórico de vendas seja preciso, o sistema passa a registrar `finalizadoEm` quando o GERENTE altera o status para `FINALIZADO`.

## Endpoints da API

Todas as rotas usam o método **GET** e exigem autenticação como ADMIN ou GERENTE. As respostas são JSON. Para acessar pelo navegador, é necessário estar autenticado na aplicação.

### Parâmetros comuns (opcionais)

| Parâmetro | Exemplo | Descrição |
| --- | --- | --- |
| `inicio` | `2026-11-01` | Primeiro dia do período, inclusive |
| `fim` | `2026-11-10` | Último dia do período, inclusive |
| `status` | `FINALIZADO` | Um status conhecido; vazio significa todos |
| `produtoId` | `id-do-produto` | Filtra por um produto cadastrado |

Valores permitidos para `status`: `RECEBIDO`, `EM_PREPARACAO`, `PRONTO`, `SAIU_PARA_ENTREGA`, `RETIRADO`, `FINALIZADO`, `CANCELADO`.

### 1. `GET /api/painel/resumo`

Exemplo: `GET /api/painel/resumo?inicio=2026-11-01&fim=2026-11-10`

Exemplo de resposta (valores ilustrativos):

```json
{
  "faturamento": 190.00,
  "pedidosFinalizados": 3,
  "ticketMedio": 63.33,
  "cancelamentos": 1,
  "pedidosRegistrados": 5
}
```

### 2. `GET /api/painel/historico`

Exemplo: `GET /api/painel/historico?inicio=2026-11-09&fim=2026-11-10`

```json
[
  {"data": "2026-11-09", "total": 100.00, "pedidos": 2},
  {"data": "2026-11-10", "total": 90.00, "pedidos": 1}
]
```

Dias sem vendas são retornados com total zero para manter a sequência do gráfico.

### 3. `GET /api/painel/situacao`

Exemplo: `GET /api/painel/situacao?status=RECEBIDO`

```json
[
  {"status": "RECEBIDO", "descricao": "Recebido", "quantidade": 2},
  {"status": "EM_PREPARACAO", "descricao": "Em preparação", "quantidade": 0}
]
```

A resposta real inclui os sete status, mesmo quando a quantidade é zero.

### 4. `GET /api/painel/populares`

Exemplo: `GET /api/painel/populares?inicio=2026-11-01&fim=2026-11-10&limite=5`

```json
[
  {"produtoId": "p1", "nome": "Pizza Calabresa", "quantidade": 8},
  {"produtoId": "p2", "nome": "Suco", "quantidade": 5}
]
```

`limite` é opcional (padrão 5), aceitando valores de 1 a 20.

### 5. `GET /api/painel/produtos`

Fornece as opções de produto para o filtro, inclusive os inativos que podem ter vendas históricas.

```json
[
  {"id": "p1", "nome": "Pizza Calabresa"},
  {"id": "p2", "nome": "Suco"}
]
```

## Possíveis erros

| Situação | Resultado esperado |
| --- | --- |
| Não autenticado | Redirecionamento para `/login` pela configuração atual de login web |
| ATENDENTE tenta consultar o Painel ou sua API | HTTP **403** |
| `inicio` posterior a `fim` | HTTP **400** com `{"erro":"A data inicial deve ser anterior ou igual à data final."}` |
| Intervalo superior a 366 dias | HTTP **400** |
| Status desconhecido | HTTP **400** com `{"erro":"Status inválido."}` |
| `limite` fora de 1–20 | HTTP **400** |
| Data fora do formato `AAAA-MM-DD` | HTTP **400** pelo Spring MVC |
| Banco indisponível ou falha inesperada | Erro no servidor; o Painel exibe mensagem em vez de dados antigos |

Os exemplos JSON são demonstrativos, não dados gravados no banco.

## Principais alterações no código

- `Pedido`: novo campo `finalizadoEm`.
- `PedidoService`: registra a data e a hora ao finalizar o pedido e inclui a mudança na auditoria.
- `PedidoRepository`: consultas por período de entrada e por período de finalização.
- `PainelService`: cálculos e filtros.
- `PainelController`: endpoints REST que devolvem JSON.
- `PainelWebController`: abre a tela `/painel` com Thymeleaf.
- `painel.html`, `painel.js` e `painel.css`: interface, requisições e gráficos usando Chart.js **já incluído no projeto**.
- `SecurityConfig` e `sidebar.html`: acesso ao Painel para ADMIN e GERENTE.
