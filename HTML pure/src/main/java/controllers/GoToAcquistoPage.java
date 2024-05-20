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
import beans.Articolo;
import beans.Asta;
import beans.Offerta;
import beans.Utente;

@WebServlet({ "/GoToAcquistoPage" })
public class GoToAcquistoPage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public GoToAcquistoPage() {
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

	//GESTIONE ERRORE
	//metodo utilizzato pr rimandare l'utente a una pagina di errore, mostrandogli il tipo di errore
	private void forwardError(HttpServletRequest request, HttpServletResponse response, String errore) throws ServletException, IOException{
		request.setAttribute("errore", errore);
		RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/ErrorPage.jsp");
		dispatcher.forward(request, response);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		//Controllo sulla sessione
		String path = getServletContext().getContextPath();
		Utente u = null;
		HttpSession s = request.getSession(); 
		if (s.isNew() || s.getAttribute("user") == null) {
			response.sendRedirect(path + "/index.jsp");
			return;
		} else {
			u = (Utente) s.getAttribute("user");
		}

		// trovo le offerte aggiudicate dall'utente 
		ArrayList<Offerta> offerteAggiudicate = new ArrayList<>();
		OffertaDAO offertaDAO = new OffertaDAO(connection);
		try {
			offerteAggiudicate = offertaDAO.findOfferteAggiudicateUser(u.getUserId());
		} catch (SQLException e) {
			forwardError(request, response, e.getMessage());
			return;
		}

		//trovo gli articoli delle offerte aggiudicate
		ArrayList<ArrayList<Articolo>> articoliAggiudicati = new ArrayList<>();  //lista che contiene per ogni asta una lista di articoli
		ArticoloDAO articoloDAO = new ArticoloDAO(connection);
		for (int i=0; offerteAggiudicate!=null && i<offerteAggiudicate.size(); i++) {
			try {
				articoliAggiudicati.add(articoloDAO.findArticoliAsta(offerteAggiudicate.get(i).getAstaId()));
			} catch (SQLException e) {
				forwardError(request, response, e.getMessage());
				return;
			}
		}


		//trovo le aste aperte NON appartenenti all'utente
		ArrayList<Asta> asteAperte = new ArrayList<>();
		AstaDAO astaDAO = new AstaDAO(connection);
		try {
			asteAperte = astaDAO.findAsteAperteNotUser(u);
		} catch (SQLException e) {
			forwardError(request, response, e.getMessage());
			return;
		}

		request.setAttribute("asteAperte", asteAperte); 
		request.setAttribute("articoliAggiudicati", articoliAggiudicati);
		request.setAttribute("offerteAggiudicate", offerteAggiudicate); //serve per JSP!!
		RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/Acquisto.jsp");
		dispatcher.forward(request, response);
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String keyword = request.getParameter("keyword").toLowerCase(); //formatto la keyword in modo da avere una ricerca case insensitive
		AstaDAO astaDAO = new AstaDAO(connection);
		ArrayList<Asta> asteAperteKeyword;

		HttpSession s = request.getSession(); 
		Utente u = (Utente) s.getAttribute("user");
		if(u==null) {
			forwardError(request, response, "utente non trovato.. ");
			return;
		}
		if(keyword == null || keyword.isBlank() || keyword.isEmpty()) {
			String erroreKeyword = "Questa non è una parola chiave accettabile! Riprova...";
			forwardError(request, response, erroreKeyword);
			return;
		}

		try {
			asteAperteKeyword = astaDAO.findAsteAperteByKeyword(keyword, u.getUserId());
		} catch(SQLException e){
			forwardError(request, response, e.getMessage());
			return;
		}

		if(asteAperteKeyword == null) {
			String erroreNotFound = "Non esistono aste aperte contenenti questa parola chiave!";
			request.setAttribute("erroreNotFound", erroreNotFound); 
			doGet(request, response);
			return;
		} 

		request.setAttribute("asteAperteKeyword", asteAperteKeyword);
		doGet(request, response);
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
