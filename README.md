# PediuPatrão — Atividade Prática M1, Parte 2

Sistema de pizzaria em **Java 21, Spring Boot 3.4.4, Spring Security, Thymeleaf e MongoDB Atlas**. Esta entrega evolui o projeto original sem trocar sua estrutura de camadas.

## 1. Como executar no NetBeans (Windows)

1. Abra como **Maven Project** a pasta `PediuPatrao` (a que contém `pom.xml`). Configure **JDK 21 ou superior**.
2. No MongoDB Atlas, confirme o usuário em **Database Access**, autorize seu IP em **Network Access** e copie a URI em **Connect → Drivers**.
3. Na raiz do projeto, **copie** `local.properties.example` para `local.properties` e substitua `USUARIO:SENHA` pelas suas credenciais de usuário *do banco*. A senha na URI deve estar codificada para URL se houver caracteres especiais. **Não envie esse arquivo ao GitHub.**
4. O banco de dados da aplicação é `pizzaria`; o cluster do Atlas pode se chamar `PediuPatrao`.
5. Se o banco ainda não tiver um administrador, descomente a linha `app.bootstrap.admin-password` em `local.properties` e escolha sua senha inicial. Depois do primeiro login, remova essa linha e mantenha o arquivo fora do Git.
6. No NetBeans, execute o projeto pelo botão **Run**, ou em um terminal na pasta do `pom.xml`:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

7. Abra **http://localhost:8080/login**. Se o banco já tiver o usuário `admin` da versão original, a senha cadastrada anteriormente continua válida. A inicialização ajusta a role do admin legado para `ADMIN`.
8. Entre como ADMIN e crie usuários com perfis `GERENTE` e `ATENDENTE` em **Usuários → Novo Usuário**.

**Alternativa sem `local.properties`:** definir a variável de ambiente `MONGODB_URI` na máquina (não publicar a URI no repositório). A configuração padrão de desenvolvimento sem Atlas aponta para `mongodb://localhost:27017/pizzaria`.

### Aviso de segurança

- `local.properties` está no `.gitignore`. Confira `git status` antes de publicar; remova quaisquer senhas antigas já commitadas e troque-as no Atlas.
- Não envie capturas com senhas, tokens ou URIs completas para colegas.
- Todas as operações que alteram dados utilizam CSRF. Formulários Thymeleaf enviam o token automaticamente.
- O usuário administrador não pode mudar o próprio perfil para outro nem excluir a própria conta; também não se pode remover o último ADMIN.

## 2. Perfis e permissões

| Operação | ADMIN | GERENTE | ATENDENTE |
|---|:---:|:---:|:---:|
| Administrar usuários e perfis | Sim | Não | Não |
| Consultar pedidos | Sim | Sim | Sim |
| Cadastrar pedidos | Não | Sim | Sim |
| Mudar status/registrar saída | Não | Sim | Não |
| Aplicar desconto e cancelar | **Não** | **Sim** | **Não** |
| Consultar clientes | Não | Sim | Sim |
| Cadastrar/alterar clientes | Não | Não | Sim |
| Excluir clientes (função preservada) | Não | Sim | Não |
| Consultar produtos | Não | Sim | Sim |
| Gerenciar produtos | Não | Sim | Não |
| Consultar auditoria | Sim | Sim | Não |

As rotas estão protegidas em `SecurityConfig`, e regras sensíveis também possuem `@PreAuthorize` no **Service**. Ocultar um botão não representa a autorização.

## 3. Requisitos implementados

- **RF01:** perfis ADMIN, GERENTE e ATENDENTE; senha BCrypt; cadastro de perfis controlado por ADMIN; proteção das rotas e dos serviços; CSRF habilitado; API de usuários não expõe senha.
- **RF02:** pedido recebe `clienteId` e lista de pares `produtoId`, `quantidade`; preço unitário e subtotal são capturados do banco no servidor e preservados como itens do pedido. O HTTP não define o total.
- **RF03:** `RECEBIDO → EM_PREPARACAO → PRONTO → (SAIU_PARA_ENTREGA | RETIRADO) → FINALIZADO`. Data/hora e usuário da entrada são automáticos; os da saída são registrados na transição. Não há alterações após cancelamento.
- **RF04:** apenas GERENTE aplica desconto percentual de 0% a 20% (antes da saída) ou cancela com justificativa obrigatória (antes da saída). Total recalculado no servidor.
- **RF05:** coleção `auditoria` no MongoDB; eventos de criação de pedidos e clientes, alterações cadastrais, status, desconto, cancelamento e exclusão de cliente. Campos alterados com antes/depois e autor/perfil/data; consulta somente de ADMIN e GERENTE. Não existem endpoints de alteração/exclusão da auditoria.

**Compatibilidade com dados antigos:** pedidos antigos continuam listados com os campos legados; somente os pedidos feitos pelo novo formulário possuem itens e totais confiáveis. Um pedido legado não pode ser atualizado por uma operação que viole os novos estados.

**Limitação técnica:** pedido e registro de auditoria são gravados em operações MongoDB separadas, sem transação distribuída. Para produção, seria necessário estudar uma transação MongoDB ou padrão outbox, para garantir atomicidade sob falha. O projeto é destinado à atividade acadêmica.

## 4. Principais classes e responsabilidade

| Classe | Explicação para apresentação oral |
|---|---|
| `PizzariaPedidosApplication` | Método `main` inicia o servidor Spring Boot. |
| `SecurityConfig` | Declara quem acessa cada caminho, o login e o CSRF. |
| `UsuarioDetailsService` | Busca o usuário no MongoDB e constrói suas autoridades Spring Security a partir da role salva. |
| `UsuarioService` | Cria usuários, codifica senhas, valida perfis e impede autoelevação/remoção do último admin. |
| `DataInitializer` | Atualiza o papel do administrador anterior e cria admin inicial somente com senha configurada. |
| `Pedido` e `ItemPedido` | Modelam o pedido, preços históricos, status, descontos, entrada e saída. |
| `NovoPedidoRequest` | DTO de entrada que aceita só cliente, produto e quantidade, não total fornecido no HTTP. |
| `PedidoService` | Valida produtos, calcula os preços, transições, desconto, cancelamento e cria eventos de auditoria. |
| `ClienteService` | Cadastra e altera clientes e compara os valores antigos/novos antes de salvar auditoria. |
| `Auditoria` / `AuditoriaRepository` / `AuditoriaService` | Representam, persistem e consultam eventos de auditoria no MongoDB. |
| `HomeController`, `PedidoWebController` | Recebem ações dos formulários web e delegam aos serviços. |
| `PedidoController`, `ClienteController`, `UsuarioController` | Endpoints JSON da API; não fazem cálculos e validações de negócio. |

## 5. Testes e demonstração (ordem recomendada)

1. Faça login como **ADMIN**, cadastre um GERENTE e um ATENDENTE.
2. Como **GERENTE**, cadastre um produto ativo (ex.: Pizza Marguerita, R$ 50,00).
3. Como **ATENDENTE**, cadastre cliente com telefone/endereço; edite o telefone e observe depois o evento na auditoria.
4. Ainda como ATENDENTE, crie pedido usando o produto (quantidade 2). O valor inicial deve ser R$ 100,00 e o estado `RECEBIDO`.
5. Como **GERENTE**, aplique 10% de desconto (total R$ 90,00) e avance `EM_PREPARACAO → PRONTO → RETIRADO → FINALIZADO`.
6. Crie um segundo pedido e cancele enquanto está `RECEBIDO`, com justificativa. Não deve permitir mais transições.
7. Como GERENTE, consulte **Auditoria**, com eventos do pedido e do cliente. Como ADMIN, confira que pode consultar mas **não** alterar descontos/cancelamentos.
8. Como ATENDENTE, tente acessar `/usuarios` e `/auditoria`: o resultado deve ser **403 — acesso negado**. Como GERENTE, tente `/usuarios`: também 403.
9. Demonstre uma tentativa indevida diretamente pela API, e não apenas por botões ocultos (exemplo abaixo).

Para demonstrar **backend 403** com sessão de ATENDENTE ou ADMIN, abra `/pedidos` e o console do navegador (F12), usando o ID real de um pedido:

```js
const token = document.querySelector('input[name="_csrf"]').value;
fetch('/api/pedidos/ID_REAL/cancelamento', {
  method: 'POST',
  headers: {'X-CSRF-TOKEN': token, 'Content-Type':'application/x-www-form-urlencoded'},
  body: new URLSearchParams({justificativa:'Teste indevido'})
}).then(r => console.log('HTTP:', r.status)); // 403: somente GERENTE
```

**Importante:** login via sessão/cookie é exigido. Sem CSRF, uma requisição inválida pode retornar 403 pelo CSRF antes mesmo de testar o perfil. Use o token para comprovar especificamente a autorização.

### Testes automatizados

```powershell
.\mvnw.cmd test
```

`PedidoServiceTest` contém cenários de cálculo do pedido, transição inválida, pedido cancelado, desconto acima de 20%, justificativa ausente e registro de saída. A verificação final no MongoDB Atlas e o vídeo devem ser feitos na máquina do aluno.

## 6. Entrega

- Publique o projeto em repositório **individual** no GitHub (sem `local.properties` e sem `.git`).
- Grave vídeo de tela curto, mostrando os três logins, acesso negado, pedido, desconto, saída, cancelamento e a auditoria associada.
- Consulte `GUIA_AVALIACAO_ORAL.md` para revisão das principais perguntas sobre o código.
