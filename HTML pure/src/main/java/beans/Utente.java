package beans;

import java.sql.Timestamp;

public class Utente {	
	private String nome;
	private String cognome;
	private String nomeUtente;
	private String indirizzo;
	private int userId;
	private Timestamp istanteLogin;
	
	public Utente() {}
	
	public String getNome() {
		return this.nome;
	}
	
	public void setNome(String name) {
		this.nome = name;
	}
	
	public String getCognome() {
		return this.cognome;
	}
	
	public void setCognome(String cognome) {
		this.cognome = cognome;
	}
	
	public String getNomeUtente() {
		return this.nomeUtente;
	}
	
	public void setNomeUtente(String utente) {
		this.nomeUtente = utente;
	}
	
	
	public String getIndirizzo() {
		return this.indirizzo;
	}
	
	public void setIndirizzo(String indirizzo) {
		this.indirizzo = indirizzo;
	}
	
	public int getUserId() {
		return this.userId;
	}
	
	public void setUserId(int id) {
		this.userId = id;
	}
	
	public void setIstanteLogin(Timestamp ist) {
		this.istanteLogin = ist;
	}
	
	public Timestamp getIstanteLogin() {
		return this.istanteLogin;
	}
}