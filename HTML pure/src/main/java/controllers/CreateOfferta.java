package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import DAO.AstaDAO;
import DAO.OffertaDAO;
import beans.Offerta;
import beans.Utente;

@WebServlet({"/CreateOfferta"})
public class CreateOfferta extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public CreateOfferta() {
		super();
	}

	//metodo utilizzato per creare la connessione con il database
	public void init() throws ServletException {
		try {
			ServletContext context = getServletContext();
			String driver = context.getInitParameter("dbDriver");
			String url = context.getInitParameter("dbUrl");
			String user = context.getInitParameter("dbUser");
			String password = context.getInitParameter("dbPassword");
			Class.forName(driver);
			connection = DriverManager.getConnection(url, user, password);
		} catch (ClassNotFoundException e) {
			throw new UnavailableException("Can't load database driver");
		} catch (SQLException e) {
			throw new UnavailableException("Couldn't get db connection");
		}
	}

	//GESTIONE ERRORE
	//metodo utilizzato pr rimandare l'utente a una pagina di errore, mostrandogli il tipo di errore
	private void forwardError(HttpServletRequest request, HttpServletResponse response, String errore) throws ServletException, IOException{
		request.setAttribute("errore", errore);
		RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/ErrorPage.jsp");
		dispatcher.forward(request, response);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost (HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		//controllo sui parametri in ingresso: prezzo offerto e id asta
		String idAstaString = request.getParameter("idAsta"); 
		String prezzoString = request.getParameter("PrezzoOfferto"); 
		if(idAstaString==null || idAstaString.isEmpty() || prezzoString==null || prezzoString.isEmpty()) {
			forwardError(request, response, "Qualche parametro non è corretto..");
			return;
		}
		int prezzoOfferto = 0;
		int idAsta = 0;
		try {
			prezzoOfferto=Integer.parseInt(prezzoString);
		}catch (NumberFormatException e) {
			forwardError(request, response, "il prezzo offerto non è un numero!");
			return;
		}
		try {
			idAsta=Integer.parseInt(idAstaString);
		}catch (NumberFormatException e) {
			forwardError(request, response, "l'id dell'asta non è un numero!");
			return;
		}
		if(prezzoOfferto<=0) {
			forwardError(request, response, "il prezzo offerto non è valido! Inserisci un numero maggiore di 0!");
			return;
		}
		if(idAsta<=0) {
			forwardError(request, response, "l'id dell'asta non è valido!");
			return;
		}

		//controllo sessione
		String path = getServletContext().getContextPath() ;
		HttpSession s = request.getSession();
		Utente user = null;
		if (s.isNew() || s.getAttribute("user") == null) {
			response.sendRedirect(path+ "/index.jsp");
			return;
		} else {
			user = (Utente) s.getAttribute("user");
		}	

		//CREAZIONE OFFERTA
		//Qui viene creata l'offerta nel database
		Date date = new Date();
		Timestamp istanteOfferta = new Timestamp(date.getTime()); //mi salvo l'istante in cui viene creata l'offerta 
		AstaDAO astaDAO = new AstaDAO(connection);
		OffertaDAO offertaDAO = new OffertaDAO(connection);
		try {
			if(istanteOfferta.after(astaDAO.datiAsta(idAsta).getScadenza())) {
				forwardError(request, response, "Non puoi fare offerte, ormai l'asta è scaduta!");
				return;
			}
			if(!offertaDAO.creaOfferta(idAsta, user.getUserId(), prezzoOfferto, istanteOfferta)){
				String errore = "Ops, semra che qualcosa sia andato storto, quest'offerta non è valida!";
				forwardError(request, response, errore);
				return;
			} else { //aggiorno offerta massima dell'asta!
				astaDAO.updateOffertaMAX(prezzoOfferto, idAsta);
			}
		} catch (SQLException e) {
			forwardError(request, response, "errore nell'invio dell'offerta, l'offerta inserita potrebbe non essere valida");
			return;		
		}

		path = getServletContext().getContextPath() + "/GoToOffertaPage?idAsta="+idAsta;
		response.sendRedirect(path);
	}

	//chiudo la connessione con il database
	public void destroy() { 
		try {
			if (connection != null) {
				connection.close();
			}
		}catch(SQLException sqlex) {
			sqlex.printStackTrace();		
		}

	}
}
