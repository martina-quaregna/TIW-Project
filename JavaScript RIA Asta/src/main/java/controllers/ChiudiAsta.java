package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
import beans.Utente;

@WebServlet( "/ChiudiAsta" )
@MultipartConfig //-> serve per far funzionare javascript
public class ChiudiAsta extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public ChiudiAsta() {
		super();
	}

	//metodo utilizzato per creare la connessione con il database
	public void init() throws ServletException { //collegamento al DB
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
			throws ServletException, IOException{
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException{
		String errore = null;
		//Controllo sessione
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
		int userId = user.getUserId();

		String idAstaString = request.getParameter("idAsta");
		if(idAstaString==null || idAstaString.isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("id asta non valido!");
			return;
		}
		int idAsta = 0;
		try {
			idAsta=Integer.parseInt(idAstaString);
		}catch (NumberFormatException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("number format exception nel id asta");
			return;
		}
		if(idAsta<=0) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("id asta non valido!");
			return;
		}


		//controllo id utente corrente e id utente asta
		AstaDAO astaDao = new AstaDAO(connection);
		int idUtenteAsta;
		try {
			idUtenteAsta= astaDao.datiAsta(idAsta).getUserId();
		} catch (SQLException e1) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Errore nel db (SQL EXCEPTION)");
			return;	
		}
		if(idUtenteAsta != userId) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Non è tua l'asta!");
			return;
		}


		try {
			AstaDAO astaDAO = new AstaDAO(connection);
			if(astaDAO.chiudiAstaSeScaduta(idAsta)==0) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Non puoi chiudere l'asta..");
				return;
			}
		} catch(SQLException e) { 
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Non puoi chiudere l'asta!");
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


