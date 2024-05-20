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

import DAO.UserDAO;
import beans.Utente;

@WebServlet({ "/CreateUser" })
public class CreateUser extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public CreateUser() {
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
	private void forwardToErrorPage(HttpServletRequest request, HttpServletResponse response, String errore) throws ServletException, IOException{
		request.setAttribute("errore", errore);
		RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/ErrorPage.jsp");
		dispatcher.forward(request, response);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/Registrazione.html");
		dispatcher.forward(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String nome = request.getParameter("Nome");
		String cognome = request.getParameter("Cognome");
		String indirizzo = request.getParameter("Indirizzo");
		String username = request.getParameter("Nickname");
		String password = request.getParameter("Password");
		String errore;

		//Controllo sui parametri (parametri non nulli)
		if (nome == null || nome.isEmpty() || cognome == null || cognome.isEmpty() ||
				indirizzo == null || indirizzo.isEmpty() || username == null || username.isEmpty() ||  
				password == null || password.isEmpty()) {
			errore = "Ops, sembra che non siano stati inseriti alcuni parametri...";
			forwardToErrorPage(request, response, errore);
			return;
		}

		//controllo sullo username		
		UserDAO userDao = new UserDAO(connection);
		Utente utente = null;
		try {
			utente = userDao.checkRegister(nome, cognome, username, password, indirizzo);
		} catch (SQLException e) {
			forwardToErrorPage(request, response, e.getMessage());
			return;
		}

		if(utente == null) {
			errore = "Questo utente esiste già!";
			forwardToErrorPage(request, response, errore);
			return;
		} 
		request.getSession().setAttribute("user", utente);
		RequestDispatcher dispatcher = request.getRequestDispatcher("/index.jsp");
		dispatcher.forward(request, response);
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
