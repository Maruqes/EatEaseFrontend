# EatEase Frontend

Sistema frontend completo para gestão de restaurante, desenvolvido no âmbito da cadeira de **Projeto 2**. O projeto é composto por uma aplicação web para clientes e uma aplicação desktop para administração.

> **Projeto complementar**: Este frontend funciona em conjunto com o backend disponível em [https://github.com/Maruqes/EatEase](https://github.com/Maruqes/EatEase)

## 🎯 Objetivos do Projeto

### Aplicação Web (Cliente)
- Fornecer uma interface moderna e responsiva para clientes
- Permitir visualização do menu do restaurante
- Facilitar o acesso às informações do estabelecimento
- Oferecer uma experiência de utilizador intuitiva

### Aplicação Desktop (Administração)
- Criar uma ferramenta de gestão completa para funcionários
- Permitir administração de stocks e ingredientes
- Facilitar a gestão de funcionários e utilizadores
- Fornecer relatórios e controlo administrativo

## 📱 Componentes

### 🌐 Web Application
- **Tecnologia**: React + TypeScript + Vite
- **Interface**: Moderna e responsiva
- **Funcionalidades**:
  - Visualização do menu por categorias
  - Informações do restaurante
  - Localização e contactos
  - Formulário de contacto

### 🖥️ Desktop Application
- **Tecnologia**: JavaFX + Java 17
- **Interface**: Aplicação nativa com design moderno
- **Funcionalidades**:
  - Sistema de autenticação
  - Gestão de funcionários
  - Administração de ingredientes e stocks
  - Gestão do menu
  - Relatórios administrativos

## 🚀 Como Executar

### Web Application
```bash
cd Web/
npm install
npm run dev
```
Aceder em: `http://localhost:5173`

### Desktop Application
```bash
cd Desktop/
mvn clean package
mvn javafx:run
```

## 📁 Estrutura

```
EatEaseFrontend/
├── Web/              # React App (Cliente)
│   ├── src/
│   │   ├── components/
│   │   └── utils/
│   └── package.json
└── Desktop/          # JavaFX App (Admin)
    ├── src/main/java/
    ├── src/main/resources/
    └── pom.xml
```

## 🔗 Projeto Relacionado

Este frontend é complementado pelo backend REST API disponível em:
**[https://github.com/Maruqes/EatEase](https://github.com/Maruqes/EatEase)**

---

**Projeto desenvolvido no âmbito da cadeira de Projeto 2**

O projeto comunica-se com uma API REST para obter e gerenciar dados. A configuração do proxy está definida em `vite.config.ts` para o frontend web e usa a classe `AppConfig` no aplicativo desktop.

## 🎯 Funcionalidades

### Frontend Web
- **Menu Dinâmico**: Exibe os pratos categorizados (Prato Principal, Entradas, Bebida, Sobremesa)
- **Localização**: Mapa integrado com Google Maps para a localização do restaurante (ESTG-IPVC)
- **Formulário de Contacto**: Interface para os clientes entrarem em contacto
- **Design Responsivo**: Funciona em dispositivos móveis e desktop

### Aplicação Desktop
- **Autenticação**: Sistema de login seguro
- **Gestão de Funcionários**: CRUD completo para gestão de funcionários
- **Gestão de Ingredientes**: Controlo de stock e disponibilidade
- **Interface Moderna**: Construída com JavaFX e ícones Ikonli

## 📄 Licença

Este projeto está licenciado sob a Licença MIT - veja o arquivo [LICENSE](LICENSE) para detalhes.
