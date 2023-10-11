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

import DAO.ArticoloDAO;
import DAO.AstaDAO;
import DAO.OffertaDAO;
import DAO.UserDAO;
import beans.Articolo;
import beans.Asta;
import beans.Offerta;
import beans.Utente;

@WebServlet({ "/GetAstaChiusa" })
@MultipartConfig //-> serve per far funzionare javascript
public class GetAstaChiusa extends HttpServlet{
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public GetAstaChiusa() {
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
		//controlli parametro
		String idAstaString = request.getParameter("idAsta");
		if(idAstaString==null || idAstaString.isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Id asta non valido!");
			return;
		}
		int idAsta = 0;
		try {
			idAsta=Integer.parseInt(idAstaString);
		}catch (NumberFormatException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Number format exception per id asta!");
			return;
		}
		if(idAsta<=0) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Id non valido!");
			return;
		}

		String path = getServletContext().getContextPath() ;
		Utente u;
		HttpSession s = request.getSession(); 
		if (s.isNew() || s.getAttribute("user") == null) {
			response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
			response.getWriter().println("Utente non trovato..");
			return;
		} else {
			u = (Utente) s.getAttribute("user");
		}

		//controllo id utente corrente e id utente asta
		int idUtenteCorrente = u.getUserId();
		AstaDAO astaDao = new AstaDAO(connection);
		int idUtenteAsta;
		try {
			idUtenteAsta= astaDao.datiAsta(idAsta).getUserId();
		} catch (SQLException e1) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Errore nel db");
			return;
		}
		if(idUtenteAsta != idUtenteCorrente) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Non e' la tua asta!");
			return;
		}

		OffertaDAO offertaDAO = new OffertaDAO(connection);
		Utente aggiudicatario;
		AstaDAO astaDAO = new AstaDAO(connection);
		Asta asta = new Asta();     
		Offerta offerta = new Offerta();
		ArrayList<String> nomeUtente = new ArrayList<>();
		UserDAO userDAO = new UserDAO(connection);
		try {
			asta = astaDAO.datiAsta(idAsta);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Errore nel db");
			return;
		}
		try {
			//asta chiusa
			offerta = offertaDAO.maxOffertaAsta(idAsta); 
			aggiudicatario = null;
			if(offerta!=null) {
				aggiudicatario = userDAO.datiUtente(offerta.getUserId()); 
			}
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Errore nel db");
			return;
		}        

		Map<String, Object> data = new HashMap<>();
		data.put("asta", asta); //dati asta
		data.put("aggiudicatario", aggiudicatario); //nome + indirizzo
		data.put("offerta", offerta); //prezzo finale

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
