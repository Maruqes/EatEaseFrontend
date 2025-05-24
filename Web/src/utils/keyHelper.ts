import { keyStorage } from '../components/Pedidos';

// Hook personalizado para acessar a key armazenada
export const useStoredKey = () => {
    const getKey = () => keyStorage.getKey();
    const setKey = (key: string) => keyStorage.setKey(key);
    const clearKey = () => keyStorage.clearKey();

    return {
        getKey,
        setKey,
        clearKey
    };
};

// Função utilitária para fazer requisições com a key armazenada
export const makeRequestWithKey = async (url: string, options: RequestInit = {}) => {
    const key = keyStorage.getKey();

    if (!key) {
        throw new Error('Nenhuma key encontrada. Acesse /pedidos/:key primeiro.');
    }

    const headers = {
        ...options.headers,
        'Authorization': `Bearer ${key}`,
        // ou qualquer outro header que você precise
        'X-API-Key': key
    };

    return fetch(url, {
        ...options,
        headers
    });
};
