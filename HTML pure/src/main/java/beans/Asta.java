package beans;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;

public class Asta {
	
	public Asta() {
		
	}
	 private int astaId;
	 private int prezzoIniziale; 
	 private int rialzoMinimo; 
	 private Timestamp scadenza; 
	 private int offertaMassima;
	 private boolean chiusa; 
	 private int userId; 
	 private Time tempoMancante;
	 
	 public int getAstaId() {
		 return this.astaId;
	 }
	 
	 public void setAstaId(int astaId) {
		 this.astaId = astaId;
	 }
	 
	 public int getPrezzoIniziale() {
		 return this.prezzoIniziale;
	 }
	 
	 public void setPrezzoIniziale(int prezzoIniziale) {
		 this.prezzoIniziale = prezzoIniziale;
	 }
	 
	 public int getRialzoMinimo() {
		 return this.rialzoMinimo;
	 }
	 
	 public void setRialzoMinimo(int rialzoMinimo) {
		 this.rialzoMinimo = rialzoMinimo;
	 }
	 
	 public Date getScadenza() {
		 return this.scadenza;
	 }
	 
	 public void setScadenza(Timestamp scadenza) {
		 this.scadenza = scadenza;
	 }
	 
	 public int getOffertaMassima() {
		 return this.offertaMassima;
	 }
	 
	 public void setOffertaMassima(int offertaMassima) {
		 this.offertaMassima = offertaMassima;
	 }
	 
	 public boolean getChiusa() {
		 return this.chiusa;
	 }
	 
	 public void setChiusa(boolean chiusa) {
		 this.chiusa = chiusa;
	 }
	 
	 public int getUserId() {
		 return this.userId;
	 }
	 
	 public void setUserId(int userId) {
		 this.userId = userId;
	 }
	 
	 public Time getTempoMancante() {
		 return this.tempoMancante;
	 }
	 
	 public void setTempoMancante(Time tempo) {
		 this.tempoMancante=tempo;
	 }
	 
	 
	 
	 
	 
	 
	 
	 
	        
	
}