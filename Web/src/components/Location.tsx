import React from 'react';

const Location: React.FC = () => {
  return (
    <section id="localizacao" className="section location">
      <h2 className="section-title">Nossa Localização</h2>
      <div className="location-content">
        <div className="location-info">
          <h3>Encontre-nos</h3>
          <p style={{ marginTop: '1rem' }}>
            <strong>Morada:</strong><br />
            <b>Escola Superior de Tecnologia e Gestão - Instituto Politécnico de Viana do Castelo</b><br />
            Av. do Atlântico 644<br />
            4900, Viana do Castelo<br />
          </p>
          <p style={{ marginTop: '1rem' }}>
            <strong>Horário de Funcionamento:</strong><br />
            Segunda a Sexta: 11h às 23h<br />
            Sábados e Domingos: 11h às 00h
          </p>
          <p style={{ marginTop: '1rem' }}>
            <strong>Contacto:</strong><br />
            Telefone: (351) 912-345-678<br />
            Email: contacto@eatease.pt
          </p>
        </div>
        <div className="location-map">
          <iframe 
            src="https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d2977.7548353004283!2d-8.84705387611121!3d41.694866530564714!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0xd25c4b1f33844c3%3A0x800ebc7516f1cdaa!2sEscola%20Superior%20de%20Tecnologia%20e%20Gest%C3%A3o!5e0!3m2!1spt-PT!2spt!4v1714427158520!5m2!1spt-PT!2spt&iwloc=B"
            width="100%" 
            height="400" 
            style={{ border: 0, borderRadius: '10px' }} 
            allowFullScreen 
            loading="lazy"
            title="Localização do Restaurante"
          ></iframe>
        </div>
      </div>
    </section>
  );
};

export default Location;
