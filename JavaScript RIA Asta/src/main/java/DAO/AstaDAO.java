package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import beans.Articolo;
import beans.Asta;
import beans.Utente;

public class AstaDAO {

	private Connection connection;


	public AstaDAO(Connection connection) {
		this.connection = connection;
	}

	//Trovo tutte le aste aperte NON appartenenti all'utente connesso
	public ArrayList<Asta> findAsteAperteNotUser(Utente utente) throws SQLException{
		ArrayList<Asta> asteAperte = new ArrayList<>();
		int idUser = utente.getUserId();//mi ricavo lo userId grazie alla sessione dell'utente		
		String query = "SELECT Id_asta, Prezzo_iniziale, Rialzo_minimo, Scadenza, Offerta_massima FROM ASTA WHERE Chiusa = 0 AND User_id != ? ORDER BY Scadenza DESC";
		PreparedStatement preparedStatement = null;
		ResultSet result = null;
		try{
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1, idUser);
			result = preparedStatement.executeQuery();
			if(!result.isBeforeFirst()) {
				asteAperte = null;// se non trovo aste aperte dovrò ritornare null
			} else {
				while(result.next()) {//fino a quando ho righe nella tabella vado avanti ad aggungere aste nel mio ArrayList
					Asta asta = new Asta();
					asta.setScadenza(result.getTimestamp("Scadenza"));
					Date dataCorrente = new Date ();
					
					if(asta.getScadenza().after(dataCorrente)) { //è ancora aperta
						asta.setAstaId(result.getInt("Id_asta"));
						asta.setPrezzoIniziale(result.getInt("Prezzo_iniziale"));
						asta.setRialzoMinimo(result.getInt("Rialzo_minimo"));
						asta.setOffertaMassima(result.getInt("Offerta_massima"));
						asta.setChiusa(false);
						asta.setUserId(idUser);
						asteAperte.add(asta);
					}
				}
			}
		}catch(SQLException e) {
			throw new SQLException("Errore nell'accesso al DB, mentre si cercano le aste aperte");
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

		return asteAperte;
	}

	
	//trovo le aste aperte che contengono la parola chiave nella descrizione o nel nome degli articoli
	public ArrayList<Asta> findAsteAperteByKeyword(String keyword, int utenteId) throws SQLException {
		ArrayList<Asta> asteAperteKeyword = new ArrayList<>();
		String query = "SELECT DISTINCT ASTA.Id_asta, ASTA.Prezzo_iniziale, ASTA.Rialzo_minimo, ASTA.Scadenza, ASTA.Offerta_massima, ASTA.User_id FROM ((ARTICOLO AS A JOIN ARTICOLOASTA AS AA ON A.Codice = AA.Codice) JOIN ASTA ON AA.Id_asta = ASTA.Id_asta) WHERE ASTA.Chiusa=false AND Asta.User_id != ?  AND (A.Nome LIKE ? OR A.Descrizione LIKE ?) ORDER BY ASTA.Scadenza DESC";
		PreparedStatement preparedStatement = null;
		ResultSet result = null;
		try {
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1,utenteId);
			preparedStatement.setString(2, '%'+keyword+'%');
			preparedStatement.setString(3, '%'+keyword+'%');
			result = preparedStatement.executeQuery();
			if(!result.isBeforeFirst()) {
				asteAperteKeyword = null;// se non trovo aste aperte dovrò ritornare null
			} else {
				while(result.next()) {//fino a quando ho righe nella tabella vado avanti ad aggungere aste nel mio ArrayList
					int idAsta = result.getInt("Id_asta");
					if(!checkAstaScaduta(idAsta)) {//se l'asta è scaduta non viene mostrata
						Asta asta = new Asta();
						asta.setAstaId(idAsta);
						asta.setPrezzoIniziale(result.getInt("Prezzo_iniziale"));
						asta.setRialzoMinimo(result.getInt("Rialzo_minimo"));
						asta.setScadenza(result.getTimestamp("Scadenza"));
						asta.setOffertaMassima(result.getInt("Offerta_massima"));							
						asta.setChiusa(false);
						asta.setUserId(result.getInt("User_id"));
						asteAperteKeyword.add(asta);
					}
				}
			}
		} catch(SQLException e) {
			throw new SQLException("Errore nell'accesso al DB, mentre si cercano le aste aperte che hanno nel nome/descrizione degli articoli la parola chiave cercata");
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
		return asteAperteKeyword;
	}

	
	//trovo le aste chiuse di un utente(attributo Chiusa = 1)
	public ArrayList<Asta> findAsteChiuse(int idUser) throws SQLException{
		ArrayList<Asta> asteChiuse = new ArrayList<>();
		String query = "SELECT Id_asta, Prezzo_iniziale, Rialzo_minimo, Scadenza, Offerta_massima FROM ASTA WHERE Chiusa = true AND User_id = ? ORDER BY scadenza ASC";
		PreparedStatement preparedStatement = null;
		ResultSet result = null;
		try{
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1, idUser);
			result = preparedStatement.executeQuery();
			if(!result.isBeforeFirst()) {
				asteChiuse = null;// se non trovo aste chiuse dovrò ritornare null
			} else {
				while(result.next()) {//fino a quando ho righe nella tabella vado avanti ad aggungere aste nel mio ArrayList
					Asta asta = new Asta();
					asta.setAstaId(result.getInt("Id_asta"));
					asta.setPrezzoIniziale(result.getInt("Prezzo_iniziale"));
					asta.setRialzoMinimo(result.getInt("Rialzo_minimo"));
					asta.setScadenza(result.getTimestamp("Scadenza"));
					asta.setOffertaMassima(result.getInt("Offerta_massima"));
					asta.setChiusa(true);
					asta.setUserId(idUser);

					asteChiuse.add(asta);
				}
			}
		}catch(SQLException e) {
			throw new SQLException("Errore nell'accesso al DB, mentre si cercano aste chiuse");
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
		return asteChiuse;
	}


	//Questo metodo mi fa la ricerca delle aste non chiuse di un utente e mi restituisce un ArrayList di tutte le aste non chiuse con i relativi dati
	public ArrayList<Asta> findAsteNonChiuse(int idUser) throws SQLException{
		ArrayList<Asta> asteNonChiuse = new ArrayList<>();
		String query = "SELECT Id_asta, Prezzo_iniziale, Rialzo_minimo, Scadenza, Offerta_massima FROM ASTA WHERE Chiusa = false AND User_id = ? ORDER BY scadenza ASC";
		PreparedStatement preparedStatement = null;
		ResultSet result = null;
		try{
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1, idUser);
			result = preparedStatement.executeQuery();
			if(!result.isBeforeFirst()) {
				asteNonChiuse = null;// se non trovo aste aperte dovrò ritornare null
			} else {
				while(result.next()) {//fino a quando ho righe nella tabella vado avanti ad aggungere aste nel mio ArrayList
					Asta asta = new Asta();
					asta.setAstaId(result.getInt("Id_asta"));
					asta.setPrezzoIniziale(result.getInt("Prezzo_iniziale"));
					asta.setRialzoMinimo(result.getInt("Rialzo_minimo"));
					asta.setScadenza(result.getTimestamp("Scadenza"));
					asta.setOffertaMassima(result.getInt("Offerta_massima"));
					asta.setChiusa(false);
					asta.setUserId(idUser);
					asteNonChiuse.add(asta);
				}
			}
		}catch(SQLException e) {
			throw new SQLException("Errore nell'accesso al DB, mentre si cercano aste non chiuse");
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
		return asteNonChiuse;
	}

	
	//crea l'asta con i parametri passati
	public void createAsta(int rialzoMinimo, Timestamp scadenza, int idUser, ArrayList<Articolo> articoliSelezionati) throws SQLException {
		String query = "INSERT INTO ASTA(Prezzo_iniziale, Rialzo_minimo, Scadenza, Offerta_massima, Chiusa, User_id) VALUES (?, ?, ?, ?, ?, ?)";
		int prezzoIniziale = 0;
		int offertaMassima = 0;
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = connection.prepareStatement(query);
			//Calcolo prezzo iniziale 
			for(int i=0; i<articoliSelezionati.size(); i++) {
				prezzoIniziale += articoliSelezionati.get(i).getPrezzo();
			}
			preparedStatement.setInt(1, prezzoIniziale);
			preparedStatement.setInt(2, rialzoMinimo);
			preparedStatement.setTimestamp(3, scadenza);//scadenza
			preparedStatement.setInt(4, offertaMassima);
			preparedStatement.setBoolean(5, false);
			preparedStatement.setInt(6, idUser);
			preparedStatement.executeUpdate(); //--> IMPORTANTE serve a fare l'update del database
			
			Asta asta = new Asta(); //FIXME: è necessario creare l'asta nel beans se non viene restituita?
			asta.setPrezzoIniziale(prezzoIniziale);
			asta.setRialzoMinimo(rialzoMinimo);
			asta.setScadenza(scadenza);
			asta.setOffertaMassima(offertaMassima);
			asta.setChiusa(false);
			asta.setUserId(idUser);
		} catch(SQLException e) {
			throw new SQLException("Errore nell'accesso al DB, mentre si cerca di creare un'asta");
		} finally {
			try {
				preparedStatement.close();  //devo chiudere prepared statement
			} catch(Exception e) {
				throw new SQLException("Errore nel tentativo di chiusura di prepared statement");
			}
		}		
	}

	
	//trovo il massimo id tra le aste
	public int maxAstaId() throws SQLException{
		String query = "SELECT max(Id_asta) FROM ASTA";
		Asta asta = new Asta();
		PreparedStatement preparedStatement = null;
		ResultSet result = null;
		try{
			preparedStatement = connection.prepareStatement(query);
			result = preparedStatement.executeQuery();
			if(!result.isBeforeFirst()) {
				asta.setAstaId(0);
			} else {
				result.next();
				asta.setAstaId(result.getInt(1));
			}
		}catch(SQLException e) {
			throw new SQLException("Errore nell'accesso al DB, mentre si l'asta max");
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
		return asta.getAstaId();
	}		
			

	//Ritorna true se l'asta è scaduta, false altrimenti 
	protected boolean checkAstaScaduta(int idAsta) throws SQLException {
		String query = "SELECT Scadenza FROM asta WHERE Id_asta = ?";
		PreparedStatement preparedStatement = null;
		ResultSet result = null;
		Boolean flag = false;
		try{
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1,idAsta);
			result = preparedStatement.executeQuery();
			Asta asta = new Asta();
			Timestamp scadenza = new Timestamp(0);
			if(result.next()) {
				scadenza = result.getTimestamp("Scadenza");
				//creo la data attuale -> lo trasformo in long per poterlo passare a Timestamp
				Date date = new Date();
				long currentDate = date.getTime();
				Timestamp currentTime = new Timestamp(currentDate);
				if(currentTime.after(scadenza)) {
					flag = true;
				} else {
					flag = false;
				}
			} 			
		} catch(SQLException e) {
			throw new SQLException("Errore nell'accesso al DB durante la verifica sulla scadenza dell'asta");
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
		return flag;
	}

	//setta Chiusa a 1 se l'asta è scaduta
	public int chiudiAstaSeScaduta(int idAsta) throws SQLException {
		//se l'asta è scaduta faccio questa query, altrimenti non faccio nulla
		String query = "UPDATE ASTA SET Chiusa = 1 WHERE Id_asta = ?";
		PreparedStatement preparedStatement = null;
		try {
			if(checkAstaScaduta(idAsta)) {
				preparedStatement = connection.prepareStatement(query);
				preparedStatement.setInt(1,idAsta);
				preparedStatement.executeUpdate();
			} else {
				idAsta = 0;
			}
		} catch(SQLException e) {
			throw new SQLException("Errore nell'accesso al DB, mentre si cerca di chiudere l'asta");
		} finally {
			try {
				preparedStatement.close();  //devo chiudere prepared statement
			} catch(Exception e) {
				throw new SQLException("Errore nel tentativo di chiusura di prepared statement");
			}
		}
		return idAsta;
	}

	//calcola il prezzo minimo di un'asta: dipende se sono già presenti offerte per l'asta o no
	public int prezzoMinimo(int idAsta, boolean primaOfferta) throws SQLException {
		String query = "SELECT Rialzo_minimo, Prezzo_iniziale, Offerta_massima FROM ASTA WHERE Id_asta = ?";
		PreparedStatement preparedStatement = null;
		ResultSet result = null;
		int ret;
		try {
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1,idAsta);
			result = preparedStatement.executeQuery();
			if(!result.isBeforeFirst()) { 
				ret = 0;
			} else {
				Asta asta = new Asta();
				result.next();
				asta.setRialzoMinimo(result.getInt("Rialzo_minimo"));
				asta.setPrezzoIniziale(result.getInt("Prezzo_iniziale"));
				asta.setOffertaMassima(result.getInt("Offerta_massima"));

				if(primaOfferta) {
					ret = (int) (asta.getPrezzoIniziale()+asta.getRialzoMinimo());
				} else {
					ret =  asta.getOffertaMassima() + asta.getRialzoMinimo();
				}	
			}
		}catch(SQLException e) {
			throw new SQLException("Errore nell'accesso al DB, mentre si cerca il prezzo minimo");
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
		return ret;
	}
	
	//Estrae i dati dell'asta
	public Asta datiAsta(int idAsta) throws SQLException {
		String query = "SELECT Prezzo_iniziale, Rialzo_minimo, Scadenza, Offerta_massima, User_id, Chiusa FROM ASTA WHERE Id_asta = ?";
		Asta asta;
		PreparedStatement preparedStatement = null;
		ResultSet result = null;
		try {
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1,idAsta);
			result = preparedStatement.executeQuery();
			if(!result.isBeforeFirst()) { 
				asta = null;
			} else {
				result.next();
				asta = new Asta();
				asta.setRialzoMinimo(result.getInt("Rialzo_minimo"));
				asta.setPrezzoIniziale(result.getInt("Prezzo_iniziale"));
				asta.setChiusa(result.getBoolean("Chiusa"));
				asta.setAstaId(idAsta);
				asta.setOffertaMassima(result.getInt("Offerta_massima"));
				asta.setScadenza(result.getTimestamp("Scadenza"));
				asta.setUserId(result.getInt("User_id"));
			}
		} catch(SQLException e) {
			throw new SQLException("Errore nell'accesso al DB, mentre si cercano i dati dell'asta");
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
		return asta;
	}
	
	//Aggiorna la massima offerta dell'asta (quando viene fatta una nuova offerta)
	public void updateOffertaMAX(int offertaMax, int idAsta) throws SQLException {
		String query = "UPDATE ASTA SET Offerta_massima=? WHERE Id_asta = ?";
		PreparedStatement preparedStatement = null;
		ResultSet result = null;
		try{
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1,offertaMax);
			preparedStatement.setInt(2,idAsta);
			preparedStatement.executeUpdate();
		} catch(SQLException e) {
			throw new SQLException("Errore nell'accesso al DB, mentre si cerca di aggiornare l'offerta massima");
		} finally {
			try {
				preparedStatement.close();  //devo chiudere prepared statement
			} catch(Exception e) {
				throw new SQLException("Errore nel tentativo di chiusura di prepared statement");
			}
		}
	}
}





