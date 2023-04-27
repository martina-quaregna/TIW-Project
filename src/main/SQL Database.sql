CREATE TABLE ARTICOLO (
    Codice int PRIMARY KEY ,
    Nome varchar(45) not null ,
    Descrizione varchar(255) not null ,
    Prezzo int not null ,
    Venduto boolean not null
    Id_asta int REFERENCES ASTA(Id_asta)
        ON UPDATE CASCADE
        ON DELETE NO ACTION,
    );

CREATE TABLE ASTA (
    Id_asta int PRIMARY KEY ,
    Prezzo_iniziale integer not null ,
    Rialzo_minimo integer not null ,
    Scadenza datetime not null,
    Offerta_massima integer not null , 
    Chiusa boolean not null
);

CREATE TABLE OFFERTA (
    Id_asta int REFERENCES ASTA(Id_asta)
        ON UPDATE CASCADE
        ON DELETE NO ACTION,
    Nome_utente varchar(45) REFERENCES UTENTE(Nome_utente)
        ON UPDATE CASCADE
        ON DELETE NO ACTION,
    Prezzo_offerto int not null ,
    Data_ora datetime not null ,
    PRIMARY KEY (Id_asta, Nome_utente)
);

CREATE TABLE UTENTE (
    Nome varchar(45) not null ,
    Cognome varchar(45) not null ,
    Nome_utente varchar(45) not null UNIQUE,
    Password varchar(45) not null,
    Indirizzo varchar(100) not null ,
    User_id int PRIMARY KEY
);


INSERT INTO UTENTE (Nome, Cognome, Nome_utente,Password, Indirizzo, User_id)
VALUES ('Mario', 'Rossi', 'MR654', 'Pippo7', 'Via Montegani, 4', 34)
/*Così si inseriscono  vari elementi dentro il Database*/



