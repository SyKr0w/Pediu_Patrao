# PediuPatrão

Pagina web com sistema que permite gerenciar clientes, produtos e pedidos de uma pizzaria.

[Como rodar](#como-rodar)

## Tecnologias

Java 21, Spring Boot 3.4.4, Spring Security, Thymeleaf e MongoDB.

## Funcionalidades

- Cadastro e consulta de clientes, produtos e pedidos.
- Três perfis de acesso: ADMIN, GERENTE e ATENDENTE.
- Pedidos com itens, quantidades, preços e cálculo do total no servidor.
- Controle dos estados do pedido, desde o recebimento até a finalização.
- Descontos de até 20% e cancelamentos com justificativa, exclusivos do GERENTE e permitidos apenas antes da saída.
- Auditoria das alterações de pedidos e clientes e das tentativas de acesso bloqueadas.
- Proteção das rotas e APIs com Spring Security.

## Perfis de acesso

| Perfil    | Permissões principais                                                                                     |
| --------- | --------------------------------------------------------------------------------------------------------- |
| ADMIN     | Gerencia usuários, consulta pedidos e consulta a auditoria.                                               |
| GERENTE   | Gerencia produtos e pedidos, consulta clientes, aplica descontos, cancela pedidos e consulta a auditoria. |
| ATENDENTE | Cadastra e altera clientes, registra pedidos e acompanha o andamento.                                     |

## Organização do código

- `controller`: recebe as requisições da interface e da API.
- `service`: contém as regras de negócio e as validações.
- `repository`: acessa os dados no MongoDB.
- `entity`: representa os dados armazenados.
- `config`: configura a segurança e a inicialização.
- `src/main/resources/templates`: páginas HTML com Thymeleaf.

## Testes

Para executar os testes automatizados, abra um terminal na pasta do `pom.xml` e use:

```powershell
.\mvnw.cmd test
```

Foram executados 12 testes, sem falhas, envolvendo pedidos, clientes, auditoria e controle de acesso. As permissões também devem ser verificadas com o sistema em execução. Por exemplo, ao tentar abrir `/api/usuarios` como ATENDENTE, a API deve retornar HTTP 403.

## Como rodar

É necessário ter o JDK 21 instalado, acesso a um banco MongoDB Atlas e o NetBeans ou um terminal.

1. Abra a pasta do projeto que contém o arquivo `pom.xml` no NetBeans.
2. No MongoDB Atlas, crie ou confira o usuário do banco em **Database Access** e libere seu IP em **Network Access**.
3. No Atlas, abra **Connect > Drivers** e copie a URI de conexão.
4. Na raiz do projeto, copie `local.properties.example` para `local.properties`. No novo arquivo, substitua o usuário, a senha e o endereço da conexão pelos dados do seu Atlas. O banco configurado no projeto é `pizzaria`.
5. Caso o banco ainda não tenha o usuário `admin`, inclua a linha `app.bootstrap.admin-password=SUA_SENHA` em `local.properties`, escolhendo uma senha própria. Após a criação do administrador, remova essa linha.
6. Execute o projeto pelo botão **Run** do NetBeans ou, no terminal, use:

```powershell
.\mvnw.cmd spring-boot:run
```

7. Abra [http://localhost:8080/login](http://localhost:8080/login) e entre com o usuário `admin` e a senha configurada. Caso esse usuário já exista no banco, use a senha que ele já tinha.
8. Como admin, cadastre os usuários gerente e atendente na seção de usuários.
