import React, { useEffect, useState } from 'react';
import dishPlaceholder from '../assets/dishes/placeholder.svg';
import { formatIngredient, fetchAllIngredients } from '../utils/ingredientsHelper';

interface ApiItem {
  id: number;
  nome: string;
  tipoPrato_id: number;
  preco: number;
  ingredientesJson: string;
  eCpmposto: boolean | number;
  stockAtual: number;
}

interface IngredientItem {
  quantidade: number;
  ingredienteId: number;
}

interface Dish {
  id: number;
  name: string;
  price: number;
  ingredients: string[];
  image: string;
  type: 'Prato Principal' | 'Entradas' | 'Bebida' | 'Sobremesa';
}

interface ItemPhoto {
  foto: string;
}

const Menu: React.FC = () => {
  const [dishes, setDishes] = useState<Dish[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [dishTypes] = useState<string[]>(['Prato Principal', 'Entradas', 'Bebida', 'Sobremesa']);

  // Função para buscar a foto de um item
  const fetchItemPhoto = async (itemId: number): Promise<string> => {
    try {
      const response = await fetch(`/api/item/getFoto/${itemId}`, {
        method: 'GET',
        headers: {
          'Accept': 'application/json',
          'Content-Type': 'application/json',
        }
      });

      if (response.ok) {
        const data: ItemPhoto = await response.json();
        if (data.foto) {
          return `/api/uploads/items/${data.foto}`;
        }
      }
    } catch (error) {
      console.error(`Erro ao buscar foto do item ${itemId}:`, error);
    }

    // Retorna placeholder se não conseguir buscar a foto
    return dishPlaceholder;
  };

  useEffect(() => {
    const fetchMenu = async () => {
      try {
        setLoading(true);

        // Carregar todos os ingredientes uma única vez no início
        console.log('Carregando ingredientes...');
        await fetchAllIngredients();
        console.log('Ingredientes carregados com sucesso!');

        const apiUrl = 'api/item/getAll';

        const response = await fetch(apiUrl, {
          method: 'GET',
          headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
          }
        });

        if (response.type === 'opaque') {
          throw new Error('Resposta opaca devido ao modo no-cors. Não foi possível aceder aos dados.');
        }

        if (!response.ok) {
          throw new Error(`Erro ao carregar menu: ${response.status}`);
        }

        const data: ApiItem[] = await response.json();
        console.log('Dados recebidos da API:', data);

        // Verificar se há dados antes de continuar
        if (!data || data.length === 0) {
          throw new Error('Não foram encontrados pratos no menu.');
        }

        console.log('Valores de eCpmposto:', data.map(item => ({ id: item.id, nome: item.nome, eCpmposto: item.eCpmposto })));

        let filteredData = data.filter(item => item.eCpmposto === true || item.eCpmposto === 1);

        // Mapeamento dos IDs de tipo para os tipos de prato
        const tiposPrato: Record<number, 'Prato Principal' | 'Entradas' | 'Bebida' | 'Sobremesa'> = {
          1: 'Entradas',
          2: 'Prato Principal',
          3: 'Sobremesa',
          4: 'Bebida'
        };

        console.log('Dados filtrados:', filteredData);
        if (filteredData.length === 0) {
          console.warn('Nenhum prato composto encontrado. A utilizar todos os pratos.');
          filteredData = data;
        }

        const formattedDishes: Dish[] = await Promise.all(filteredData.map(async item => {
          let ingredientsList: string[] = [];
          try {
            const ingredientsData = JSON.parse(item.ingredientesJson);
            // Usar Promise.all para processar todos os ingredientes de forma assíncrona
            ingredientsList = await Promise.all(
              ingredientsData.map(async (ing: IngredientItem) =>
                await formatIngredient(ing.ingredienteId, ing.quantidade)
              )
            );
          } catch (e) {
            console.error('Erro ao processar ingredientes:', e);
            ingredientsList = ['Ingredientes indisponíveis'];
          }

          // Buscar a foto do item
          const imageUrl = await fetchItemPhoto(item.id);

          return {
            id: item.id,
            name: item.nome,
            price: item.preco,
            ingredients: ingredientsList,
            image: imageUrl,
            type: tiposPrato[item.tipoPrato_id] || 'Prato Principal'
          };
        }));

        console.log('Pratos formatados a serem exibidos:', formattedDishes);
        setDishes(formattedDishes);
        setError(null);
      } catch (err) {
        console.error("Erro ao buscar dados do menu:", err);
        setError("Não foi possível carregar o menu. Tente novamente mais tarde.");

        // Dados de fallback para teste em caso de erro na API
        setDishes([
          {
            id: 1,
            name: 'Risoto de Cogumelos',
            price: 22.50,
            ingredients: ['Arroz arbóreo', 'Cogumelos variados', 'Vinho branco', 'Queijo parmesão'],
            image: dishPlaceholder,
            type: 'Prato Principal'
          },
          {
            id: 2,
            name: 'Filé Mignon ao Molho Madeira',
            price: 32.00,
            ingredients: ['Filé mignon', 'Vinho madeira', 'Cogumelos', 'Batatas rústicas'],
            image: dishPlaceholder,
            type: 'Prato Principal'
          },
          {
            id: 3,
            name: 'Salmão Grelhado com Legumes',
            price: 27.50,
            ingredients: ['Filé de salmão', 'Aspargos', 'Cenoura', 'Azeite de ervas'],
            image: dishPlaceholder,
            type: 'Prato Principal'
          },
          // Entradas
          {
            id: 4,
            name: 'Bruschettas de Tomate e Manjericão',
            price: 9.50,
            ingredients: ['Pão italiano', 'Tomate', 'Manjericão fresco', 'Azeite extra virgem'],
            image: dishPlaceholder,
            type: 'Entradas'
          },
          {
            id: 5,
            name: 'Carpaccio de Carne',
            price: 12.00,
            ingredients: ['Carne bovina', 'Rúcula', 'Queijo parmesão', 'Molho especial'],
            image: dishPlaceholder,
            type: 'Entradas'
          },
          // Bebidas
          {
            id: 6,
            name: 'Vinho Tinto Reserva',
            price: 18.00,
            ingredients: ['Uvas selecionadas', 'Safra especial'],
            image: dishPlaceholder,
            type: 'Bebida'
          },
          {
            id: 7,
            name: 'Caipirinha de Frutas',
            price: 8.50,
            ingredients: ['Cachaça artesanal', 'Frutas da estação', 'Açúcar'],
            image: dishPlaceholder,
            type: 'Bebida'
          },
          // Sobremesas
          {
            id: 8,
            name: 'Tiramisu Tradicional',
            price: 10.50,
            ingredients: ['Biscoito champagne', 'Café', 'Queijo mascarpone', 'Cacau em pó'],
            image: dishPlaceholder,
            type: 'Sobremesa'
          },
          {
            id: 9,
            name: 'Petit Gateau com Sorvete',
            price: 11.50,
            ingredients: ['Chocolate belga', 'Sorvete de baunilha', 'Calda de frutas vermelhas'],
            image: dishPlaceholder,
            type: 'Sobremesa'
          }
        ]);
      } finally {
        setLoading(false);
      }
    };

    fetchMenu();
  }, []); // Array vazio garante que a função seja executada apenas uma vez

  if (loading) {
    return (
      <section id="menu" className="section">
        <h2 className="section-title">Nosso Menu</h2>
        <div className="loading-container">
          <p>Carregando menu...</p>
        </div>
      </section>
    );
  }

  if (error) {
    return (
      <section id="menu" className="section">
        <h2 className="section-title">Nosso Menu</h2>
        <div className="error-container">
          <p className="error-message">{error}</p>
          {dishes.length > 0 && <p>Exibindo menu offline.</p>}
        </div>
        {dishes.length > 0 && renderMenuContent()}
      </section>
    );
  }

  return (
    <section id="menu" className="section">
      <h2 className="section-title">Nosso Menu</h2>
      {renderMenuContent()}
    </section>
  );

  function renderMenuContent() {
    return (
      <>
        {dishTypes.map(type => {
          // Filtra os pratos por tipo para verificar se essa categoria tem pratos
          const dishesOfType = dishes.filter(dish => dish.type === type);
          const hasDishes = dishesOfType.length > 0;

          return (
            <div key={type} className="menu-category">
              <h3 className="category-title">{type}</h3>

              {hasDishes ? (
                // Renderiza grid de pratos se houver pratos nessa categoria
                <div className="menu-grid">
                  {dishesOfType.map((dish) => (
                    <div key={dish.id} className="menu-item">
                      <img src={dish.image} alt={dish.name} className="menu-image" />
                      <div className="menu-content">
                        <h3 className="menu-title">{dish.name}</h3>
                        <p className="menu-price">{dish.price.toFixed(2)} €</p>
                        <p className="menu-ingredients">{dish.ingredients.join(', ')}</p>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                // Mostra mensagem quando não há pratos nessa categoria
                <div className="no-dishes-message">
                  <p>Não há pratos disponíveis nesta categoria de momento.</p>
                </div>
              )}
            </div>
          );
        })}
      </>
    );
  }
};

export default Menu;
