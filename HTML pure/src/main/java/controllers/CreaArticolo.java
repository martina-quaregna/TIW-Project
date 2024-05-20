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

import DAO.ArticoloDAO;
import beans.Articolo;
import beans.Utente;

@WebServlet({ "/CreaArticolo" })
@MultipartConfig //-> è necessario ai fini del caricamento dell'immagine
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
		String errore=null;
		
		//controllo della sessione
		String path = getServletContext().getContextPath() ;
        HttpSession s = request.getSession();
        Utente user = null;
        if (s.isNew() || s.getAttribute("user") == null) {
			response.sendRedirect(path+ "/index.jsp");
			return;
		} else {
			user = (Utente) s.getAttribute("user");
		}	
	
        //parametri e controlli
        String nome = request.getParameter("Nome");
		String descrizione = request.getParameter("Descrizione");
		String prezzoString = request.getParameter("Prezzo");
		if(nome==null || nome.isEmpty() || descrizione == null || descrizione.isEmpty() || prezzoString == null || prezzoString.isEmpty()) {
			errore = "Alcuni dei parametri inseriti sono null o vuoti...";
			forwardError(request, response, errore);
			return;
		}
		if(descrizione.length()>254) {
			errore = "Ops, descrizione troppo lunga..";
			forwardError(request, response, errore);
			return;
		}		
		int prezzo=0;
		try {
			prezzo = Integer.parseInt(prezzoString);
		} catch (NumberFormatException e) {
			errore = "Errore di prezzo";
			forwardError(request, response, errore);
			return;
		} 
		if(prezzo <= 0) {
			forwardError(request, response, "inserisci un prezzo valido..");
			return;
		}
		Boolean venduto = false;
		if(request.getContentType()==null || !request.getContentType().startsWith("multipart/form-data")) {
			forwardError(request, response, "content type errato, non toccare l'html :)");
			return;
		}
		Part image = request.getPart("Immagine");
		String fileName = image.getSubmittedFileName();
		String var = getServletContext().getMimeType(fileName);
		InputStream in = null;
		if(image==null || image.getSize() <= 0  || fileName==null || var==null || !image.getContentType().startsWith("image/")) {
			errore = "Ops, sembra che non siano stati inseriti alcuni parametri...";
			forwardError(request, response, errore);
			return;
		}
		try{
			in = image.getInputStream();
		}catch(IOException e) {
			forwardError(request, response, e.getMessage());
			return;
		}
		int userId = user.getUserId();
		if(userId<=0 || in == null || (in.available() == 0 )) {
			errore = "Ops, sembra che non siano stati inseriti alcuni parametri...";
			forwardError(request, response, errore);
			return;
		}

		//CREA ARTICOLO
		//Creazione dell'articolo in database
		ArticoloDAO articoloDAO = new ArticoloDAO(connection);
		try {
			Articolo articolo = articoloDAO.creaArticolo(nome, descrizione, prezzo, venduto, userId, in);
		} catch(SQLException e) {
			forwardError(request, response, e.getMessage());
			return;
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
