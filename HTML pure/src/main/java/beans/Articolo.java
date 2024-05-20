package beans;

public class Articolo{
	
	public Articolo() {
		
	}
	
	private int codice;
	private String nome;
	private String descrizione;
	private int prezzo;
	private boolean venduto;
	private int userId;
	private String immagine;
	

	
	public int getCodice() {
		return this.codice;
	}
	
	public void setCodice(int codice) {
		this.codice = codice;
	}
	
	public String getNome() {
		return this.nome;
	}
	
	public void setNome(String nome) {
		this.nome = nome;
	}
	
	public String getDescrizione() {
		return this.descrizione;
	}
	
	public void setDescrizione(String descrizione) {
		this.descrizione = descrizione;
	}
	
	public int getPrezzo() {
		return this.prezzo;
	}
	
	public void setPrezzo(int prezzo2) {
		this.prezzo = prezzo2;
	}
	
	public boolean getVenduto() {
		return this.venduto;
	}
	
	public void setVenduto(boolean venduto) {
		this.venduto = true;
	}
	
	public int getUserId() {
		return this.userId;
	}
	
	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getImmagine() {
		return immagine;
	}

	public void setImmagine(String immagine) {
		this.immagine = immagine;
	}
	
	
	
	
}