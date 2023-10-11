package beans;

import java.sql.Timestamp;
import java.util.Date;

public class Offerta{	
	private int codiceOfferta;
	private int astaId;
	private int userId;
	private int prezzoOfferto;
	private Timestamp dataOra;
	
	public Offerta() {}
	
	public int getCodiceOfferta() {return this.codiceOfferta;}
	
	public void setCodiceOfferta(int cod) { this.codiceOfferta = cod;}
	
	public int getAstaId() {
		return this.astaId;
	}
	
	public void setAstaId(int id) {
		this.astaId = id;
	}
	
	public int getUserId() {
		return this.userId;
	}
	
	public void setUserId(int userId) {
		this.userId = userId;
	}
	
	public int getPrezzoOfferto() {
		return this.prezzoOfferto;
	}
	
	public void setPrezzoOfferto(int prezzo) {
		this.prezzoOfferto = prezzo;
	}
	
	public Date getDataOra() {
		return this.dataOra;
	}
	
	public void setDataOra(Timestamp dataOra) {
		this.dataOra = dataOra;
	}
}