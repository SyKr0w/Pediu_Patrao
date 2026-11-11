# Evidências de testes — Painel

A documentação das evidências deve ser preenchida depois de executar os testes no NetBeans e verificar o sistema no navegador. Não incluir capturas que exponham senhas, tokens ou a URI do MongoDB.

## 1. Testes automatizados

Comando executado na pasta do `pom.xml`:

```powershell
.\mvnw.cmd clean test
```

Inserir uma captura do resultado do Maven com `Tests run`, `Failures`, `Errors` e `Skipped`. Os novos cenários em `PainelServiceTest` verificam: soma de faturamento, dias sem vendas, status, produtos mais populares, filtros inválidos, filtro por produto e opções de produto. `PedidoServiceTest` ganhou um teste para a data de finalização.

**Resultado na máquina do aluno:** preencher após executar.

## 2. Testes manuais

| Cenário | Ação | Resultado esperado | Evidência |
| --- | --- | --- | --- |
| Acesso ADMIN | Entrar como ADMIN e abrir `/painel` | HTTP 200, tela abre | Captura da tela |
| Acesso GERENTE | Entrar como GERENTE e abrir `/painel` | HTTP 200 | Captura da tela |
| Bloqueio ATENDENTE | Acessar `/api/painel/resumo` logado como ATENDENTE | HTTP 403 | Captura da aba Network |
| Dados vazios | Abrir o Painel com banco vazio | Indicadores zerados, gráficos sem dados e sem erro 500 | Captura da tela |
| Venda concluída | Criar pedido e avançar até FINALIZADO | Indicadores e Histórico de vendas atualizados | Captura antes/depois |
| Desconto | Aplicar 10% antes de finalizar | Faturamento usa o total com desconto | Captura do pedido e Painel |
| Cancelamento | Cancelar outro pedido | Não soma no faturamento; aumenta cancelamentos | Captura da tela |
| Produto popular | Finalizar pedidos com quantidades diferentes | Mais populares ordenado por quantidade | Captura do gráfico |
| Filtro de período | Escolher datas sem pedidos | Resultado vazio/zero, sem falha | Captura dos filtros |
| Filtro inválido | Colocar data inicial posterior à final | Erro claro, API HTTP 400 | Captura do alerta e Network |
| Persistência | Reiniciar a aplicação e atualizar o Painel | Dados permanecem no MongoDB | Captura após reiniciar |

## 3. Roteiro do vídeo (até 3 minutos)

1. **0:00–0:35:** identificar a branch `dashboard` e mostrar a tela do Painel como ADMIN.
2. **0:35–1:20:** mostrar os indicadores, Histórico de vendas e gráficos de pizza e colunas com dados reais.
3. **1:20–2:00:** alterar período, status e produto; utilizar Atualizar e Limpar.
4. **2:00–2:35:** abrir o DevTools/Network e mostrar a resposta JSON de um endpoint da API. Demonstrar um HTTP 403 com ATENDENTE autenticado.
5. **2:35–3:00:** explicar `PainelController` → `PainelService` → `PedidoRepository` → MongoDB e a data `finalizadoEm`.

Anexar as capturas reais na pasta `docs/evidencias/` e referenciá-las neste arquivo ao concluir a validação.
