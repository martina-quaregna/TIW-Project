<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">

<title>Login page</title>
<link rel="stylesheet" href="https://www.w3schools.com/w3css/4/w3.css">
<link rel="stylesheet" href="https://fonts.googleapis.com/icon?family=Material+Icons">
  
</head>
<body>

	<h1>Benvenuto! </h1>
	<!--Qui facciamo la parte di autenticazione dell'utente -->
	<div id="Autenticazione" class="w3-container">
	 <form action="CheckLogin" method="POST" class="w3-panel w3-card-4">
	 <h3>Login: <i class="material-icons w3-right">person</i></h3>
			<input id="Nome" type="text" name ="username" placeholder="Nome utente" required class="w3-input"> 
			<input id="Password" type="password" name ="password" placeholder="Password" required class="w3-input"><br>
			<input id="submit" type="submit" value="Invio" class="w3-btn w3-black"> <br><br>
	</form> 
	<c:if test="${errore!=null}"><span class="w3-tag w3-red">ERRORE:<c:out value="${errore}"> </c:out></span></c:if>
	</div>
	
	
	<div class="w3-container">	
		<h3>Se non sei ancora registrato: </h3>
		<a href="CreateUser" class="w3-btn w3-black">Registrati</a>
	</div>

</body>
</html>