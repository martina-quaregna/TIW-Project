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
import com.mysql.cj.x.protobuf.MysqlxDatatypes.Array;

import DAO.ArticoloDAO;
import DAO.AstaDAO;
import beans.Articolo;
import beans.Asta;
import beans.Utente;

@WebServlet({ "/GoToVendoPage" })
@MultipartConfig //-> serve per far funzionare javascript
public class GoToVendoPage extends HttpServlet{
	private static final long serialVersionUID = 1L;
	private Connection connection = null;


	public GoToVendoPage() {
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

		//aste chiuse e non con i relativi articoli
		AstaDAO astaDAO = new AstaDAO(connection);
		ArticoloDAO articoloDAO = new ArticoloDAO(connection);
		ArrayList<Asta> asteChiuse = new ArrayList<>();
		ArrayList<Asta> asteNonChiuse = new ArrayList<>();
		ArrayList<ArrayList<Articolo>> articoliAsteChiuse = new ArrayList<>();
		ArrayList<ArrayList<Articolo>> articoliAsteNonChiuse = new ArrayList<>();
		ArrayList<long[]> tempoMancanteNonChiuse = new ArrayList<>(); 
		try {
			asteChiuse = astaDAO.findAsteChiuse(u.getUserId());
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Errore nel db");
			return;
		}
		try {
			asteNonChiuse = astaDAO.findAsteNonChiuse(u.getUserId());
		}catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Errore nel db");
			return;
		}
		long giorni, ore, diff;
		for(int i=0; asteNonChiuse!=null && i<asteNonChiuse.size(); i++) {
			diff = asteNonChiuse.get(i).getScadenza().getTime() - u.getIstanteLogin().getTime();
			if(diff>0) {
				giorni = (diff/(1000 * 60 * 60))/24;
				ore = (diff/(1000 * 60 * 60))%24;
			} else {
				giorni = 0;
				ore = 0;
			}
			long[] tempo = {giorni, ore};
			tempoMancanteNonChiuse.add(tempo);
		}
		for(int i=0; asteChiuse!=null && i<asteChiuse.size(); i++) {
			try{
				articoliAsteChiuse.add(articoloDAO.findArticoliAsta(asteChiuse.get(i).getAstaId()));
			} catch(SQLException e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().println("Errore nel db");
				return;				
			}
		}
		for(int i=0; asteNonChiuse!=null && i<asteNonChiuse.size(); i++) {
			try{
				articoliAsteNonChiuse.add(articoloDAO.findArticoliAsta(asteNonChiuse.get(i).getAstaId()));
			} catch(SQLException e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().println("Errore nel db");
				return;				
			}
		}

		//articoli disponibili per creare l'asta
		ArrayList<Articolo> articoliDisponibili = new ArrayList<>();
		try {
			articoliDisponibili = articoloDAO.findArticoliDisponibili(u.getUserId());
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Errore nel db");
			return;	
		}


		Map<String, ArrayList<?>> data = new HashMap<>();
		data.put("tempoMancanteNonChiuse", tempoMancanteNonChiuse);
		data.put("articoliAsteNonChiuse", articoliAsteNonChiuse);
		data.put("articoliAsteChiuse", articoliAsteChiuse);
		data.put("asteNonChiuse", asteNonChiuse);
		data.put("asteChiuse", asteChiuse);
		data.put("articoliDisponibiliList", articoliDisponibili);

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
