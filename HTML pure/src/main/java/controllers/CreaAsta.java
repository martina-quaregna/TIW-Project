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

@WebServlet({ "/CreaAsta" })
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

	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException{
		//Creazione e controllo della sessione
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


		//lettura parametri
		String scadenzaTemp = request.getParameter("Scadenza");
		Timestamp scadenza = null;
		try {
			LocalDateTime ldt = LocalDateTime.parse(scadenzaTemp);
			scadenza = Timestamp.valueOf(ldt);
		}catch(DateTimeParseException e) {
			forwardError(request, response, e.getMessage());
			return;
		}
		String rialzoMinimoString = request.getParameter("Rialzo_minimo");
		if(scadenzaTemp == null || scadenzaTemp.isEmpty() || scadenza == null || userId<=0 || rialzoMinimoString == null || rialzoMinimoString.isEmpty()) {
			forwardError( request, response, "Ops, sembra che non siano stati inseriti alcuni parametri... o i parametri sono errati!");
			return;
		}
		Date date = new Date();
		long currentDate = date.getTime();
		Timestamp currentTime = new Timestamp(currentDate);
		if(currentTime.after(scadenza)) {
			forwardError( request, response, "Data errata!");
			return;
		}
		Integer rialzoMinimo;
		try {
			rialzoMinimo = Integer.parseInt(rialzoMinimoString);	
		}catch(NumberFormatException e) {
			forwardError(request, response, e.getMessage());
			return;
		}
		if(rialzoMinimo<=0) {
			forwardError( request, response, "Il rialzo minimo è inferiore o uguale a 0!");
			return;
		}

		//CREA ASTA
		//Qui viene creata l'asta nel database, sfruttando un ArrayList di articoli estratto tramite DAO
		ArticoloDAO articoloDao = new ArticoloDAO(connection);
		ArrayList<Articolo> articoliDisponibili = null;
		try {
			articoliDisponibili = articoloDao.findArticoliDisponibili(userId);
		} catch (SQLException e1) {
			forwardError(request, response, e1.getMessage());
			return;
		}
		String[] codiciSelezionati = request.getParameterValues("articolo");
		if(codiciSelezionati==null) {
			forwardError( request, response, "Ops, sembra che non siano stati inseriti alcuni parametri... o i parametri sono errati!");
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
					forwardError( request, response, "Ops, NON è un articolo disponibile o non ci sono articoli disponibili al momento!");
					return;
				}
				articoliSelezionati.add(art);

			}catch (NumberFormatException | SQLException e) {
				forwardError(request, response, e.getMessage());
				return;
			} 
		}

		AstaDAO astaDao = new AstaDAO(connection);		
		int idAsta=0;
		try {
			astaDao.createAsta((int)rialzoMinimo, scadenza, userId, articoliSelezionati);
		} catch(SQLException e) {
			forwardError(request, response, e.getMessage());
			return;	
		}
		try{
			idAsta = astaDao.maxAstaId(); //viene preso l'id dell'asta appena creata
		} catch(SQLException e) {
			forwardError(request, response, e.getMessage());
			return;	
		}			
		if(idAsta==0) {
			forwardError(request, response, "Errore nell'accesso al db");
			return;	
		}

		for(int i=0; i<articoliSelezionati.size(); i++) {
			try {
				articoloDao.aggiungiArticoloAdAsta(articoliSelezionati.get(i).getCodice(), idAsta);
			} catch (SQLException e) {
				forwardError(request, response, e.getMessage());
				return;		
			}
		}
		path = getServletContext().getContextPath() + "/GoToVendoPage";
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
