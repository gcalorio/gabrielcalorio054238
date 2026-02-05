# Music API - Sistema de Gerenciamento de Artistas e Álbuns

Esta API foi desenvolvida como parte de um processo seletivo, fornecendo uma solução robusta para o gerenciamento de artistas, seus álbuns e capas de álbuns, além de integração com serviços externos de regionais.

### DADOS DE INSCRIÇÃO

- **N° Inscrição**: 16460
- **Nome**: GABRIEL CALORIO
- **Email**: gabriel_calorio@hotmail.com
- **CPF**: 054.238.881-23
- **RG**: 18751067
- **PROCESSO SELETIVO**: CONJUNTO Nº 001/2026/SEPLAG e demais Órgãos - Engenheiro da Computação- Sênior
- **Cidade**: Cuiabá
- **Local**: SECRETARIA DE ESTADO DE PLANEJAMENTO E GESTÃO
- **Cargo**: ANALISTA DE TECNOLOGIA DA INFORMAÇÃO
- **Perfil**: ENGENHEIRO DA COMPUTAÇÃO - SÊNIOR

---

### Funcionalidades Principais

- **Gestão de Artistas e Álbuns**: CRUD completo com relacionamento N:N.
- **Armazenamento de Capas**: Integração com Amazon S3/MinIO para upload de imagens com geração de URLs pré-assinadas.
- **Segurança**: Autenticação via JWT (JSON Web Token) com renovação (refresh) de token.
- **Rate Limit**: Limitação de 10 requisições por minuto por usuário autenticado.
- **Notificações em Tempo Real**: WebSocket (STOMP) para notificar a criação de novos álbuns.
- **Sincronização de Regionais**: Consumo de API externa com lógica de inserção, inativação e histórico de alterações.
- **Monitoramento**: Endpoints de Health Check (Liveness e Readiness) via Spring Actuator.
- **Documentação**: Swagger/OpenAPI integrada.

### Tecnologias Utilizadas

- **Java 8** e **Spring Boot 2.7.17**
- **Spring Security** & **JWT**
- **Spring Data JPA** (Hibernate)
- **PostgreSQL** (Produção/Docker) / **H2 Database** (Desenvolvimento/Testes)
- **Flyway** (Migrações de banco de dados)
- **MinIO** (Armazenamento de objetos compatível com S3)
- **Docker** & **Docker Compose**
- **JUnit 5** & **Mockito** (Testes Unitários e de Integração)

---

### Como Executar a Aplicação

#### Opção 1: Usando Docker Compose (Recomendado)

Esta opção sobe a API, o banco de dados PostgreSQL e o servidor MinIO automaticamente.

1. Certifique-se de ter o Docker e o Docker Compose instalados.
2. Na raiz do projeto, execute:
   ```bash
   docker-compose up --build
   ```
3. A API estará disponível em `http://localhost:8080`.
4. O Swagger UI pode ser acessado em `http://localhost:8080/swagger-ui.html`.

#### Opção 2: Execução Local (Maven)

Para execução local sem Docker, a aplicação utilizará o banco de dados H2 (em memória). Note que o upload de arquivos falhará a menos que você tenha um MinIO rodando localmente na porta 9000.

1. Compile o projeto:
   ```bash
   mvn clean install
   ```
2. Execute a aplicação:
   ```bash
   mvn spring-boot:run
   ```

---

### Como Executar os Testes

A aplicação possui uma suite completa de testes unitários e de integração (34 testes no total).

#### Via Maven:
```bash
mvn test
```

#### Cobertura de Testes:
Os testes cobrem:
- Fluxos de Autenticação e Segurança (JWT).
- Lógica de Rate Limit.
- CRUD de Artistas e Álbuns.
- Upload de arquivos e integração com S3 (Mockado nos testes).
- Sincronização de Regionais.
- Endpoints de Health Check.

---

### Decisões de Projeto e Justificativas Técnicas

Nesta seção, detalhamos as motivações por trás das escolhas tecnológicas e arquiteturais feitas durante o desenvolvimento da API.

#### 1. Arquitetura e Frameworks
- **Spring Boot 2.7.17 & Java 8**: Optou-se por uma base sólida e amplamente utilizada no mercado corporativo. O Spring Boot facilita a configuração e o deploy, enquanto o Java 8 garante compatibilidade com sistemas legados e estabilidade.
- **Spring Security + JWT**: A escolha por tokens JWT permite uma autenticação **stateless**, essencial para escalabilidade em ambientes distribuídos (Docker/Kubernetes). O filtro de requisição foi personalizado para incluir não apenas a validação de segurança, mas também o controle de **Rate Limit** de forma centralizada.

#### 2. Estrutura de Dados e Persistência
- **Relacionamento N:N (Artista-Álbum)**: Inicialmente concebido como 1:N, o relacionamento foi evoluído para Many-to-Many utilizando uma tabela de junção (`artist_album`). Isso reflete a realidade da indústria musical, onde álbuns podem ter múltiplos artistas (colaborações) e artistas participam de diversos álbuns.
- **PostgreSQL vs H2**: O uso do PostgreSQL via Docker Compose garante um ambiente de produção idêntico ao de desenvolvimento em termos de comportamento de banco de dados. O H2 foi mantido para agilizar a execução de testes unitários e de integração em ambientes de CI/CD.
- **Flyway**: Utilizado para garantir o versionamento do esquema do banco de dados. Cada alteração (como a mudança para N:N ou a criação da tabela de regionais) é rastreável e aplicada automaticamente.

#### 3. Sincronização e Integração
- **Lógica de Sincronização de Regionais**: Implementou-se uma estratégia de **Soft Delete** e **Versionamento por Registro**. Em vez de apenas atualizar o nome de uma regional, o registro antigo é inativado e um novo é criado. Isso preserva a integridade referencial e o histórico de dados, caso outros registros dependam da versão anterior do nome da regional.
- **Amazon S3 / MinIO**: Para o armazenamento de imagens (capas), optou-se por um serviço de objetos (Object Storage) em vez de salvar no sistema de arquivos local. O uso de **URLs pré-assinadas** garante que os arquivos não fiquem expostos publicamente, exigindo um token temporário gerado pela API para o acesso.

#### 4. Monitoramento e UX
- **WebSockets (STOMP)**: Adicionado para fornecer uma experiência de usuário reativa. Em vez do frontend precisar fazer "polling" para saber se há novos dados, a API "empurra" a notificação assim que um novo álbum é persistido.
- **Spring Actuator**: Implementado para fornecer observabilidade. As probes de **Liveness** e **Readiness** são fundamentais para que orquestradores como o Kubernetes saibam quando o container está pronto para receber tráfego ou quando deve ser reiniciado.

---

### Endpoints Principais

- **Autenticação**: `POST /api/v1/authenticate` (admin/password)
- **Artistas**: `GET/POST/PUT/DELETE /api/v1/artists`
- **Álbuns**: `GET /api/v1/albums`
- **Upload de Capas**: `POST /api/v1/albums/{id}/covers`
- **Regionais**: `GET /v1/regionais`
- **Health**: `/actuator/health`
- **WebSocket**: `/ws-music`