package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet({ "/Logout" })
public class Logout extends HttpServlet{
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	
	public Logout() {
		super();
	}
	
	//Distruggo la sessione
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		} 		
		RequestDispatcher dispatcher = request.getRequestDispatcher("/index.jsp");
		dispatcher.forward(request, response);
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request,response);
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

