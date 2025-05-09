import React from 'react';

const Footer: React.FC = () => {
  const currentYear = new Date().getFullYear();
  
  return (
    <footer className="footer">
      <div className="footer-content">
        <div className="footer-section">
          <h3>EatEase</h3>
          <p>Deliciosas refeições preparadas com ingredientes frescos e muito carinho</p>
        </div>
        <div className="footer-section">
          <h3>Links Rápidos</h3>
          <p><a href="#inicio" className="nav-link">Início</a></p>
          <p><a href="#sobre" className="nav-link">Sobre</a></p>
          <p><a href="#menu" className="nav-link">Menu</a></p>
          <p><a href="#localizacao" className="nav-link">Localização</a></p>
          <p><a href="#contato" className="nav-link">Contacto</a></p>
        </div>
        <div className="footer-section">
          <h3>Contacto</h3>
          <p>Telefone: (351) 912-345-678</p>
          <p>Email: contacto@eatease.pt</p>
        </div>
      </div>
      <div className="footer-bottom">
        <p>&copy; {currentYear} EatEase - Todos os direitos reservados</p>
      </div>
    </footer>
  );
};

export default Footer;
