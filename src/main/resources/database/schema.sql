DROP TABLE IF EXISTS CONTINENTS;

/*Tabella che contiene i continenti*/
CREATE TABLE CONTINENTS (
  id VARCHAR(250),
  name VARCHAR(250) NOT NULL,
  bonus_armies INT NOT NULL,
  PRIMARY KEY (id)
);

DROP TABLE IF EXISTS TERRITORIES;

/*Tabella che contiene tutte le nazioni del gioco*/
CREATE TABLE TERRITORIES (
  id VARCHAR(250),
  name VARCHAR(250) NOT NULL,
  continent VARCHAR(250) NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (continent) REFERENCES CONTINENTS(id)
);

DROP TABLE IF EXISTS NEIGHBORING;

/*Tabella che associa ad ogni nazione le nazioni confinanti*/
CREATE TABLE NEIGHBORING (
  id VARCHAR(250) NOT NULL,
  neighboring VARCHAR(250) NOT NULL ,
  FOREIGN KEY (id) REFERENCES TERRITORIES(id),
  FOREIGN KEY (neighboring) REFERENCES TERRITORIES(id)
);