package controllers;

import java.io.IOException;
import java.io.InputStream;
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
import javax.servlet.http.Part;

import com.google.gson.Gson;

import DAO.ArticoloDAO;
import beans.Articolo;
import beans.Utente;

@WebServlet( "/CreaArticolo" )
@MultipartConfig //-> serve per far funzionare javascript
public class CreaArticolo extends HttpServlet{
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public CreaArticolo() {
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
		String errore=null;

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

		//parametri e controlli
		String nome = request.getParameter("Nome");
		String descrizione = request.getParameter("Descrizione");
		String prezzoString = request.getParameter("Prezzo");
		if(nome==null || nome.isEmpty() || descrizione == null || descrizione.isEmpty() || prezzoString == null || prezzoString.isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Alcuni dei parametri non sono validi..");;
			return;
		}
		if(descrizione.length()>254) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("descrizione troppo lunga! ");
			return;
		}		
		int prezzo=0;
		try {
			prezzo = Integer.parseInt(prezzoString);
		} catch (NumberFormatException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("number format exception nel id asta");
			return;
		}
		if(prezzo <= 0) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Prezzo minore di 0, inserisci un prezzo valido! ");
			return;
		}
		Boolean venduto = false;
		if(request.getContentType()==null || !request.getContentType().startsWith("multipart/form-data")) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Non hai inserito l'immagine");
			return;
		}
		Part image = request.getPart("Immagine");
		String fileName = image.getSubmittedFileName(); //FIXME QUI ERROR
		String var = getServletContext().getMimeType(fileName);
		InputStream in = null;
		if(image==null || image.getSize() <= 0  || fileName==null || var==null || !image.getContentType().startsWith("image/")) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Non hai inserito l'immagine");
			return;
		}
		try{
			in = image.getInputStream();
		}catch(IOException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("IOException, riprova");
			return;
		}
		int userId = user.getUserId();
		if(userId<=0 || in == null || (in.available() == 0 )) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Parametri errati, riprova");
			return;
		}

		//CREA ARTICOLO
		ArticoloDAO articoloDAO = new ArticoloDAO(connection);
		Articolo articolo;
		try {
			articolo = articoloDAO.creaArticolo(nome, descrizione, prezzo, venduto, userId, in);
		} catch(SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Errore nel db (SQL EXCEPTION)");
			return;		
		}

		//Necessario per passare parametri al javascript
		Gson gson = new Gson();
		String json = new Gson().toJson(articolo);

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);
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
