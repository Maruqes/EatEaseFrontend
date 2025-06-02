# Sistema de Mensagens da Aplicação EatEase

Este documento descreve como usar o sistema centralizado de mensagens de erro, sucesso e informação da aplicação EatEase.

## Estrutura do Sistema

O sistema de mensagens é composto por duas classes principais:

1. **ErrorMessages.java** - Centraliza todas as mensagens de texto
2. **PopUp.java** - Fornece métodos para exibir popups padronizados

## Categorias de Mensagens

### 1. Mensagens de Erro

#### Conectividade (Network)
```java
ErrorMessages.Network.CONNECTION_FAILED
ErrorMessages.Network.CONNECTION_TIMEOUT
ErrorMessages.Network.SERVER_UNAVAILABLE
ErrorMessages.Network.INTERNET_CONNECTION
```

#### Dados Gerais (Data)
```java
ErrorMessages.Data.LOADING_FAILED
ErrorMessages.Data.SAVING_FAILED
ErrorMessages.Data.DELETING_FAILED
ErrorMessages.Data.UPDATING_FAILED
ErrorMessages.Data.INVALID_DATA
ErrorMessages.Data.DATA_NOT_FOUND
```

#### Ingredientes (Ingredients)
```java
ErrorMessages.Ingredients.LOAD_TITLE/HEADER/MESSAGE
ErrorMessages.Ingredients.ADD_TITLE/HEADER/MESSAGE
ErrorMessages.Ingredients.UPDATE_TITLE/HEADER/MESSAGE
ErrorMessages.Ingredients.DELETE_TITLE/HEADER/MESSAGE
ErrorMessages.Ingredients.STOCK_TITLE/HEADER/MESSAGE
```

#### Itens (Items)
```java
ErrorMessages.Items.LOAD_TITLE/HEADER/MESSAGE
ErrorMessages.Items.CREATE_TITLE/HEADER/MESSAGE
ErrorMessages.Items.UPDATE_TITLE/HEADER/MESSAGE
ErrorMessages.Items.DELETE_TITLE/HEADER/MESSAGE
ErrorMessages.Items.LOAD_INGREDIENTS_TITLE/HEADER/MESSAGE
```

#### Funcionários (Employees)
```java
ErrorMessages.Employees.LOAD_TITLE/HEADER/MESSAGE
ErrorMessages.Employees.CREATE_TITLE/HEADER/MESSAGE
ErrorMessages.Employees.UPDATE_TITLE/HEADER/MESSAGE
ErrorMessages.Employees.LOAD_DEPARTMENTS_TITLE/HEADER/MESSAGE
```

#### Pedidos (Orders)
```java
ErrorMessages.Orders.LOAD_TITLE/HEADER/MESSAGE
ErrorMessages.Orders.CREATE_TITLE/HEADER/MESSAGE
ErrorMessages.Orders.UPDATE_STATUS_TITLE/HEADER/MESSAGE
ErrorMessages.Orders.CANCEL_TITLE/HEADER/MESSAGE
ErrorMessages.Orders.DELETE_TITLE/HEADER/MESSAGE
```

#### Menus (Menus)
```java
ErrorMessages.Menus.LOAD_TITLE/HEADER/MESSAGE
ErrorMessages.Menus.CREATE_TITLE/HEADER/MESSAGE
ErrorMessages.Menus.UPDATE_TITLE/HEADER/MESSAGE
ErrorMessages.Menus.DELETE_TITLE/HEADER/MESSAGE
ErrorMessages.Menus.ACTIVATE_TITLE/HEADER/MESSAGE
ErrorMessages.Menus.DEACTIVATE_TITLE/HEADER/MESSAGE
```

#### Mesas (Tables)
```java
ErrorMessages.Tables.LOAD_TITLE/HEADER/MESSAGE
ErrorMessages.Tables.CREATE_TITLE/HEADER/MESSAGE
ErrorMessages.Tables.UPDATE_TITLE/HEADER/MESSAGE
ErrorMessages.Tables.DELETE_TITLE/HEADER/MESSAGE
ErrorMessages.Tables.UPDATE_STATUS_TITLE/HEADER/MESSAGE
ErrorMessages.Tables.OCCUPY_TITLE/HEADER/MESSAGE
ErrorMessages.Tables.FREE_TITLE/HEADER/MESSAGE
```

#### Autenticação (Authentication)
```java
ErrorMessages.Authentication.LOGIN_TITLE/HEADER/MESSAGE
ErrorMessages.Authentication.INVALID_CREDENTIALS_TITLE/HEADER/MESSAGE
ErrorMessages.Authentication.SESSION_EXPIRED_TITLE/HEADER/MESSAGE
ErrorMessages.Authentication.ACCESS_DENIED_TITLE/HEADER/MESSAGE
ErrorMessages.Authentication.PASSWORD_CHANGE_TITLE/HEADER/MESSAGE
```

#### Sistema (System)
```java
ErrorMessages.System.BACKUP_TITLE/HEADER/MESSAGE
ErrorMessages.System.RESTORE_TITLE/HEADER/MESSAGE
ErrorMessages.System.SETTINGS_TITLE/HEADER/MESSAGE
ErrorMessages.System.DATABASE_TITLE/HEADER/MESSAGE
```

#### Validação (Validation)
```java
ErrorMessages.Validation.REQUIRED_FIELDS
ErrorMessages.Validation.INVALID_EMAIL
ErrorMessages.Validation.INVALID_NUMBER
ErrorMessages.Validation.INVALID_DATE
ErrorMessages.Validation.PASSWORD_TOO_SHORT
ErrorMessages.Validation.PASSWORDS_DONT_MATCH
ErrorMessages.Validation.INVALID_PHONE
ErrorMessages.Validation.INVALID_PRICE
ErrorMessages.Validation.INVALID_QUANTITY
ErrorMessages.Validation.DUPLICATE_NAME
ErrorMessages.Validation.INVALID_TABLE_NUMBER
ErrorMessages.Validation.INVALID_CAPACITY
ErrorMessages.Validation.NAME_TOO_SHORT
ErrorMessages.Validation.NAME_TOO_LONG
ErrorMessages.Validation.DESCRIPTION_TOO_LONG
```

### 2. Mensagens de Sucesso

Todas as mensagens de sucesso seguem a mesma estrutura das mensagens de erro, mas estão na classe `ErrorMessages.SuccessMessages`:

```java
ErrorMessages.SuccessMessages.Ingredients.*
ErrorMessages.SuccessMessages.Items.*
ErrorMessages.SuccessMessages.Employees.*
ErrorMessages.SuccessMessages.Orders.*
ErrorMessages.SuccessMessages.Menus.*
ErrorMessages.SuccessMessages.Tables.*
ErrorMessages.SuccessMessages.Authentication.*
ErrorMessages.SuccessMessages.System.*
```

## Como Usar os Popups

### Métodos de Popup Predefinidos

#### Para Ingredientes:
```java
// Erro
PopUp.showIngredientLoadError();
PopUp.showIngredientLoadError(statusCode);
PopUp.showIngredientLoadError(exception);
PopUp.showIngredientAddError(statusCode);
PopUp.showIngredientAddError(exception);
PopUp.showIngredientUpdateError(statusCode);
PopUp.showIngredientUpdateError(exception);
PopUp.showIngredientDeleteError(statusCode);
PopUp.showIngredientDeleteError(exception);
PopUp.showIngredientStockError(statusCode);
PopUp.showIngredientStockError(exception);

// Sucesso
PopUp.showIngredientAddSuccess();
PopUp.showIngredientUpdateSuccess();
PopUp.showIngredientDeleteSuccess();
PopUp.showIngredientStockSuccess();
```

#### Para Itens:
```java
// Erro
PopUp.showItemLoadError();
PopUp.showItemLoadError(statusCode);
PopUp.showItemLoadError(exception);
PopUp.showItemCreateError(statusCode);
PopUp.showItemCreateError(exception);
PopUp.showItemUpdateError(statusCode);
PopUp.showItemUpdateError(exception);
PopUp.showItemDeleteError(statusCode);
PopUp.showItemDeleteError(exception);
PopUp.showItemIngredientsLoadError(statusCode);
PopUp.showItemIngredientsLoadError(exception);

// Sucesso
PopUp.showItemCreateSuccess();
PopUp.showItemUpdateSuccess();
PopUp.showItemDeleteSuccess();
```

#### Para Funcionários:
```java
// Erro
PopUp.showEmployeeLoadError();
PopUp.showEmployeeLoadError(statusCode);
PopUp.showEmployeeLoadError(exception);
PopUp.showEmployeeCreateError(statusCode);
PopUp.showEmployeeCreateError(exception);
PopUp.showEmployeeUpdateError(statusCode);
PopUp.showEmployeeUpdateError(exception);
PopUp.showEmployeeDeleteError(statusCode);
PopUp.showEmployeeDeleteError(exception);

// Sucesso
PopUp.showEmployeeCreateSuccess();
PopUp.showEmployeeUpdateSuccess();
PopUp.showEmployeeDeleteSuccess();
```

#### Para Pedidos:
```java
// Erro
PopUp.showOrderLoadError();
PopUp.showOrderLoadError(statusCode);
PopUp.showOrderLoadError(exception);
PopUp.showOrderCreateError(statusCode);
PopUp.showOrderCreateError(exception);
PopUp.showOrderStatusUpdateError(statusCode);
PopUp.showOrderStatusUpdateError(exception);
PopUp.showOrderCancelError(statusCode);
PopUp.showOrderCancelError(exception);
PopUp.showOrderDeleteError(statusCode);
PopUp.showOrderDeleteError(exception);

// Sucesso
PopUp.showOrderCreateSuccess();
PopUp.showOrderStatusUpdateSuccess();
PopUp.showOrderCancelSuccess();
PopUp.showOrderCompleteSuccess();
PopUp.showOrderDeleteSuccess();
```

#### Para Menus:
```java
// Erro
PopUp.showMenuLoadError();
PopUp.showMenuLoadError(statusCode);
PopUp.showMenuLoadError(exception);
PopUp.showMenuCreateError(statusCode);
PopUp.showMenuCreateError(exception);
PopUp.showMenuUpdateError(statusCode);
PopUp.showMenuUpdateError(exception);
PopUp.showMenuDeleteError(statusCode);
PopUp.showMenuDeleteError(exception);

// Sucesso
PopUp.showMenuCreateSuccess();
PopUp.showMenuUpdateSuccess();
PopUp.showMenuDeleteSuccess();
PopUp.showMenuActivateSuccess();
PopUp.showMenuDeactivateSuccess();
```

#### Para Mesas:
```java
// Erro
PopUp.showTableLoadError();
PopUp.showTableLoadError(statusCode);
PopUp.showTableLoadError(exception);
PopUp.showTableCreateError(statusCode);
PopUp.showTableCreateError(exception);
PopUp.showTableUpdateError(statusCode);
PopUp.showTableUpdateError(exception);
PopUp.showTableDeleteError(statusCode);
PopUp.showTableDeleteError(exception);
PopUp.showTableStatusUpdateError(statusCode);
PopUp.showTableStatusUpdateError(exception);

// Sucesso
PopUp.showTableCreateSuccess();
PopUp.showTableUpdateSuccess();
PopUp.showTableDeleteSuccess();
PopUp.showTableFreeSuccess();
PopUp.showTableOccupiedSuccess();
PopUp.showTableReservedSuccess();
PopUp.showTableCleaningSuccess();
PopUp.showTableMaintenanceSuccess();
```

#### Para Autenticação:
```java
// Erro
PopUp.showLoginError();
PopUp.showInvalidCredentialsError();
PopUp.showSessionExpiredError();
PopUp.showAccessDeniedError();

// Sucesso
PopUp.showLoginSuccess();
PopUp.showLogoutSuccess();
PopUp.showPasswordChangeSuccess();
```

#### Para Validação:
```java
PopUp.showRequiredFieldsError();
PopUp.showInvalidEmailError();
PopUp.showInvalidNumberError();
PopUp.showInvalidPriceError();
PopUp.showInvalidQuantityError();
PopUp.showDuplicateNameError();
PopUp.showInvalidTableNumberError();
```

#### Para Sistema:
```java
// Erro
PopUp.showDatabaseConnectionError();
PopUp.showBackupError(statusCode);
PopUp.showBackupError(exception);

// Sucesso
PopUp.showBackupSuccess();
```

### Métodos Genéricos

Para casos não cobertos pelos métodos predefinidos:

```java
// Popup simples
PopUp.showPopupDialog(Alert.AlertType.ERROR, "Título", "Cabeçalho", "Mensagem");

// Popup de confirmação com callback
PopUp.showConfirmationPopup(Alert.AlertType.WARNING, "Título", "Cabeçalho", "Mensagem", () -> {
    // Ação a executar se o usuário confirmar
});

// Popup com formatação automática de erro HTTP
PopUp.showHttpErrorPopup("Título", "Cabeçalho", statusCode);

// Popup com formatação automática de exceção
PopUp.showExceptionErrorPopup("Título", "Cabeçalho", exception);
```

## Exemplos de Uso

### Carregar Lista de Ingredientes
```java
try {
    // Código para carregar ingredientes
    List<Ingredient> ingredients = loadIngredients();
    // Sucesso - não mostrar popup, apenas atualizar UI
} catch (HttpException e) {
    PopUp.showIngredientLoadError(e.getStatusCode());
} catch (Exception e) {
    PopUp.showIngredientLoadError(e.getMessage());
}
```

### Adicionar Novo Item
```java
try {
    // Código para adicionar item
    addItem(newItem);
    PopUp.showItemCreateSuccess();
} catch (ValidationException e) {
    PopUp.showRequiredFieldsError();
} catch (HttpException e) {
    PopUp.showItemCreateError(e.getStatusCode());
} catch (Exception e) {
    PopUp.showItemCreateError(e.getMessage());
}
```

### Excluir Mesa com Confirmação
```java
PopUp.showConfirmationPopup(
    Alert.AlertType.WARNING,
    "Confirmar Exclusão",
    "Excluir Mesa",
    "Tem certeza que deseja excluir esta mesa?",
    () -> {
        try {
            deleteTable(tableId);
            PopUp.showTableDeleteSuccess();
        } catch (HttpException e) {
            PopUp.showTableDeleteError(e.getStatusCode());
        } catch (Exception e) {
            PopUp.showTableDeleteError(e.getMessage());
        }
    }
);
```

## Benefícios do Sistema

1. **Consistência**: Todas as mensagens seguem o mesmo padrão
2. **Facilidade de Manutenção**: Alterações centralizadas
3. **Internacionalização**: Fácil de traduzir no futuro
4. **Reutilização**: Métodos predefinidos para operações comuns
5. **Flexibilidade**: Métodos genéricos para casos especiais

## Formatação Automática

O sistema inclui formatação automática para:

- **Códigos HTTP**: Converte códigos como 404, 500, etc. em mensagens amigáveis
- **Exceções**: Converte exceções técnicas em mensagens para usuário final

```java
ErrorMessages.formatHttpError(404); // "Recurso não encontrado no servidor (Código: 404)"
ErrorMessages.formatExceptionError("ConnectException"); // "Não foi possível conectar ao servidor..."
```
