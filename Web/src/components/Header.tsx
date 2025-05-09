import React from 'react';

const Header: React.FC = () => {
  return (
    <header className="header">
      <nav className="navbar">
        <div className="logo">EatEase</div>
        <div className="nav-links">
          <a href="#inicio" className="nav-link">Início</a>
          <a href="#sobre" className="nav-link">Sobre</a>
          <a href="#menu" className="nav-link">Menu</a>
          <a href="#localizacao" className="nav-link">Localização</a>
          <a href="#contato" className="nav-link">Contacto</a>
        </div>
      </nav>
    </header>
  );
};

export default Header;
