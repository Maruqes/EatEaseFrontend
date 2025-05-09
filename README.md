# EatEase - Sistema de Gestão de Restaurante

![Versão](https://img.shields.io/badge/Versão-1.0-blue)
![Licença](https://img.shields.io/badge/Licença-MIT-green)

EatEase é um sistema completo de gestão para restaurantes, com uma interface web para clientes e uma aplicação desktop para administração. O projeto foi desenvolvido para oferecer uma experiência fluida tanto para os clientes ao visualizarem o menu e informações do restaurante, quanto para os funcionários na gestão do estabelecimento.

## 📋 Tabela de Conteúdos

- [Visão Geral](#-visão-geral)
- [Tecnologias](#-tecnologias)
- [Estrutura do Projeto](#-estrutura-do-projeto)
- [Requisitos](#-requisitos)
- [Instalação e Configuração](#-instalação-e-configuração)
  - [Frontend Web](#frontend-web)
  - [Aplicação Desktop](#aplicação-desktop)
- [API](#-api)
- [Funcionalidades](#-funcionalidades)
- [Contribuição](#-contribuição)
- [Licença](#-licença)

## 🔭 Visão Geral

O EatEase é composto por duas aplicações principais:

1. **Frontend Web (Cliente)**: Uma interface web responsiva desenvolvida com React, TypeScript e Vite, que permite aos clientes:
   - Visualizar o menu do restaurante com categorias (Prato Principal, Entradas, Bebida, Sobremesa)
   - Consultar informações sobre o estabelecimento
   - Ver a localização do restaurante
   - Enviar formulários de contacto

2. **Aplicação Desktop (Administração)**: Uma aplicação JavaFX para gestão do restaurante, que permite aos funcionários:
   - Realizar login com autenticação segura
   - Gerir funcionários
   - Administrar ingredientes e stocks
   - Gestão dos pratos do menu

## 🚀 Tecnologias

### Frontend Web
- **React** (v19.0.0)
- **TypeScript**
- **Vite** (v6.2.0)
- **TailwindCSS** (v4.1.5)
- **React Router DOM** (v7.6.0)

### Aplicação Desktop
- **Java** (v17)
- **JavaFX** (v21)
- **Maven**
- **Ikonli** (v12.3.1) - Para ícones modernos

### API e Comunicação
- Integração com backend via REST API
- Proxy configurado para comunicação segura

## 📁 Estrutura do Projeto

```
EatEaseFrontend/
├── Desktop/                  # Aplicação de administração em Java
│   ├── pom.xml               # Configuração Maven
│   └── src/                  # Código fonte Java
│       ├── main/
│       │   ├── java/         # Classes Java
│       │   └── resources/    # Recursos (CSS, imagens)
│       └── test/             # Testes unitários
├── Web/                      # Frontend para clientes
│   ├── public/               # Arquivos públicos
│   ├── src/                  # Código fonte React
│   │   ├── assets/           # Imagens e recursos
│   │   ├── components/       # Componentes React
│   │   └── utils/            # Funções utilitárias
│   ├── package.json          # Dependências NPM
│   └── vite.config.ts        # Configuração do Vite
└── LICENSE                   # Licença do projeto
```

## 📋 Requisitos

### Frontend Web
- Node.js (18+)
- npm ou yarn

### Aplicação Desktop
- JDK 17+
- Maven 3.6+

## 💻 Instalação e Configuração

### Frontend Web

1. Navegue até a pasta Web:
```bash
cd EatEaseFrontend/Web
```

2. Instale as dependências:
```bash
npm install
```

3. Inicie o servidor de desenvolvimento:
```bash
npm run dev
```

4. Para build de produção:
```bash
npm run build
```

Por padrão, o servidor estará disponível em `http://localhost:5173/`

### Aplicação Desktop

1. Navegue até a pasta Desktop:
```bash
cd EatEaseFrontend/Desktop
```

2. Compile o projeto com Maven:
```bash
mvn clean package
```

3. Execute a aplicação:
```bash
java -jar target/javafx-login-1.0-SNAPSHOT.jar
```

Ou usando o plugin Maven:
```bash
mvn javafx:run
```

## 🔌 API

O projeto comunica-se com uma API REST para obter e gerenciar dados. A configuração do proxy está definida em `vite.config.ts` para o frontend web e usa a classe `AppConfig` no aplicativo desktop.

### Endpoints principais:

- `/item/getAll` - Obter todos os itens do menu
- `/auth/login` - Autenticação de utilizadores (Desktop)
- `/funcionario/getAll` - Obter todos os funcionários (Desktop)
- `/ingrediente/getAll` - Obter todos os ingredientes (Desktop)

## 🎯 Funcionalidades

### Frontend Web
- **Menu Dinâmico**: Exibe os pratos categorizados (Prato Principal, Entradas, Bebida, Sobremesa)
- **Filtragem Inteligente**: Opção para filtrar por pratos compostos
- **Localização**: Mapa integrado com Google Maps para a localização do restaurante (ESTG-IPVC)
- **Formulário de Contacto**: Interface para os clientes entrarem em contacto
- **Design Responsivo**: Funciona em dispositivos móveis e desktop

### Aplicação Desktop
- **Autenticação**: Sistema de login seguro
- **Gestão de Funcionários**: CRUD completo para gestão de funcionários
- **Gestão de Ingredientes**: Controlo de stock e disponibilidade
- **Interface Moderna**: Construída com JavaFX e ícones Ikonli

## 🤝 Contribuição

1. Faça um Fork do projeto
2. Crie a sua Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit suas alterações (`git commit -m 'Add: AmazingFeature'`)
4. Push para a Branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📄 Licença

Este projeto está licenciado sob a Licença MIT - veja o arquivo [LICENSE](LICENSE) para detalhes.

---

Desenvolvido por Equipe EatEase © 2025
