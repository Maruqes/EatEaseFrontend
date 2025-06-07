// Interface para os dados da API de ingredientes
interface ApiIngredient {
  id: number;
  nome: string;
  stock: number;
  stock_min: number;
  unidade_id: number;
}

// Cache de ingredientes para evitar múltiplas requisições
let ingredientsCache: Record<number, ApiIngredient> = {};
let cacheLoaded = false;

// Mapeamento dos tipos de unidade
const unidadeNames: Record<number, string> = {
  1: 'kg',    // quilos
  2: 'g',     // gramas 
  3: 'l',     // litros
  4: 'ml',    // mililitros
  5: 'unid',  // unidades
  6: 'doses', // doses
  7: 'caixas' // caixas
};

// Função para buscar todos os ingredientes da API
export const fetchAllIngredients = async (): Promise<Record<number, ApiIngredient>> => {
  if (cacheLoaded) {
    console.log('Usando ingredientes do cache (já carregados)');
    return ingredientsCache;
  }

  console.log('Fazendo requisição à API /api/ingredientes/getAll...');
  try {
    const response = await fetch('/api/ingredientes/getAll', {
      method: 'GET',
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json',
      }
    });

    if (response.ok) {
      const ingredients: ApiIngredient[] = await response.json();

      // Converter array para objeto indexado por ID
      ingredientsCache = ingredients.reduce((acc, ingredient) => {
        acc[ingredient.id] = ingredient;
        return acc;
      }, {} as Record<number, ApiIngredient>);

      cacheLoaded = true;
      console.log(`Ingredientes carregados da API: ${ingredients.length} ingredientes em cache`);
      return ingredientsCache;
    } else {
      console.error('Erro ao buscar ingredientes:', response.status);
    }
  } catch (error) {
    console.error('Erro ao buscar ingredientes:', error);
  }

  // Retorna cache vazio se houver erro
  return ingredientsCache;
};

// Fallback para ingredientes estáticos (caso a API falhe)
export const ingredientNames: Record<number, string> = {
  1: 'Arroz',
  2: 'Feijão',
  3: 'Batata',
  4: 'Carne bovina',
  5: 'Frango',
  6: 'Peixe',
  7: 'Tomate',
  8: 'Cebola',
  9: 'Alho',
  10: 'Alface',
  11: 'Cenoura',
  12: 'Brócolos',
  13: 'Queijo',
  14: 'Leite',
  15: 'Ovos',
  16: 'Farinha',
  17: 'Sal',
  18: 'Açúcar',
  19: 'Azeite',
  20: 'Manteiga'
};

export const getIngredientName = async (id: number): Promise<string> => {
  // Primeiro tenta usar o cache se já estiver carregado
  if (cacheLoaded && ingredientsCache[id]) {
    return ingredientsCache[id].nome;
  }

  // Se não estiver no cache, carrega todos os ingredientes
  const ingredients = await fetchAllIngredients();
  return ingredients[id]?.nome || ingredientNames[id] || `Ingrediente ${id}`;
};

export const getUnidadeName = (unidade_id: number): string => {
  return unidadeNames[unidade_id] || 'unid';
};

// Função para verificar o status do cache
export const isCacheLoaded = (): boolean => {
  return cacheLoaded;
};

// Função para limpar o cache (útil para testes ou recarregamento)
export const clearCache = (): void => {
  ingredientsCache = {};
  cacheLoaded = false;
  console.log('Cache de ingredientes limpo');
};

export const formatIngredient = async (ingredienteId: number, quantidade: number): Promise<string> => {
  // Usa o cache se já estiver carregado
  let ingredient: ApiIngredient | undefined;

  if (cacheLoaded && ingredientsCache[ingredienteId]) {
    ingredient = ingredientsCache[ingredienteId];
  } else {
    // Carrega todos se não estiver no cache
    const ingredients = await fetchAllIngredients();
    ingredient = ingredients[ingredienteId];
  }

  let nome: string;
  let unidade: string;

  if (ingredient) {
    nome = ingredient.nome;
    unidade = getUnidadeName(ingredient.unidade_id);
  } else {
    // Fallback para nomes estáticos
    nome = ingredientNames[ingredienteId] || `Ingrediente ${ingredienteId}`;
    unidade = 'g'; // unidade padrão para fallback
  }

  const formatQuantidade = (qtd: number, unit: string): string => {
    switch (unit) {
      case 'kg':
        return qtd >= 1000 ? `${qtd / 1000}kg` : `${qtd}g`;
      case 'g':
        return qtd >= 1000 ? `${qtd / 1000}kg` : `${qtd}g`;
      case 'l':
        return qtd >= 1000 ? `${qtd / 1000}l` : `${qtd}ml`;
      case 'ml':
        return qtd >= 1000 ? `${qtd / 1000}l` : `${qtd}ml`;
      case 'unid':
      case 'doses':
      case 'caixas':
        return `${qtd} ${unit}`;
      default:
        return `${qtd} ${unit}`;
    }
  };

  return `${nome} (${formatQuantidade(quantidade, unidade)})`;
};
