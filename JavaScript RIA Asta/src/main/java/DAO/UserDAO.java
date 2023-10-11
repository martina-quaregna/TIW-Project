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
			preparedStatement.setString(1,username);//controllo il primo statement
			preparedStatement.setString(2, password);//controllo il secondo statement
			result = preparedStatement.executeQuery();
			if(!result.isBeforeFirst()) {
				utenteConnesso=null;
			} else {
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
