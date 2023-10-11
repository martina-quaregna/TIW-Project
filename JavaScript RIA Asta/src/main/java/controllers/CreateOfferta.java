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
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import DAO.AstaDAO;
import DAO.OffertaDAO;
import beans.Offerta;
import beans.Utente;

@WebServlet("/CreateOfferta")
@MultipartConfig //-> serve per far funzionare javascript
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

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost (HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		//controllo sui parametri in ingresso: prezzo offerto e id asta
		String idAstaString = request.getParameter("idAsta"); 
		String prezzoString = request.getParameter("PrezzoOfferto"); //controlli sul parametro
		if(idAstaString==null || idAstaString.isEmpty() || prezzoString==null || prezzoString.isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("id asta o prezzo non validi!");
			return;
		}
		int prezzoOfferto = 0;
		int idAsta = 0;
		try {
			prezzoOfferto=Integer.parseInt(prezzoString);
		}catch (NumberFormatException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("number format exception nel prezzo offerto");
			return;
		}
		try {
			idAsta=Integer.parseInt(idAstaString);
		}catch (NumberFormatException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("number format exception nel id asta");
			return;
		}
		if(prezzoOfferto<=0) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Prezzo offerto non valido");
			return;
		}
		if(idAsta<=0) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Id asta non valido");
			return;
		}

		//controllo sessione
		String path = getServletContext().getContextPath() ;
		HttpSession s = request.getSession();
		Utente user = null;
		if (s.isNew() || s.getAttribute("user") == null) {
			response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
			response.getWriter().println("Utente non trovato..");
			return;
		} else {
			user = (Utente) s.getAttribute("user");
		}	


		//CREAZIONE OFFERTA
		Date date = new Date();
		Timestamp istanteOfferta = new Timestamp(date.getTime()); //mi salvo l'istante in cui viene creata l'offerta 
		AstaDAO astaDAO = new AstaDAO(connection);
		OffertaDAO offertaDAO = new OffertaDAO(connection);
		try {
			if(istanteOfferta.after(astaDAO.datiAsta(idAsta).getScadenza())) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Stai facendo un'offerta per un'asta scaduta!");
				return;
			}
			if(!offertaDAO.creaOfferta(idAsta, user.getUserId(), prezzoOfferto, istanteOfferta)){
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().println("Offerta non valida!");
				return;
			} else { //aggiorno offerta massima dell'asta!
				astaDAO.updateOffertaMAX(prezzoOfferto, idAsta);
			}
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Errore: prova a controllare la tua offerta");
			return;		
		}

		response.setStatus(HttpServletResponse.SC_OK);

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
