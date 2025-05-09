import React from 'react';

const Hero: React.FC = () => {
  return (
    <section id="inicio" className="hero">
      <h1>Bem-vindo ao EatEase</h1>
      <p>Deliciosas refeições preparadas com ingredientes frescos e muito carinho</p>
      <a href="#menu" className="btn">Ver Menu</a>
    </section>
  );
};

export default Hero;
