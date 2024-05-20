package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import DAO.ArticoloDAO;
import DAO.AstaDAO;
import beans.Utente;

@WebServlet({ "/ChiudiAsta" })
public class ChiudiAsta extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public ChiudiAsta() {
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
			throws ServletException, IOException{
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException{
		String errore = null;

		//Creazione e controllp della sessione
		String path = getServletContext().getContextPath() ;
		HttpSession s = request.getSession();
		Utente user = null;
		if (s.isNew() || s.getAttribute("user") == null) {
			response.sendRedirect(path+ "/index.jsp");
			return;
		} else {
			user = (Utente) s.getAttribute("user");
		}	
		int userId = user.getUserId();

		String idAstaString = request.getParameter("idAsta");
		if(idAstaString==null || idAstaString.isEmpty()) {
			forwardError(request, response, "l'id dell'asta è vuoto o null");
			return;
		}
		int idAsta = 0;
		try {
			idAsta=Integer.parseInt(idAstaString);
		}catch (NumberFormatException e) {
			forwardError(request, response, "l'id dell'asta non è un numero!");
			return;
		}
		if(idAsta<=0) {
			forwardError(request, response, "l'id dell'asta non è valido!");
			return;
		}


		//controllo id utente corrente e id utente asta
		AstaDAO astaDao = new AstaDAO(connection);
		int idUtenteAsta;
		try {
			idUtenteAsta= astaDao.datiAsta(idAsta).getUserId();
		} catch (SQLException e1) {
			forwardError(request, response, e1.getMessage());
			return;
		}
		if(idUtenteAsta != userId) {
			forwardError(request, response, "non è la tua asta!! NON puoi chiudere le aste di altri utenti!");
			return;
		}


		try {
			AstaDAO astaDAO = new AstaDAO(connection);
			ArticoloDAO articoloDAO = new ArticoloDAO(connection);
			if(astaDAO.chiudiAstaSeScaduta(idAsta)==0) {
				errore = "ATTENZIONE: non puoi chiudere l'asta!";
				forwardError(request, response, errore);
			}else{
				articoloDAO.setVenduto(idAsta);
			}
		} catch(SQLException e) {
			forwardError(request, response, e.getMessage());
			return;		
		}

		path = getServletContext().getContextPath() + "/GoToDettaglioAsta?idAsta="+idAsta;
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


