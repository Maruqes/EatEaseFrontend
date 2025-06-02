package com.EatEaseFrontend;

/**
 * Classe utilitária para centralizar e padronizar as mensagens de erro da
 * aplicação
 */
public class ErrorMessages {

    // Mensagens de erro de conectividade
    public static class Network {
        public static final String CONNECTION_FAILED = "Falha na conexão com o servidor";
        public static final String CONNECTION_TIMEOUT = "Tempo limite de conexão excedido";
        public static final String SERVER_UNAVAILABLE = "Servidor temporariamente indisponível";
        public static final String INTERNET_CONNECTION = "Verifique sua conexão com a internet";
    }

    // Mensagens de erro de dados
    public static class Data {
        public static final String LOADING_FAILED = "Erro ao carregar dados";
        public static final String SAVING_FAILED = "Erro ao salvar dados";
        public static final String DELETING_FAILED = "Erro ao excluir dados";
        public static final String UPDATING_FAILED = "Erro ao atualizar dados";
        public static final String INVALID_DATA = "Dados inválidos fornecidos";
        public static final String DATA_NOT_FOUND = "Dados não encontrados";
    }

    // Mensagens específicas para ingredientes
    public static class Ingredients {
        public static final String LOAD_TITLE = "Erro ao Carregar Ingredientes";
        public static final String LOAD_HEADER = "Não foi possível carregar a lista de ingredientes";
        public static final String LOAD_MESSAGE = "Verifique sua conexão com a internet e tente novamente. Se o problema persistir, entre em contato com o suporte técnico.";

        public static final String ADD_TITLE = "Erro ao Adicionar Ingrediente";
        public static final String ADD_HEADER = "Não foi possível adicionar o novo ingrediente";
        public static final String ADD_MESSAGE = "Verifique se todos os campos foram preenchidos corretamente e tente novamente.";

        public static final String UPDATE_TITLE = "Erro ao Atualizar Ingrediente";
        public static final String UPDATE_HEADER = "Não foi possível atualizar as informações do ingrediente";
        public static final String UPDATE_MESSAGE = "Verifique se as informações estão corretas e tente novamente.";

        public static final String DELETE_TITLE = "Erro ao Excluir Ingrediente";
        public static final String DELETE_HEADER = "Não foi possível excluir o ingrediente";
        public static final String DELETE_MESSAGE = "O ingrediente pode estar sendo usado em outros itens. Verifique as dependências antes de excluir.";

        public static final String STOCK_TITLE = "Erro na Movimentação de Stock";
        public static final String STOCK_HEADER = "Não foi possível atualizar o stock do ingrediente";
        public static final String STOCK_MESSAGE = "Verifique se a quantidade informada é válida e tente novamente.";
    }

    // Mensagens específicas para itens
    public static class Items {
        public static final String LOAD_TITLE = "Erro ao Carregar Itens";
        public static final String LOAD_HEADER = "Não foi possível carregar a lista de itens";
        public static final String LOAD_MESSAGE = "Verifique sua conexão com a internet e tente novamente. Se o problema persistir, entre em contato com o suporte técnico.";

        public static final String CREATE_TITLE = "Erro ao Criar Item";
        public static final String CREATE_HEADER = "Não foi possível criar o novo item";
        public static final String CREATE_MESSAGE = "Verifique se todos os campos obrigatórios foram preenchidos corretamente e tente novamente.";

        public static final String UPDATE_TITLE = "Erro ao Atualizar Item";
        public static final String UPDATE_HEADER = "Não foi possível atualizar as informações do item";
        public static final String UPDATE_MESSAGE = "Verifique se as informações estão corretas e tente novamente.";

        public static final String DELETE_TITLE = "Erro ao Excluir Item";
        public static final String DELETE_HEADER = "Não foi possível excluir o item";
        public static final String DELETE_MESSAGE = "O item pode estar sendo usado em menus ou pedidos ativos. Verifique as dependências antes de excluir.";

        public static final String LOAD_INGREDIENTS_TITLE = "Erro ao Carregar Ingredientes";
        public static final String LOAD_INGREDIENTS_HEADER = "Não foi possível carregar a lista de ingredientes para o item";
        public static final String LOAD_INGREDIENTS_MESSAGE = "Tente novamente em alguns instantes. Se o erro persistir, verifique sua conexão.";
    }

    // Mensagens específicas para funcionários
    public static class Employees {
        public static final String LOAD_TITLE = "Erro ao Carregar Funcionários";
        public static final String LOAD_HEADER = "Não foi possível carregar a lista de funcionários";
        public static final String LOAD_MESSAGE = "Verifique sua conexão com a internet e tente novamente. Se o problema persistir, entre em contato com o suporte técnico.";

        public static final String LOAD_DEPARTMENTS_TITLE = "Erro ao Carregar Departamentos";
        public static final String LOAD_DEPARTMENTS_HEADER = "Não foi possível carregar a lista de departamentos";
        public static final String LOAD_DEPARTMENTS_MESSAGE = "Tente novamente em alguns instantes.";

        public static final String CREATE_TITLE = "Erro ao Criar Funcionário";
        public static final String CREATE_HEADER = "Não foi possível registrar o novo funcionário";
        public static final String CREATE_MESSAGE = "Verifique se todos os campos obrigatórios foram preenchidos corretamente e se o email não está em uso.";

        public static final String UPDATE_TITLE = "Erro ao Atualizar Funcionário";
        public static final String UPDATE_HEADER = "Não foi possível atualizar as informações do funcionário";
        public static final String UPDATE_MESSAGE = "Verifique se as informações estão corretas e tente novamente.";
    }

    // Mensagens específicas para pedidos
    public static class Orders {
        public static final String LOAD_TITLE = "Erro ao Carregar Pedidos";
        public static final String LOAD_HEADER = "Não foi possível carregar a lista de pedidos";
        public static final String LOAD_MESSAGE = "Verifique sua conexão com a internet e tente novamente. Se o problema persistir, entre em contato com o suporte técnico.";

        public static final String CREATE_TITLE = "Erro ao Criar Pedido";
        public static final String CREATE_HEADER = "Não foi possível criar o novo pedido";
        public static final String CREATE_MESSAGE = "Verifique se os itens selecionados estão disponíveis e a mesa está livre.";

        public static final String UPDATE_STATUS_TITLE = "Erro ao Atualizar Status";
        public static final String UPDATE_STATUS_HEADER = "Não foi possível atualizar o estado do pedido";
        public static final String UPDATE_STATUS_MESSAGE = "Tente novamente em alguns instantes. Se o erro persistir, verifique se o pedido ainda está ativo.";

        public static final String CANCEL_TITLE = "Erro ao Cancelar Pedido";
        public static final String CANCEL_HEADER = "Não foi possível cancelar o pedido";
        public static final String CANCEL_MESSAGE = "O pedido pode já estar em preparação. Verifique o status atual.";

        public static final String DELETE_TITLE = "Erro ao Excluir Pedido";
        public static final String DELETE_HEADER = "Não foi possível excluir o pedido";
        public static final String DELETE_MESSAGE = "Apenas pedidos cancelados podem ser excluídos do sistema.";
    }

    // Mensagens específicas para menus
    public static class Menus {
        public static final String LOAD_TITLE = "Erro ao Carregar Menus";
        public static final String LOAD_HEADER = "Não foi possível carregar a lista de menus";
        public static final String LOAD_MESSAGE = "Verifique sua conexão com a internet e tente novamente. Se o problema persistir, entre em contato com o suporte técnico.";

        public static final String CREATE_TITLE = "Erro ao Criar Menu";
        public static final String CREATE_HEADER = "Não foi possível criar o novo menu";
        public static final String CREATE_MESSAGE = "Verifique se todos os campos obrigatórios foram preenchidos e se os itens selecionados estão disponíveis.";

        public static final String UPDATE_TITLE = "Erro ao Atualizar Menu";
        public static final String UPDATE_HEADER = "Não foi possível atualizar as informações do menu";
        public static final String UPDATE_MESSAGE = "Verifique se as informações estão corretas e tente novamente.";

        public static final String DELETE_TITLE = "Erro ao Excluir Menu";
        public static final String DELETE_HEADER = "Não foi possível excluir o menu";
        public static final String DELETE_MESSAGE = "O menu pode estar sendo usado em pedidos ativos. Verifique as dependências antes de excluir.";

        public static final String ACTIVATE_TITLE = "Erro ao Ativar Menu";
        public static final String ACTIVATE_HEADER = "Não foi possível ativar o menu";
        public static final String ACTIVATE_MESSAGE = "Verifique se todos os itens do menu estão disponíveis.";

        public static final String DEACTIVATE_TITLE = "Erro ao Desativar Menu";
        public static final String DEACTIVATE_HEADER = "Não foi possível desativar o menu";
        public static final String DEACTIVATE_MESSAGE = "O menu pode estar sendo usado em pedidos ativos.";
    }

    // Mensagens específicas para mesas
    public static class Tables {
        public static final String LOAD_TITLE = "Erro ao Carregar Mesas";
        public static final String LOAD_HEADER = "Não foi possível carregar o status das mesas";
        public static final String LOAD_MESSAGE = "Verifique sua conexão com a internet e tente novamente.";

        public static final String CREATE_TITLE = "Erro ao Criar Mesa";
        public static final String CREATE_HEADER = "Não foi possível criar a nova mesa";
        public static final String CREATE_MESSAGE = "Verifique se o número da mesa não está em uso e se todos os campos foram preenchidos.";

        public static final String UPDATE_TITLE = "Erro ao Atualizar Mesa";
        public static final String UPDATE_HEADER = "Não foi possível atualizar as informações da mesa";
        public static final String UPDATE_MESSAGE = "Verifique se as informações estão corretas e tente novamente.";

        public static final String DELETE_TITLE = "Erro ao Excluir Mesa";
        public static final String DELETE_HEADER = "Não foi possível excluir a mesa";
        public static final String DELETE_MESSAGE = "A mesa pode estar ocupada ou ter pedidos ativos. Verifique antes de excluir.";

        public static final String UPDATE_STATUS_TITLE = "Erro ao Atualizar Mesa";
        public static final String UPDATE_STATUS_HEADER = "Não foi possível alterar o status da mesa";
        public static final String UPDATE_STATUS_MESSAGE = "Tente novamente em alguns instantes. Se o erro persistir, verifique se a mesa não está sendo usada por outro usuário.";

        public static final String OCCUPY_TITLE = "Erro ao Ocupar Mesa";
        public static final String OCCUPY_HEADER = "Não foi possível marcar a mesa como ocupada";
        public static final String OCCUPY_MESSAGE = "A mesa pode já estar ocupada por outro cliente.";

        public static final String FREE_TITLE = "Erro ao Liberar Mesa";
        public static final String FREE_HEADER = "Não foi possível liberar a mesa";
        public static final String FREE_MESSAGE = "Verifique se não há pedidos ativos para esta mesa.";
    }

    // Mensagens de validação
    public static class Validation {
        public static final String REQUIRED_FIELDS = "Por favor, preencha todos os campos obrigatórios";
        public static final String INVALID_EMAIL = "Por favor, insira um endereço de email válido";
        public static final String INVALID_NUMBER = "Por favor, insira um número válido";
        public static final String INVALID_DATE = "Por favor, insira uma data válida";
        public static final String PASSWORD_TOO_SHORT = "A senha deve ter pelo menos 6 caracteres";
        public static final String PASSWORDS_DONT_MATCH = "As senhas não coincidem";
        public static final String INVALID_PHONE = "Por favor, insira um número de telefone válido";
        public static final String INVALID_PRICE = "Por favor, insira um preço válido (maior que 0)";
        public static final String INVALID_QUANTITY = "Por favor, insira uma quantidade válida (maior que 0)";
        public static final String DUPLICATE_NAME = "Este nome já está em uso. Escolha um nome diferente";
        public static final String INVALID_TABLE_NUMBER = "Número da mesa deve ser maior que 0";
        public static final String INVALID_CAPACITY = "Capacidade da mesa deve ser maior que 0";
        public static final String NAME_TOO_SHORT = "O nome deve ter pelo menos 2 caracteres";
        public static final String NAME_TOO_LONG = "O nome não pode ter mais de 50 caracteres";
        public static final String DESCRIPTION_TOO_LONG = "A descrição não pode ter mais de 255 caracteres";
    }

    /**
     * Formata uma mensagem de erro com código de status HTTP
     */
    public static String formatHttpError(int statusCode) {
        switch (statusCode) {
            case 400:
                return "Dados inválidos enviados para o servidor (Código: 400)";
            case 401:
                return "Não autorizado. Faça login novamente (Código: 401)";
            case 403:
                return "Acesso negado. Você não tem permissão para esta operação (Código: 403)";
            case 404:
                return "Recurso não encontrado no servidor (Código: 404)";
            case 409:
                return "Conflito de dados. O recurso já existe ou está sendo usado (Código: 409)";
            case 500:
                return "Erro interno do servidor. Tente novamente mais tarde (Código: 500)";
            case 502:
                return "Servidor temporariamente indisponível (Código: 502)";
            case 503:
                return "Serviço temporariamente indisponível (Código: 503)";
            default:
                return "Erro inesperado do servidor (Código: " + statusCode + ")";
        }
    }

    /**
     * Formata uma mensagem de erro de exceção
     */
    public static String formatExceptionError(String exceptionMessage) {
        if (exceptionMessage == null || exceptionMessage.trim().isEmpty()) {
            return "Erro inesperado ocorreu. Tente novamente mais tarde.";
        }

        if (exceptionMessage.contains("ConnectException")) {
            return "Não foi possível conectar ao servidor. Verifique sua conexão com a internet.";
        }

        if (exceptionMessage.contains("SocketTimeoutException")) {
            return "Tempo limite de conexão excedido. Tente novamente.";
        }

        if (exceptionMessage.contains("UnknownHostException")) {
            return "Servidor não encontrado. Verifique sua conexão com a internet.";
        }

        return "Erro: " + exceptionMessage;
    }

    // Mensagens de sucesso centralizadas
    public static class SuccessMessages {

        // Mensagens de sucesso para ingredientes
        public static class Ingredients {
            public static final String ADD_TITLE = "Sucesso";
            public static final String ADD_HEADER = "Ingrediente Adicionado";
            public static final String ADD_MESSAGE = "O ingrediente foi adicionado com sucesso ao sistema.";

            public static final String UPDATE_TITLE = "Sucesso";
            public static final String UPDATE_HEADER = "Ingrediente Atualizado";
            public static final String UPDATE_MESSAGE = "As informações do ingrediente foram atualizadas com sucesso.";

            public static final String DELETE_TITLE = "Sucesso";
            public static final String DELETE_HEADER = "Ingrediente Excluído";
            public static final String DELETE_MESSAGE = "O ingrediente foi removido com sucesso do sistema.";

            public static final String STOCK_TITLE = "Sucesso";
            public static final String STOCK_HEADER = "Stock Atualizado";
            public static final String STOCK_MESSAGE = "A movimentação de stock foi registrada com sucesso.";
        }

        // Mensagens de sucesso para itens
        public static class Items {
            public static final String CREATE_TITLE = "Sucesso";
            public static final String CREATE_HEADER = "Item Criado";
            public static final String CREATE_MESSAGE = "O item foi criado com sucesso e está disponível no sistema.";

            public static final String UPDATE_TITLE = "Sucesso";
            public static final String UPDATE_HEADER = "Item Atualizado";
            public static final String UPDATE_MESSAGE = "As informações do item foram atualizadas com sucesso.";

            public static final String DELETE_TITLE = "Sucesso";
            public static final String DELETE_HEADER = "Item Excluído";
            public static final String DELETE_MESSAGE = "O item foi removido com sucesso do sistema.";
        }

        // Mensagens de sucesso para funcionários
        public static class Employees {
            public static final String CREATE_TITLE = "Sucesso";
            public static final String CREATE_HEADER = "Funcionário Cadastrado";
            public static final String CREATE_MESSAGE = "O funcionário foi registrado com sucesso no sistema.";

            public static final String UPDATE_TITLE = "Sucesso";
            public static final String UPDATE_HEADER = "Funcionário Atualizado";
            public static final String UPDATE_MESSAGE = "As informações do funcionário foram atualizadas com sucesso.";

            public static final String DELETE_TITLE = "Sucesso";
            public static final String DELETE_HEADER = "Funcionário Removido";
            public static final String DELETE_MESSAGE = "O funcionário foi removido com sucesso do sistema.";

            public static final String ACTIVATE_TITLE = "Sucesso";
            public static final String ACTIVATE_HEADER = "Funcionário Ativado";
            public static final String ACTIVATE_MESSAGE = "O funcionário foi ativado e pode acessar o sistema.";

            public static final String DEACTIVATE_TITLE = "Sucesso";
            public static final String DEACTIVATE_HEADER = "Funcionário Desativado";
            public static final String DEACTIVATE_MESSAGE = "O funcionário foi desativado e não pode mais acessar o sistema.";
        }

        // Mensagens de sucesso para pedidos
        public static class Orders {
            public static final String CREATE_TITLE = "Sucesso";
            public static final String CREATE_HEADER = "Pedido Criado";
            public static final String CREATE_MESSAGE = "O pedido foi criado com sucesso e enviado para a cozinha.";

            public static final String UPDATE_STATUS_TITLE = "Sucesso";
            public static final String UPDATE_STATUS_HEADER = "Status Atualizado";
            public static final String UPDATE_STATUS_MESSAGE = "O status do pedido foi atualizado com sucesso.";

            public static final String CANCEL_TITLE = "Sucesso";
            public static final String CANCEL_HEADER = "Pedido Cancelado";
            public static final String CANCEL_MESSAGE = "O pedido foi cancelado com sucesso.";

            public static final String COMPLETE_TITLE = "Sucesso";
            public static final String COMPLETE_HEADER = "Pedido Finalizado";
            public static final String COMPLETE_MESSAGE = "O pedido foi marcado como concluído com sucesso.";

            public static final String DELETE_TITLE = "Sucesso";
            public static final String DELETE_HEADER = "Pedido Excluído";
            public static final String DELETE_MESSAGE = "O pedido foi removido com sucesso do sistema.";
        }

        // Mensagens de sucesso para menus
        public static class Menus {
            public static final String CREATE_TITLE = "Sucesso";
            public static final String CREATE_HEADER = "Menu Criado";
            public static final String CREATE_MESSAGE = "O menu foi criado com sucesso e está disponível.";

            public static final String UPDATE_TITLE = "Sucesso";
            public static final String UPDATE_HEADER = "Menu Atualizado";
            public static final String UPDATE_MESSAGE = "As informações do menu foram atualizadas com sucesso.";

            public static final String DELETE_TITLE = "Sucesso";
            public static final String DELETE_HEADER = "Menu Excluído";
            public static final String DELETE_MESSAGE = "O menu foi removido com sucesso do sistema.";

            public static final String ACTIVATE_TITLE = "Sucesso";
            public static final String ACTIVATE_HEADER = "Menu Ativado";
            public static final String ACTIVATE_MESSAGE = "O menu foi ativado e está disponível para pedidos.";

            public static final String DEACTIVATE_TITLE = "Sucesso";
            public static final String DEACTIVATE_HEADER = "Menu Desativado";
            public static final String DEACTIVATE_MESSAGE = "O menu foi desativado e não está mais disponível para pedidos.";
        }

        // Mensagens de sucesso para mesas
        public static class Tables {
            public static final String CREATE_TITLE = "Sucesso";
            public static final String CREATE_HEADER = "Mesa Criada";
            public static final String CREATE_MESSAGE = "A mesa foi criada com sucesso e está disponível.";

            public static final String UPDATE_TITLE = "Sucesso";
            public static final String UPDATE_HEADER = "Mesa Atualizada";
            public static final String UPDATE_MESSAGE = "As informações da mesa foram atualizadas com sucesso.";

            public static final String DELETE_TITLE = "Sucesso";
            public static final String DELETE_HEADER = "Mesa Excluída";
            public static final String DELETE_MESSAGE = "A mesa foi removida com sucesso do sistema.";

            public static final String STATUS_UPDATE_TITLE = "Sucesso";
            public static final String STATUS_UPDATE_HEADER = "Status Atualizado";

            public static final String FREE_MESSAGE = "A mesa foi liberada com sucesso.";
            public static final String OCCUPIED_MESSAGE = "A mesa foi marcada como ocupada com sucesso.";
            public static final String RESERVED_MESSAGE = "A mesa foi reservada com sucesso.";
            public static final String CLEANING_MESSAGE = "A mesa foi marcada para limpeza com sucesso.";
            public static final String MAINTENANCE_MESSAGE = "A mesa foi marcada para manutenção com sucesso.";
        }

        // Mensagens de sucesso para login/autenticação
        public static class Authentication {
            public static final String LOGIN_TITLE = "Sucesso";
            public static final String LOGIN_HEADER = "Login Realizado";
            public static final String LOGIN_MESSAGE = "Bem-vindo! Login realizado com sucesso.";

            public static final String LOGOUT_TITLE = "Sucesso";
            public static final String LOGOUT_HEADER = "Logout Realizado";
            public static final String LOGOUT_MESSAGE = "Você foi desconectado com sucesso. Até logo!";

            public static final String PASSWORD_CHANGED_TITLE = "Sucesso";
            public static final String PASSWORD_CHANGED_HEADER = "Senha Alterada";
            public static final String PASSWORD_CHANGED_MESSAGE = "Sua senha foi alterada com sucesso.";
        }

        // Mensagens de sucesso para backup/configurações
        public static class System {
            public static final String BACKUP_TITLE = "Sucesso";
            public static final String BACKUP_HEADER = "Backup Criado";
            public static final String BACKUP_MESSAGE = "O backup dos dados foi criado com sucesso.";

            public static final String RESTORE_TITLE = "Sucesso";
            public static final String RESTORE_HEADER = "Dados Restaurados";
            public static final String RESTORE_MESSAGE = "Os dados foram restaurados com sucesso.";

            public static final String SETTINGS_TITLE = "Sucesso";
            public static final String SETTINGS_HEADER = "Configurações Salvas";
            public static final String SETTINGS_MESSAGE = "As configurações foram salvas com sucesso.";
        }
    }

    // Mensagens específicas para autenticação
    public static class Authentication {
        public static final String LOGIN_TITLE = "Erro de Login";
        public static final String LOGIN_HEADER = "Não foi possível realizar o login";
        public static final String LOGIN_MESSAGE = "Verifique suas credenciais e tente novamente.";

        public static final String INVALID_CREDENTIALS_TITLE = "Credenciais Inválidas";
        public static final String INVALID_CREDENTIALS_HEADER = "Email ou senha incorretos";
        public static final String INVALID_CREDENTIALS_MESSAGE = "Por favor, verifique seus dados e tente novamente.";

        public static final String SESSION_EXPIRED_TITLE = "Sessão Expirada";
        public static final String SESSION_EXPIRED_HEADER = "Sua sessão expirou";
        public static final String SESSION_EXPIRED_MESSAGE = "Por favor, faça login novamente para continuar.";

        public static final String ACCESS_DENIED_TITLE = "Acesso Negado";
        public static final String ACCESS_DENIED_HEADER = "Você não tem permissão";
        public static final String ACCESS_DENIED_MESSAGE = "Você não possui as permissões necessárias para esta operação.";

        public static final String PASSWORD_CHANGE_TITLE = "Erro ao Alterar Senha";
        public static final String PASSWORD_CHANGE_HEADER = "Não foi possível alterar a senha";
        public static final String PASSWORD_CHANGE_MESSAGE = "Verifique se a senha atual está correta e tente novamente.";
    }

    // Mensagens específicas para sistema
    public static class System {
        public static final String BACKUP_TITLE = "Erro no Backup";
        public static final String BACKUP_HEADER = "Não foi possível criar backup";
        public static final String BACKUP_MESSAGE = "Verifique o espaço em disco e as permissões de arquivo.";

        public static final String RESTORE_TITLE = "Erro na Restauração";
        public static final String RESTORE_HEADER = "Não foi possível restaurar os dados";
        public static final String RESTORE_MESSAGE = "O arquivo de backup pode estar corrompido ou incompatível.";

        public static final String SETTINGS_TITLE = "Erro nas Configurações";
        public static final String SETTINGS_HEADER = "Não foi possível salvar as configurações";
        public static final String SETTINGS_MESSAGE = "Verifique se os valores inseridos são válidos.";

        public static final String DATABASE_TITLE = "Erro de Base de Dados";
        public static final String DATABASE_HEADER = "Problema na base de dados";
        public static final String DATABASE_MESSAGE = "Não foi possível conectar à base de dados. Verifique a configuração.";
    }
}
