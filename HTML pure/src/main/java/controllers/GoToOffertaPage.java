package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

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
import DAO.OffertaDAO;
import DAO.UserDAO;
import beans.Utente;
import beans.Articolo;
import beans.Asta;
import beans.Offerta;

@WebServlet({ "/GoToOffertaPage" })
public class GoToOffertaPage extends HttpServlet{
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public GoToOffertaPage() {
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

		String path = getServletContext().getContextPath() ;
		Utente u = null;
		HttpSession s = request.getSession(); 
		if (s.isNew() || s.getAttribute("user") == null) {
			response.sendRedirect(path+ "/index.jsp");
			return;
		} else {
			u = (Utente) s.getAttribute("user");
		}	

		//controllo id utente corrente e id utente asta
		int idUtenteCorrente = u.getUserId();
		AstaDAO astaDao = new AstaDAO(connection);
		Asta asta = new Asta();
		int idUtenteAsta;
		try {
			asta= astaDao.datiAsta(idAsta);
			idUtenteAsta = asta.getUserId();
		} catch (SQLException e1) {
			forwardError(request, response, e1.getMessage());
			return;
		}
		if(idUtenteAsta == idUtenteCorrente) {
			forwardError(request, response, "Non puoi acquistare le tue aste...");
			return;
		}
		if(asta.getChiusa()) {
			forwardError(request, response, "asta chiusa o scaduta, non puoi fare offerte");
			return;
		}

		//articoli presenti in una data asta
		ArticoloDAO articoloDAO = new ArticoloDAO(connection);
		ArrayList<Articolo> articoliAsta = new ArrayList<>();
		try {
			articoliAsta = articoloDAO.findArticoliAsta(idAsta);
		} catch (SQLException e) {
			forwardError(request, response, e.getMessage());
			return;
		}		

		//Offerte presenti per una data asta
		OffertaDAO offertaDAO = new OffertaDAO(connection);
		ArrayList<Offerta> offerteAsta = new ArrayList<>();
		try {
			offerteAsta = offertaDAO.findOfferteAsta(idAsta);
		} catch (SQLException e) {
			forwardError(request, response, e.getMessage());
			return;
		}

		request.setAttribute("asta", asta);
		request.setAttribute("offerteAsta", offerteAsta);
		request.setAttribute("articoliAsta", articoliAsta);
		request.setAttribute("idAsta", idAsta);
		RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/Offerta.jsp");
		dispatcher.forward(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

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
