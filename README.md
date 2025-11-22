# Gerenciador de Senhas 

##  Sobre o Projeto

Sistema seguro para gerenciamento de senhas desenvolvido na linguagem 
Java com o framework **Spring Boot** os principios de segurança do **OWASP** e princípios criptográficos do **Crypto101** 
esse projeto foi feito pensado em ser usual nao apenas so mais um projeto, ele segue as normas de seguranca do bitwarden. 

---

## Princípios de Segurança Implementados

### Criptografia (Baseado no Crypto101)
- **AES-256-GCM** para criptografia de dados sensíveis
- **PBKDF2WithHmacSHA256** com 200.000 iterações para derivação de chaves
- **Salts únicos** por cofre para prevenir rainbow table attacks
- **IVs aleatórios** para cada operação de criptografia

### Prevenção de Ataques (OWASP Top 10)
- **Rate Limiting** - Máximo 5 tentativas de login 
- **Validação de Força de Senhas** - Blacklist de senhas comuns
- **SQL Injection Prevention** - JPA com parâmetros parametrizados
- **XSS Protection** - Validação de entrada e Content-Type headers
- **JWT com expiração** - Tokens válidos por 2 horas

### Autenticação e Autorização
- **BCrypt** para hashing de senhas
- **JWT** para autenticação stateless
- **Autorização por recurso** - Usuários só acessam seus próprios cofres
- **CORS configurado** - Origens específicas permitidas

---

## Arquitetura

### Tecnologias Utilizadas
- **Java 17** + Spring Boot 3
- **PostgreSQL** - Banco de dados
- **JWT** - Autenticação
- **JPA/Hibernate** - ORM
- **Docker** - Containerização
- **Swagger/OpenAPI** - Documentação

### Estrutura do Projeto
```
src/
├── 📁 LoginGerenciadorDeSenha/
│   ├── 📁 controller/      # Auth, PasswordReset, User
│   ├── 📁 domain/          # Entidades (User)
|   ├── 📁 dto/             # Controle de fluxo de dados Login,Register,Response
│   ├── 📁 infra/security/  # Configurações de segurança
│   └── 📁 repository/      # Repositórios JPA
└── 📁 Vault/
    ├── 📁 controller/      # Gestão de cofres e entradas
    ├── 📁 domain/          # Vault, VaultEntry, AuditLog
    ├── 📁 dto/             # Controle de fluxo de dados 
    ├── 📁 service/         # Lógica de negócio e criptografia
    └── 📁 repository/      # Acesso a dados
```

---

##  Como Executar

### Pré-requisitos
- Java 17+
- Docker e Docker Compose
- Maven 3.6+

### Execução com Docker (Recomendado)
```bash
# 1. Clone o repositório
git clone <url-do-repositorio>
cd gerenciador-senhas

# 2. Execute com Docker Compose
docker-compose up -d

# 3. Acesse a aplicação
# API: http://localhost:8080
# Swagger: http://localhost:8080/swagger-ui.html
# PGAdmin: http://localhost:5050
```

###  Execução Local
```bash
# 1. Configure o banco de dados
docker-compose up db -d

# 2. Execute a aplicação
mvn spring-boot:run

# 3. Acesse: http://localhost:8080
```

---

## API Endpoints

###  Autenticação
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/auth/register` | Registrar novo usuário |
| `POST` | `/auth/login` | Login e obtenção de JWT |
| `POST` | `/auth/forgot-password` | Solicitar reset de senha |
| `POST` | `/auth/reset-password` | Resetar senha com token |

### Gerenciamento de Cofres
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/vault` | Criar novo cofre |
| `GET` | `/vault` | Listar cofres do usuário |
| `PUT` | `/vault/{id}` | Atualizar cofre |
| `DELETE` | `/vault/{id}` | Excluir cofre |

### Entradas de Senha
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/vault/{id}/entries` | Adicionar entrada |
| `GET` | `/vault/{id}/entries` | Listar entradas |
| `DELETE` | `/vault/{id}/entries/{entryId}` | Excluir entrada |

### Exportação/Importação
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/vault/export` | Exportar cofres (JSON) |
| `POST` | `/vault/import` | Importar cofres (JSON) |

---

## Testes de Segurança

### Cobertura Atual
- **Controllers**: Auth, PasswordReset, Vault
- **Serviços**: Criptografia, Auditoria
- **Segurança**: JWT, BCrypt, Rate Limiting


### Testes Implementados
```java
// Exemplo: Teste de força bruta
@Test
void whenLoginWithValidCredentials_thenReturnToken() {
    // Testa login bem-sucedido com geração de JWT
    // Verifica: Status 200, token gerado, auditoria registrada
}

@Test
void whenLoginWithInvalidCredentials_thenReturnUnauthorized() {
    // Testa credenciais inválidas
    // Verifica: Status 401, rate limiting incrementado
}

@Test
void whenLoginWithBlockedEmail_thenReturnTooManyRequests() {
    // Testa proteção contra força bruta
    // Verifica: Status 429 após 5 tentativas
}

@Test
void whenRegisterWithValidData_thenReturnSuccess() {
    // Testa registro com senha forte
    // Verifica: Status 200, usuário salvo, auditoria
}

@Test
void whenRegisterWithExistingEmail_thenReturnBadRequest() {
    // Testa duplicação de email
    // Verifica: Status 400, mensagem de erro
}

@Test
void whenRegisterWithWeakPassword_thenReturnBadRequest() {
    // Testa validação de força de senha
    // Verifica: Status 400, rejeição de senha fraca
}
@Test
void testResetPasswordSuccess() {
    // Testa fluxo completo de reset de senha
    // Verifica: Geração de token, validação, atualização segura
}

@Test
void testForgotPasswordEmailNotFound() {
    // Testa email inexistente
    // Verifica: Status 400, prevenção de enumeração
}

@Test
void testResetPasswordWeakPassword() {
    // Testa validação de senha no reset
    // Verifica: Rejeição de senhas fracas
}

@Test
void testResetPasswordTokenInvalido() {
    // Testa tokens expirados/inválidos
    // Verifica: Status 400, segurança contra reuse
}@Test
void shouldCreateVault() {
    // Testa criação segura de cofres
    // Verifica: Hash da chave, associação correta ao usuário
}

@Test
void shouldReturnUserVaults() {
    // Testa listagem isolada por usuário
    // Verifica: Princípio do menor privilégio
}

@Test
void shouldAddEntryToCorrectVault() {
    // Testa adição segura de entradas
    // Verifica: Criptografia AES-256-GCM, autorização
}

@Test
void shouldDenyAccessWhenVaultNotOwned() {
    // Testa controle de acesso rigoroso
    // Verifica: Status 403 para acesso não autorizado
}

```

---

## Configuração

### Variáveis de Ambiente
```properties
# Banco de Dados
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/gerenciadordesenhas
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=sua_senha

# JWT
JWT_SECRET=seu_jwt_secret_super_seguro

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:4200

# Spring
SPRING_PROFILES_ACTIVE=dev
```

### Docker Compose
```yaml
services:
  app:
    image: gerenciador-senhas:latest
    environment:
      - JWT_SECRET=${JWT_SECRET}
      - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/gerenciadordesenhas
      
  db:
    image: postgres:15-alpine
    environment:
      - POSTGRES_DB=gerenciadordesenhas
      - POSTGRES_PASSWORD=${DB_PASSWORD}
```

---

## Monitoramento e Auditoria

### Logs de Auditoria
- **Login bem-sucedido/falho**
- **Registro de novos usuários**
- **Operações em cofres** (criação, exclusão)
- **Acesso a entradas de senha**

### Health Checks
```bash
# Health Check da aplicação
GET /actuator/health

# Health Check do banco (automático)
# Via Docker healthcheck
```

---

## Resposta a Incidentes

### Fluxo de Recuperação
1. **Detecção** via logs de auditoria
2. **Bloqueio** automático por rate limiting
3. **Notificação** ao usuário
4. **Reset** de credenciais comprometidas

### Checklist de Segurança
- [x] Senhas hasheadas com BCrypt
- [x] Dados sensíveis criptografados com AES-256
- [x] Rate limiting implementado
- [x] JWT com expiração
- [x] CORS configurado
- [x] SQL injection prevenido
- [x] XSS protection ativo
- [x] Logs de auditoria
---


---
### Projeto de autoria: 

www.linkedin.com/in/jonatadev

https://github.com/devJonatas06

*Documentação baseada nas práticas do OWASP, Crypto101 e arquitetura do Bitwarden para garantir a máxima segurança dos dados dos usuários.*