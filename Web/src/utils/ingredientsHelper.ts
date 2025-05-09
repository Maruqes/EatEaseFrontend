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

export const getIngredientName = (id: number): string => {
  return ingredientNames[id] || `Ingrediente ${id}`;
};

export const formatIngredient = (ingredienteId: number, quantidade: number): string => {
  const nome = getIngredientName(ingredienteId);
  
  const formatQuantidade = (qtd: number): string => {
    if (qtd < 1) {
      return `${qtd * 1000}ml`;
    }
    return qtd >= 1000 ? `${qtd / 1000}kg` : `${qtd}g`;
  };
  
  return `${nome} (${formatQuantidade(quantidade)})`;
};
