package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
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

import DAO.UserDAO;
import beans.Utente;

@WebServlet({ "/CheckLogin" })
public class CheckLogin extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public CheckLogin() {
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
		RequestDispatcher dispatcher = request.getRequestDispatcher("index.jsp");
		dispatcher.forward(request, response);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String errore = null;	

		String username = request.getParameter("username");
		String password = request.getParameter("password");
		UserDAO user = new UserDAO(connection); //creo una connessione al DAO dell'utente
		Utente u = null; //inizializzo un utente a null per poterlo verificare in seguito

		//verifica dei parametri (input non vuoto)
		if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
			errore = "Ops, sembra che non siano stati inseriti alcuni parametri...";			
			forwardError(request, response, errore);
			return;
		}

		try {
			u = user.checkCredentials(username, password);
		}catch(SQLException e){
			forwardError(request, response, e.getMessage());
			return;
		}

		if(u==null) {//l'utente è null (non è registrato oppure ha sbagliato a immettere dati)
			errore = "Utente inesistente! username o password errati";
			forwardError(request, response, errore);
			return;
		} 

		//salvo l'istante del login di un utente
		Date date = new Date();
		long currentDate = date.getTime();
		Timestamp currentTime = new Timestamp(currentDate);
		u.setIstanteLogin(currentTime);

		//reindirizzamento
		String path = getServletContext().getContextPath(); //il percorso da seguire nei vari casi
		request.getSession().setAttribute("user", u);//l'utente ha fatto tutto correttamente
		path = path + "/GoToHomePage";//lo mando alla Homepage
		response.sendRedirect(path);//faccio la redirect sulla pagina scelta	
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

