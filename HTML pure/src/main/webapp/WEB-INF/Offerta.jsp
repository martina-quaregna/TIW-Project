<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">

<title>OFFERTA</title>
<link rel="stylesheet" href="https://www.w3schools.com/w3css/4/w3.css">
<link rel="stylesheet" href="https://fonts.googleapis.com/icon?family=Material+Icons">

</head>
<body>

	<div class="w3-bar w3-black" style="position:sticky;top: 0;right: 0;left: 0;z-index: 1030;">
		<a href="GoToHomePage"  class="w3-bar-item w3-button w3-mobile"><i class="material-icons">home</i> HOME</a>
		<a href="Logout"  class="w3-bar-item w3-button w3-mobile w3-right"><i class="material-icons">logout</i> LOGOUT</a>
		<a href="GoToAcquistoPage" class="w3-bar-item w3-button w3-mobile"><i class="material-icons">shopping_cart</i> ACQUISTO PAGE</a><br>
	</div>

<h1>OFFERTA PAGE</h1>
	
	<div class="w3-panel">
	<h2> ARTICOLI </h2>
	<c:if test="${articoliAsta==null}"><h3 class="w3-text-red">Non ci sono articoli...</h3></c:if>
	<c:if test="${articoliAsta!=null}">
		<table  class="w3-table-all">
			<thead>
				<tr class="w3-black">
					<th> Codice </th>
					<th> Nome </th>
					<th> Descrizione </th>
					<th> Prezzo </th>
					<th> Immagine </th>		
				</tr>
			</thead>
			<tbody>
				<c:forEach var="articolo" items="${articoliAsta}" varStatus="art">					
					<tr>	
						<td><c:out value="${articolo.codice}" /></td>
						<td><c:out value="${articolo.nome}" /></td>
						<td><c:out value="${articolo.descrizione}" /></td>
						<td><c:out value="${articolo.prezzo}" />€</td>
						<td><img width="150" src="data:image/jpeg;base64,${articolo.immagine}"/></td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
		<br>
	</c:if>
	
	<h2> OFFERTE PERVENUTE  </h2>
	<c:if test="${offerteAsta==null}"><h3 class="w3-text-red">Non ci sono offerte per questa asta!</h3></c:if>
	<c:if test="${offerteAsta!=null}">
	<table class="w3-table-all">
		<thead>
			<tr class="w3-black">
				<th> Utente N° </th>
				<th> Prezzo offerto </th>
				<th> Data e ora dell'offerta </th>	
			</tr>
		</thead>
		<tbody>
			<c:forEach var="offerta" items="${offerteAsta}">					
				<tr>	
					<td><c:out value="${offerta.userId}" /></td>
					<td><c:out value="${offerta.prezzoOfferto}" />€</td>
					<td><c:out value="${offerta.dataOra}" /></td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
	</c:if>
	<br>
		
	
	<h2>Inserisci qui la tua offerta:</h2>
	<div class="w3-panel">
	<form action="CreateOfferta" method="POST" >	
		<input type="hidden" name="idAsta" value="${idAsta}" />
		<input name="PrezzoOfferto" type=number placeholder="100" required class="w3-input w3-border ">
		<p><c:if test="${asta.offertaMassima!=0}"> offerta minima ammissibile ${asta.offertaMassima + asta.rialzoMinimo}€</c:if>
			<c:if  test="${asta.offertaMassima==0}"> offerta minima ammissibile ${asta.prezzoIniziale + asta.rialzoMinimo}€</c:if>
		</p>
		<input type="submit" value="Invia" class="w3-button w3-black"/> 
	</form>
	</div>
	
	</div>

</body>
</html>