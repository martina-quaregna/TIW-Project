CREATE TABLE ARTICOLO (
    Codice int PRIMARY KEY ,
    Nome varchar(45) not null ,
    Descrizione varchar(255) not null ,
    Prezzo int not null ,
    Venduto boolean
    );

CREATE TABLE ASTA (
    Id_asta int PRIMARY KEY ,
    Prezzo_iniziale integer not null ,
    Rialzo_minimo integer not null ,
    Scadenza datetime ,
    Offerta_massima integer not null
);

CREATE TABLE ASTA_CHIUSA (
    Id_asta int REFERENCES ASTA(Id_asta)
        ON UPDATE CASCADE
        ON DELETE NO ACTION,
    Nome_aggiudicatario varchar(45) REFERENCES UTENTE(Nome_utente)
        ON UPDATE CASCADE
        ON DELETE NO ACTION,
    Prezzo_finale int REFERENCES ASTA(Offerta_massima)
        ON UPDATE CASCADE
        ON DELETE NO ACTION,
    Indririzzo_spedizione varchar(100) REFERENCES UTENTE(Indirizzo)
        ON UPDATE CASCADE
        ON DELETE NO ACTION,
    PRIMARY KEY(Id_asta, Nome_aggiudicatario)
);

CREATE TABLE OFFERTA (
    Id_asta int REFERENCES ASTA(Id_asta)
        ON UPDATE CASCADE
        ON DELETE NO ACTION,
    Nome_utente varchar(45) REFERENCES UTENTE(Nome_utente)
        ON UPDATE CASCADE
        ON DELETE NO ACTION,
    Prezzo_offerto int not null,
    Data_ora datetime,
    PRIMARY KEY (Id_asta, Nome_utente)
);

CREATE TABLE UTENTE (
    Nome varchar(45) not null ,
    Cognome varchar(45) not null ,
    Nome_utente varchar(45) not null ,
    Password varchar(45) not null,
    Indirizzo varchar(100) not null ,
    User_id int PRIMARY KEY
);


INSERT INTO UTENTE (Nome, Cognome, Nome_utente,Password, Indirizzo, User_id)
VALUES ('Mario', 'Rossi', 'MR654', 'Pippo7', 'Via Montegani, 4', 34)
/*Così si inseriscono  vari elementi dentro il Database*/



