package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import beans.Offerta;
import beans.Utente;

public class OffertaDAO {

	private Connection connection;
	public OffertaDAO(Connection connection) {
		this.connection = connection;
	}
	
	//Trova le aste aggiudicate dell'utente 
	public ArrayList<Offerta> findOfferteAggiudicateUser(int utente) throws SQLException{
		ArrayList<Offerta> offerteAggiudicate = new ArrayList<Offerta>();
		String query ="SELECT O.Id_asta, O.Prezzo_offerto, O.Data_ora FROM ASTA as A JOIN OFFERTA as O ON O.Id_asta=A.Id_asta WHERE A.Chiusa = 1 AND O.Prezzo_offerto=A.Offerta_massima AND O.User_id=?"; //tutte le aste aggiudicate da un utente
		PreparedStatement preparedStatement = null;
		ResultSet result = null;
		try {
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1, utente);
			result = preparedStatement.executeQuery();
			if(!result.isBeforeFirst()) {
				return null;
			} else {
				while(result.next()) {
					Offerta offerta = new Offerta();
					offerta.setAstaId(result.getInt("O.Id_asta"));
					offerta.setPrezzoOfferto(result.getInt("O.Prezzo_offerto"));
					offerta.setDataOra(result.getTimestamp("O.Data_ora"));
					offerteAggiudicate.add(offerta);
				}
			}	
		}catch(SQLException e) {
			throw new SQLException("Errore nell'accesso al DB nella ricerca delle offerte aggiudicate");
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
		return offerteAggiudicate;
	}

	//trova le offerte relative a un'asta
	public ArrayList<Offerta> findOfferteAsta(int idAsta) throws SQLException{
		ArrayList<Offerta> offerteAsta = new ArrayList<>();
		String query = "SELECT User_id, Prezzo_offerto, Data_ora FROM OFFERTA WHERE Id_asta = ? ORDER BY Data_ora DESC";
		PreparedStatement preparedStatement = null;
		ResultSet result = null;
		try{
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1, idAsta);
			result = preparedStatement.executeQuery();
			if(!result.isBeforeFirst()) {
				return null;
			} else {
				while(result.next()) {
					Offerta offerta = new Offerta();
					offerta.setUserId(result.getInt("User_id"));
					offerta.setPrezzoOfferto(result.getInt("Prezzo_offerto"));
					offerta.setDataOra(result.getTimestamp("Data_ora"));
					offerteAsta.add(offerta);
				}
			}	
		}catch(SQLException e) {
			throw new SQLException("Errore nell'accesso al DB nella ricerca delle offerte ");
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
		return offerteAsta;
	}

	
	//Crea l'offerta coi parametri passati
	public boolean creaOfferta(int idAsta, int idUser, int prezzoOfferta, Timestamp dataEOra) throws SQLException {
		String query = "INSERT INTO OFFERTA(Id_asta, User_id, Prezzo_offerto, Data_ora) VALUES (?, ?, ?, ?)";
		PreparedStatement preparedStatement = null;
		Offerta offerta = new Offerta();
		Boolean flag = false;
		try {		
			if(checkOfferta(idAsta, prezzoOfferta)) { //l'offerta è valida
				preparedStatement = connection.prepareStatement(query);
				preparedStatement.setInt(1,idAsta);
				preparedStatement.setInt(2, idUser);
				preparedStatement.setInt(3, prezzoOfferta);
				preparedStatement.setTimestamp(4, dataEOra);
				preparedStatement.executeUpdate(); //--> IMPORTANTE serve a fare l'update del database
				
				offerta.setAstaId(idAsta); 
				offerta.setUserId(idUser);
				offerta.setPrezzoOfferto(prezzoOfferta);
				offerta.setDataOra(dataEOra);
				
				flag = true;
			}
		}catch(SQLException e) {
			throw new SQLException("Errore nell'accesso al DB, mentre si crea l'offerta ");
		} finally {
			try {
				preparedStatement.close();  //devo chiudere prepared statement
			} catch(Exception e) {
				throw new SQLException("Errore nel tentativo di chiusura di prepared statement!!");
			}
		}			
		return flag;
	}

	//Verifica dell'offerta: se il prezzo offerto è maggiore dell'offerta minima allora ritorna true
	public boolean checkOfferta(int idAsta, int prezzoOfferta) throws SQLException {
		String query = "SELECT max(Prezzo_offerto) FROM OFFERTA WHERE Id_asta = ?";
		PreparedStatement preparedStatement = null;
		ResultSet result = null;
		AstaDAO astaDAO = new AstaDAO(connection);
		int offertaMinima = 0;
		try{
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1,idAsta);
			result = preparedStatement.executeQuery();
			if(!result.isBeforeFirst()) { // se non esiste il prezzo il max prezzo offerto
				return false;
			} else {
				result.next();
				Offerta offerta = new Offerta();			
				offerta.setPrezzoOfferto(result.getInt(1));
				if(offerta.getPrezzoOfferto()!=0) { //c'è almeno un'offerta (max tra le offerte per quell'asta)
					offertaMinima = astaDAO.prezzoMinimo(idAsta, false);
				}else {
					offertaMinima = astaDAO.prezzoMinimo(idAsta, true);//mi salvo l'offerta minima possibile
				}
			}
		}catch(SQLException e) {
			throw new SQLException("Errore nell'accesso al DB nel check dell'offerta");
		} finally {
			try {
				result.close(); //Devo chiudere result set
			} catch(Exception e) {
				throw new SQLException("Errore nel tentativo di chiusura del Result Set");
			}
			try {
				preparedStatement.close();  //devo chiudere prepared statement
			} catch(Exception e) {
				throw new SQLException("Errore nel database");
			}
		}
		if(offertaMinima <= prezzoOfferta) {
			return true;
		} else {
			return false;
		}
	}
	
	//trova la massima offerta dell'asta	
	public Offerta maxOffertaAsta(int idAsta) throws SQLException {
		String query = "SELECT Prezzo_offerto, User_id FROM OFFERTA WHERE Prezzo_offerto = (SELECT MAX(Prezzo_offerto) FROM OFFERTA WHERE Id_asta=?)";
        Offerta offerta = new Offerta();
        PreparedStatement preparedStatement = null;
		ResultSet result = null;
        try {
        	preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1,idAsta);
            result = preparedStatement.executeQuery();
        	if(!result.isBeforeFirst()) { // se non esiste il prezzo il max prezzo offerto
        		offerta = null;
        	} else {
        		result.next();
				offerta.setPrezzoOfferto(result.getInt("Prezzo_offerto"));
            	offerta.setUserId(result.getInt("User_id"));
			}
        }catch(SQLException e) {
			throw new SQLException("Errore nell'accesso al DB mentre si cerca l'offerta max");
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
        return offerta;
    }
}
