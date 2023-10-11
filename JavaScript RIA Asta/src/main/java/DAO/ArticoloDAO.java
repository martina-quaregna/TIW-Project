package DAO;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;

import beans.Articolo;
import beans.ArticoloAsta;

public class ArticoloDAO {

	private Connection connection;


	public ArticoloDAO(Connection connection) {
		this.connection = connection;//setto la connessione dell'utente al database
	}

	// per trovare gli articoli relativi all'asta passata	
	public ArrayList<Articolo> findArticoliAsta(int idAsta) throws SQLException{
		ArrayList<Articolo> articoliAsta = new ArrayList<Articolo>();
		String query = "SELECT A.Codice, A.Nome, A.Descrizione, A.Prezzo, A.Immagine FROM (ARTICOLO as A JOIN ARTICOLOASTA as AA ON AA.Codice = A.Codice) JOIN ASTA ON ASTA.Id_asta = AA.Id_asta WHERE ASTA.Id_asta = ?";//seleziono gli articoli appartenenti ad un asta
		PreparedStatement preparedStatement = null;
		ResultSet result = null;
		try{ 
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1,idAsta);
			result = preparedStatement.executeQuery();
			if(!result.isBeforeFirst()) {
				return null;
			} else {
				while(result.next()) {
					Articolo articolo = new Articolo();
					articolo.setCodice(result.getInt("A.Codice"));
					articolo.setNome(result.getString("A.Nome"));
					articolo.setDescrizione(result.getString("A.Descrizione"));
					articolo.setPrezzo(result.getInt("A.Prezzo"));
					byte[] imgData = result.getBytes("immagine");
					String encodedImg=Base64.getEncoder().encodeToString(imgData);
					articolo.setImmagine(encodedImg);
					articoliAsta.add(articolo);
				}
			}
		}catch(SQLException e) {
			throw new SQLException("Errore nell'accesso al DB nel tentativo di estrarre gli articoli dell'asta");
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
		return articoliAsta;
	}

	

	//Trovo tutti gli articoli disponibili (non ancora appartenenti ad un'asta)
	public ArrayList<Articolo> findArticoliDisponibili(int idUser) throws SQLException{
		ArrayList<Articolo> articoliDisponibili = new ArrayList<>();
		//Seleziono gli articoli che non appartengono ancora a nessuna asta
		String query ="SELECT A.Codice, A.Nome, A.Descrizione, A.Prezzo, A.Venduto FROM ARTICOLO as A WHERE NOT EXISTS (SELECT AA.Codice FROM ARTICOLOASTA AS AA WHERE A.Codice = AA.Codice) AND A.User_id = ?";
		PreparedStatement preparedStatement = null;
		ResultSet result = null;
		try{
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1,idUser);
			result = preparedStatement.executeQuery();
			if(!result.isBeforeFirst()) {
				articoliDisponibili = null;
			} else {
				while(result.next()) {
					Articolo articolo = new Articolo();
					articolo.setCodice(result.getInt("A.Codice"));
					articolo.setNome(result.getString("A.Nome"));
					articolo.setDescrizione(result.getString("A.Descrizione"));
					articolo.setPrezzo(result.getInt("A.Prezzo"));
					articolo.setVenduto(false);
					articolo.setUserId(idUser);
					articoliDisponibili.add(articolo);
				}
			}
		}catch(SQLException e) {
			throw new SQLException("Errore nell'accesso al DB, mentre si cercano gli articoli disponibili dell'asta");
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
		return articoliDisponibili;
	}

	//seleziono un articolo ritornando i suoi dati
	public Articolo selectArticolo(int codiceArticolo, int idUser) throws SQLException{
		Articolo articolo = new Articolo();
		String query = "SELECT Nome, Descrizione, Prezzo, Venduto FROM ARTICOLO WHERE Codice = ? AND User_id = ?";
		PreparedStatement preparedStatement = null;
		ResultSet result = null;
		try{
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1, codiceArticolo);
			preparedStatement.setInt(2,idUser);
			result = preparedStatement.executeQuery();
			if(!result.isBeforeFirst()) {
				articolo = null;
			} else {
				result.next();
				articolo.setCodice(codiceArticolo);
				articolo.setNome(result.getString("Nome"));
				articolo.setDescrizione(result.getString("Descrizione"));
				articolo.setPrezzo(result.getInt("Prezzo"));
				articolo.setVenduto(false);
				articolo.setUserId(idUser);
			}
		} catch(SQLException e) {
			throw new SQLException("Errore nell'accesso al DB, mentre si cercano gli estrarre un articolo");
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
		return articolo;
	}

	//creo un articolo passando tutti i parametri necessari
	public Articolo creaArticolo(String Nome, String Descrizione, int Prezzo, boolean Venduto, int User_id, InputStream input) throws SQLException {
		String query = "INSERT INTO ARTICOLO (Nome, Descrizione, Prezzo, Venduto, User_id, Immagine) VALUES (?, ?, ?, ?, ?, ?)";
		PreparedStatement preparedStatement = null;
		Articolo articolo = new Articolo();
		try { 
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, Nome);
			preparedStatement.setString(2, Descrizione);
			preparedStatement.setInt(3, Prezzo);
			preparedStatement.setBoolean(4, Venduto);
			preparedStatement.setInt(5, User_id);
			preparedStatement.setBlob(6, input);
			preparedStatement.executeUpdate(); //--> IMPORTANTE serve a fare l'update del database
			
			articolo.setNome(Nome);
			articolo.setDescrizione(Descrizione);
			articolo.setPrezzo(Prezzo);
			articolo.setVenduto(Venduto);
			articolo.setUserId(User_id);
			articolo.setImmagine(input.toString());
		} catch(SQLException e) {
			throw new SQLException("Errore nell'accesso al DB, mentre si cerca di creare un articolo");
		} finally {
			try {
				preparedStatement.close();  //devo chiudere prepared statement
			} catch(Exception e) {
				throw new SQLException("Errore nel tentativo di chiusura di prepared statement");
			}
		}
		return articolo;
	}

	
	//metodo usato per aggiungere articoli in un'asta (collegarli nel database)
	public void aggiungiArticoloAdAsta(int codice, int idAsta) throws SQLException {
		String query ="INSERT INTO ARTICOLOASTA (Codice, Id_asta) VALUES (?, ?)";
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1, codice);
			preparedStatement.setInt(2, idAsta);
			preparedStatement.executeUpdate(); //--> IMPORTANTE serve a fare l'update del database
		} catch(SQLException e) {
			throw new SQLException("Errore nell'accesso al DB, mentre si cerca di aggiungere un articolo all'asta");
		} finally {
			try {
				preparedStatement.close();  //devo chiudere prepared statement
			} catch(Exception e) {
				throw new SQLException("Errore nel tentativo di chiusura di prepared statement");
			}
		}
	}
	
	//metodo per settare un articolo a venduto
	public void setVenduto(int astaId) throws SQLException{
		String query = "UPDATE ARTICOLO SET Venduto = 1 WHERE CODICE IN (SELECT CODICE FROM ARTICOLOASTA WHERE Id_asta = ?)";
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1,astaId);
			preparedStatement.executeUpdate();
		} catch(SQLException e) {
			throw new SQLException("Errore nell'accesso al DB, mentre si cerca di settare a venduto l'articolo");
		} finally {
			try {
				preparedStatement.close();  //devo chiudere prepared statement
			} catch(Exception e) {
				throw new SQLException("Errore nel tentativo di chiusura di prepared statement");
			}
		}
	}
}
