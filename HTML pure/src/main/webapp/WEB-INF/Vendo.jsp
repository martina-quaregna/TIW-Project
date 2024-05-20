<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">

<title>VENDO</title>
<link rel="stylesheet" href="https://www.w3schools.com/w3css/4/w3.css">
<link rel="stylesheet" href="https://fonts.googleapis.com/icon?family=Material+Icons">

</head>
<body>
	<div class="w3-bar w3-black" style="position:sticky;top: 0;right: 0;left: 0;z-index: 1030;">
		<a href="GoToHomePage"  class="w3-bar-item w3-button w3-mobile"><i class="material-icons">home</i> HOME</a>
		<a href="Logout"  class="w3-bar-item w3-button w3-mobile w3-right"><i class="material-icons">logout</i> LOGOUT</a>
	</div>
	

	<h1>PAGINA VENDO</h1>
	
	<c:if test = "${asteNonChiuse == null}">
		<h3 class="w3-text-red">Non ci sono aste create dall'utente e non ancora chiuse</h3>
	</c:if>
	<c:if test = "${asteNonChiuse != null}">
	<h2>Aste create dall'utente e non ancora chiuse</h2>	
	<div class="w3-panel">
	<ul class="w3-ul">	
		<c:forEach items="${asteNonChiuse}" var="asta" varStatus="as">
			<c:url value="/GoToDettaglioAsta" var="dettURL">
				<c:param name="idAsta" value="${asta.astaId}" />
			</c:url>
			<td></td>
				<c:forEach items="${tempoMancanteNONChiuse}" var="tempo" varStatus="temp">
					<c:if test="${as.index == temp.index}">
					<li class="w3-panel w3-light-grey"> <a href="${dettURL}" class="w3-text-green w3-large">Codice asta: ${asta.astaId}</a><br>Tempo mancante ${tempo[0]} giorni e ${tempo[1]} ore, scadenza: ${asta.scadenza}, offerta massima: ${asta.offertaMassima}€</li> 	
						<div class="w3-panel">				
						<table class="w3-table-all">
							<thead>
								<tr>
									<th> Codice </th>
									<th> Nome </th>
								</tr>
							</thead>
							<tbody>
								<c:forEach var="articoliAsta" items="${articoliAsteNonChiuse}" varStatus="art">	
									<c:forEach var="articolo" items="${articoliAsta}" varStatus="row">
										<c:if test="${as.index == art.index}">
											<tr>		
												<td><c:out value="${articolo.codice}" /></td>
												<td><c:out value="${articolo.nome}" /></td>
											</tr>
										</c:if>
									</c:forEach>
								</c:forEach>
							</tbody>
						</table>
						</div>
					</c:if>
			</c:forEach>
		</c:forEach>
	</ul>
	</div>
	</c:if>

	<c:if test = "${asteChiuse == null}">
		<h3 class="w3-text-red">Non ci sono aste create dall'utente e chiuse</h3>
	</c:if>
	<c:if test = "${asteChiuse != null}">
	<h2>Aste create dall'utente e chiuse</h2>
	<div class="w3-panel">
	<ul class="w3-ul">			
		<c:forEach items="${asteChiuse}" var="asta" varStatus="as">
				<c:url value="/GoToDettaglioAsta" var="dettURL">
				<c:param name="idAsta" value="${asta.astaId}" />
			</c:url>
			<td></td>
			<li class="w3-panel w3-light-grey"><a href="${dettURL}"  class="w3-text-blue w3-large">Codice asta: ${asta.astaId}</a><br>Scadenza: ${asta.scadenza}, offerta massima: ${asta.offertaMassima}€  </li> 	
				<div class="w3-panel">
				<table class="w3-table-all">				
				<thead>
					<tr>
						<th> Codice </th>
						<th> Nome </th>
					</tr>
				</thead>
				<tbody>
					<c:forEach var="articoliAsta" items="${articoliAsteChiuse}" varStatus="art">
						<c:forEach var="articolo" items="${articoliAsta}" varStatus="row">
							<c:if test="${as.index == art.index}">
								<c:choose>
									<c:when test="${row.count % 2 == 0}">
										<tr class="even">
									</c:when>
									<c:otherwise>
										<tr>
									</c:otherwise>
								</c:choose>
								<tr>
									<td><c:out value="${articolo.codice}" /></td>
									<td><c:out value="${articolo.nome}" /></td>
								</tr>
							</c:if>
						</c:forEach>
					</c:forEach>
				</tbody>
			</table>
			</div>
		</c:forEach>
	</ul>
	</div>
	</c:if>

	<div class="w3-container">
	<h3>CREA ARTICOLO:</h3>
	<div id="Creazione Articolo" class="w3-panel w3-card-4">
		<c:url value="/CreaArticolo" var="creaArticolo" />			
		<form action="${creaArticolo}" method="POST"  enctype="multipart/form-data" >  
			<input id="Nome" type="text" name="Nome" placeholder="Nome articolo" required class="w3-input w3-animate-input" style="width:50%"/>
			<input id="Descrizione" type="text" name="Descrizione" placeholder="Descrizione (massimo 255 caratteri)" required class="w3-input w3-animate-input" style="width:50%"/> 
			<input id="Prezzo" type=number name="Prezzo" placeholder="Prezzo" required class="w3-input w3-animate-input" style="width:50%"/><br>
			<input id="Immagine" type = "file" name ="Immagine" required/><br> <br>
			<button type="submit"  class="w3-btn w3-black"> CREA </button><br><br>
		</form>
	</div>
	</div>
	<br>

	<div class="w3-container">
	<h3>CREA ASTA:</h3>
	<div class="w3-panel w3-card-4">
	<form action="CreaAsta" method="POST">
		<input id="Rialzo_minimo" type="number" name="Rialzo_minimo" placeholder="Rialzo Minimo" required class="w3-input w3-animate-input" style="width:50%"/> <br>
		<label>Scadenza:</label>
		<input type="datetime-local" name="Scadenza" value="" required > <br>
		<label for="articoli">Articoli disponibili:</label> 
			<c:forEach var="articolo" items="${articoliDisponibili}" varStatus="row"><br>
					<input style="text-indent: 50px;" type="checkbox" name="articolo" value="${articolo.codice}" >
					<c:out   value="${articolo.nome}"/> 			
			</c:forEach><br><br>
		<input type="submit" value="CREA" class="w3-btn w3-black"/><br><br>
	</form>
	</div>
	</div>
	<br>

</body>
</html>