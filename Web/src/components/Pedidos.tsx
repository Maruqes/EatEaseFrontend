import { useEffect } from 'react';
import { useParams } from 'react-router-dom';

// Sistema simples para armazenar a key globalmente
class KeyStorage {
    private static instance: KeyStorage;
    private key: string | null = null;

    static getInstance(): KeyStorage {
        if (!KeyStorage.instance) {
            KeyStorage.instance = new KeyStorage();
        }
        return KeyStorage.instance;
    }

    setKey(key: string): void {
        this.key = key;
        // Também armazena no localStorage para persistência
        localStorage.setItem('pedidos_key', key);
    }

    getKey(): string | null {
        // Tenta pegar do localStorage se não estiver em memória
        if (!this.key) {
            this.key = localStorage.getItem('pedidos_key');
        }
        return this.key;
    }

    clearKey(): void {
        this.key = null;
        localStorage.removeItem('pedidos_key');
    }
}

export const keyStorage = KeyStorage.getInstance();

function Pedidos() {
    const { key } = useParams<{ key: string }>();

    useEffect(() => {
        if (key) {
            keyStorage.setKey(key);
            console.log('Key armazenada:', key);
        }
    }, [key]);

    return (
        <div className="min-h-screen bg-gray-100 p-8">
            <div className="max-w-4xl mx-auto">
                <h1 className="text-3xl font-bold text-gray-800 mb-6">Pedidos</h1>
                <div className="bg-white rounded-lg shadow-md p-6">
                    <p className="text-gray-600">
                        Key recebida: <span className="font-mono bg-gray-100 px-2 py-1 rounded">{key}</span>
                    </p>
                    <p className="text-sm text-gray-500 mt-2">
                        Esta key foi armazenada e estará disponível para futuras requisições.
                    </p>
                </div>
            </div>
        </div>
    );
}

export default Pedidos;
