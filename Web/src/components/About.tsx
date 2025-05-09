import React from 'react';

const About: React.FC = () => {
  return (
    <section id="sobre" className="section">
      <h2 className="section-title">Sobre Nós</h2>
      <div className="about-content">
        <div className="about-text">
          <p>
            O EatEase é um restaurante familiar fundado em 2020 com a missão de oferecer pratos deliciosos em um ambiente aconchegante.
            Nossa equipe é composta por chefs experientes e apaixonados pela culinária.
          </p>
          <p style={{ marginTop: '1rem' }}>
            Utilizamos apenas ingredientes frescos e de alta qualidade, muitos dos quais são adquiridos de produtores locais.
            Nossa especialidade é a fusão de sabores tradicionais com técnicas modernas de preparo.
          </p>
          <p style={{ marginTop: '1rem' }}>
            Seja para um almoço de negócios, um jantar romântico ou uma reunião familiar, 
            o EatEase oferece o ambiente ideal para todas as ocasiões.
          </p>
        </div>
        <div className="about-image"></div>
      </div>
    </section>
  );
};

export default About;
