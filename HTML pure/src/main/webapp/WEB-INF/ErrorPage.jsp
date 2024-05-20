<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>ERROR PAGE</title>
<link rel="stylesheet" href="https://www.w3schools.com/w3css/4/w3.css">
<link rel="stylesheet" href="https://fonts.googleapis.com/icon?family=Material+Icons">
</head>
<body>

	<div class="w3-bar w3-black" style="position:sticky;top: 0;right: 0;left: 0;z-index: 1030;">
		<a href="GoToHomePage"  class="w3-bar-item w3-button w3-mobile"><i class="material-icons">home</i> HOME</a>
		<a href="Logout"  class="w3-bar-item w3-button w3-mobile w3-right"><i class="material-icons">logout</i> LOGOUT</a>
	</div>
	
	<h1> ERROR PAGE </h1>
	<div class="w3-container">
		<h3 style="color: red;"> <span class="w3-tag w3-red"> ATTENZIONE: </span><c:out value="${errore}"></c:out></h3>
		
	</div>
		
</body>
	

</html>