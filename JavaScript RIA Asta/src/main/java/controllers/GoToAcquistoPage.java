package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import DAO.ArticoloDAO;
import DAO.AstaDAO;
import DAO.OffertaDAO;
import beans.Articolo;
import beans.Asta;
import beans.Offerta;
import beans.Utente;

@WebServlet({ "/GoToAcquistoPage" })
@MultipartConfig //-> serve per far funzionare javascript
public class GoToAcquistoPage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public GoToAcquistoPage() {
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
		String path = getServletContext().getContextPath();
		Utente u = null;
		HttpSession s = request.getSession(); 
		if (s.isNew() || s.getAttribute("user") == null) {
			response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
			response.getWriter().println("Utente non trovato..");
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
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Errore nel db");
			return;
		}

		//trovo gli articoli delle offerte aggiudicate
		ArrayList<ArrayList<Articolo>> articoliAggiudicati = new ArrayList<>();  //lista che contiene per ogni asta una lista di articoli
		ArticoloDAO articoloDAO = new ArticoloDAO(connection);
		for (int i=0; offerteAggiudicate!=null && i<offerteAggiudicate.size(); i++) {
			try {
				articoliAggiudicati.add(articoloDAO.findArticoliAsta(offerteAggiudicate.get(i).getAstaId()));
			} catch (SQLException e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().println("Errore nel db");
				return;
			}
		}

		//trovo le aste aperte NON appartenenti all'utente
		ArrayList<Asta> asteAperte = new ArrayList<>();
		AstaDAO astaDAO = new AstaDAO(connection);
		try {
			asteAperte = astaDAO.findAsteAperteNotUser(u);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Errore nel db");
			return;
		}

		Map<String, ArrayList<?>> data = new HashMap<>();
		data.put("offerteAggiudicate", offerteAggiudicate);
		data.put("articoliAggiudicati", articoliAggiudicati);
		data.put("asteAperte", asteAperte);
		Gson gson = new Gson();
		String json = new Gson().toJson(data);

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);

	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
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