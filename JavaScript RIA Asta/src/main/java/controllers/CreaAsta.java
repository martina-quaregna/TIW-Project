package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
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

import DAO.ArticoloDAO;
import DAO.AstaDAO;
import beans.Articolo;
import beans.Asta;
import beans.Utente;

@WebServlet("/CreaAsta" )
@MultipartConfig //-> serve per far funzionare javascript
public class CreaAsta extends HttpServlet{
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public CreaAsta() {
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

	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException{
		//Conrollo sessione
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


		//lettura parametri
		String scadenzaTemp = request.getParameter("Scadenza");
		Timestamp scadenza = null;
		try {
			LocalDateTime ldt = LocalDateTime.parse(scadenzaTemp);
			scadenza = Timestamp.valueOf(ldt);
		}catch(DateTimeParseException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Parametri errati o mancanti, riprova!");
			return;
		}
		String rialzoMinimoString = request.getParameter("Rialzo_minimo");
		if(scadenzaTemp == null || scadenzaTemp.isEmpty() || scadenza == null || userId<=0 || rialzoMinimoString == null || rialzoMinimoString.isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Parametri errati o mancanti, riprova!");
			return;
		}
		Date date = new Date();
		long currentDate = date.getTime();
		Timestamp currentTime = new Timestamp(currentDate);
		if(currentTime.after(scadenza)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Scadenza non valida");
			return;
		}
		Integer rialzoMinimo;
		try {
			rialzoMinimo = Integer.parseInt(rialzoMinimoString);	
		}catch(NumberFormatException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Rialzo minimo non valido");
			return;	
		}
		if(rialzoMinimo<=0) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Rialzo minimo non valido");
			return;
		}

		//CREAZIONE ASTA
		ArticoloDAO articoloDao = new ArticoloDAO(connection);
		ArrayList<Articolo> articoliDisponibili = null;
		try {
			articoliDisponibili = articoloDao.findArticoliDisponibili(userId);
		} catch (SQLException e1) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Errore nel db (SQL EXCEPTION)");
			return;	
		}
		String[] codiciSelezionati = request.getParameterValues("articolo");
		if(codiciSelezionati==null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Parametri errati, riprova");
			return;
		}
		ArrayList<Articolo> articoliSelezionati = new ArrayList<>();
		boolean controllo=false;
		for(int i=0; i< codiciSelezionati.length; i++) {
			try {
				controllo = false;
				Articolo art = articoloDao.selectArticolo(Integer.parseInt(codiciSelezionati[i]), userId);
				for(Articolo articolo : articoliDisponibili) {
					if(articolo.getCodice() == Integer.parseInt(codiciSelezionati[i])) {
						controllo=true;
					}
				}
				if(!controllo) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					response.getWriter().println("Articolo selezionato non valido");
					return;
				}
				articoliSelezionati.add(art);

			} catch (NumberFormatException | SQLException e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().println("Errore nel db");
				return;	
			} 
		}

		AstaDAO astaDao = new AstaDAO(connection);		
		int idAsta=0;
		try {
			astaDao.createAsta((int)rialzoMinimo, scadenza, userId, articoliSelezionati);
		} catch(SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Errore nel db");
			return;
		}
		try{
			idAsta = astaDao.maxAstaId();
		} catch(SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Errore nel db");
			return;
		}			

		for(int i=0; i<articoliSelezionati.size(); i++) {
			try {
				articoloDao.aggiungiArticoloAdAsta(articoliSelezionati.get(i).getCodice(), idAsta);
			} catch (SQLException e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().println("Errore nel db");
				return;		
			}
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
