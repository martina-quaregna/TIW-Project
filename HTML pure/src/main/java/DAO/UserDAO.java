package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import beans.Utente;

public class UserDAO {

	private Connection connection;

	public UserDAO(Connection connection) {
		this.connection = connection;//setto la connessione dell'utente al database
	}
	
	//Verifica se le credenziali sono valide
	public Utente checkCredentials(String username, String password)throws SQLException {
		String query = "SELECT Nome, Cognome, Nome_utente, Indirizzo, User_id FROM UTENTE WHERE Nome_utente = ? AND Password = ? ";
		//la query seleziona gli attributi dell'utente che andrò a settare grazie al bean dell'utente
		PreparedStatement preparedStatement = null;
		ResultSet result = null;
		Utente utenteConnesso = new Utente();
		try {
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1,username);
			preparedStatement.setString(2, password);
			result = preparedStatement.executeQuery();
			if(!result.isBeforeFirst()) {  //le credenziali inserite non esistono nel DB
				utenteConnesso=null;
			} else {  //utente trovato: setto i suoi attributi 
				result.next(); 
				utenteConnesso.setCognome(result.getString("Cognome"));
				utenteConnesso.setNome(result.getString("Nome"));
				utenteConnesso.setIndirizzo(result.getString("Indirizzo"));
				utenteConnesso.setNomeUtente(result.getString("Nome_utente"));
				utenteConnesso.setUserId(result.getInt("User_id"));
			}
		} catch(SQLException e) {
			throw new SQLException("Errore nell'accesso al DB nel check delle credenziali");
		} finally {
			try {
				result.close(); //Devo chiudere result set
			} catch(Exception e) {
				throw new SQLException("Errore nel tentativo di chiusura del Result Set");
			}
			try {
				preparedStatement.close();  //devo chiudere prepared statement
			} catch(Exception e) {
				throw new SQLException("Errore nel tentativo di chiusura di prepared statement");
			}
		}
		return utenteConnesso;
	}

	
	//Verifica dei parametri per la registrazione
	public Utente checkRegister(String Nome, String Cognome, String username, String Password, String Indirizzo) throws SQLException {
		String query = "SELECT Nome, Cognome, Nome_utente, Indirizzo, User_id FROM UTENTE WHERE Nome_utente = ?";
		PreparedStatement preparedStatement = null;
		ResultSet result = null;
		Utente u = new Utente();
		try{
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1,username);
			result = preparedStatement.executeQuery();
			if(!result.isBeforeFirst()) {//non ha trovato nessun utente con lo username scelto => può crearlo!
				u = createUser(Nome, Cognome, username, Password, Indirizzo); 
			} else {
				u = null;
			}
		}catch(SQLException e) {
			throw new SQLException("Errore nell'accesso al DB nel controllo per la registrazione dell'utente");
		} finally {
			try {
				result.close(); //Devo chiudere result set
			} catch(Exception e) {
				throw new SQLException("Errore nel tentativo di chiusura del Result Set");
			}
			try {
				preparedStatement.close();  //devo chiudere prepared statement
			} catch(Exception e) {
				throw new SQLException("Errore nel tentativo di chiusura di prepared statement");
			}
		}
		return u;
	}

	
	//Crea l'utente con gli attributi passati
	protected Utente createUser(String Nome, String Cognome, String Nome_utente, String Password, String Indirizzo)throws SQLException {
		Utente utenteConnesso = new Utente();
		String query = "INSERT INTO UTENTE (Nome, Cognome, Nome_utente, Password, Indirizzo) VALUES (?, ?, ?, ?, ?)";
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1,Nome);
			preparedStatement.setString(2, Cognome);
			preparedStatement.setString(3, Nome_utente);
			preparedStatement.setString(4, Password);
			preparedStatement.setString(5, Indirizzo);
			preparedStatement.executeUpdate(); //--> IMPORTANTE serve a fare l'update del database
	
			utenteConnesso.setCognome(Cognome);
			utenteConnesso.setNome(Nome);
			utenteConnesso.setIndirizzo(Indirizzo);
			utenteConnesso.setNomeUtente(Nome_utente);
		}catch(SQLException e) {
			throw new SQLException("Errore nell'accesso al DB, mentre si cerca di aggiungere un articolo all'asta");
		} finally {
			try {
				preparedStatement.close();  //devo chiudere prepared statement
			} catch(Exception e) {
				throw new SQLException("Errore nel tentativo di chiusura di prepared statement");
			}
		}
		return utenteConnesso;		
	}
	
	//Estrae i dati dell'utente
	public Utente datiUtente(int userId) throws SQLException {
        String query = "SELECT Nome, Indirizzo, Nome_utente FROM Utente WHERE User_id = ?";
        Utente utente;
        PreparedStatement preparedStatement = null;
		ResultSet result = null;
		try{
			preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1,userId);
            result = preparedStatement.executeQuery();
            if(!result.isBeforeFirst()) { 
                utente = null;
            } else {
            	result.next();
                utente = new Utente();
                utente.setNome(result.getString("Nome"));
                utente.setNomeUtente(result.getString("Nome_utente"));
                utente.setIndirizzo(result.getString("Indirizzo"));
            }
		}catch(SQLException e) {
			throw new SQLException("Errore nell'accesso al DB, mentre si cerca di estrarre i dati dell'utente");
		} finally {
			try {
				result.close(); //Devo chiudere result set
			} catch(Exception e) {
				throw new SQLException("Errore nel tentativo di chiusura del Result Set");
			}
			try {
				preparedStatement.close();  //devo chiudere prepared statement
			} catch(Exception e) {
				throw new SQLException("Errore nel tentativo di chiusura di prepared statement");
			}
		}
        return utente;
    }

}
