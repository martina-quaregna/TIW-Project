<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">

<title>DETTAGLIO ASTA</title>
<link rel="stylesheet" href="https://www.w3schools.com/w3css/4/w3.css">
<link rel="stylesheet" href="https://fonts.googleapis.com/icon?family=Material+Icons">

</head>
<body>

	<div class="w3-bar w3-black" style="position:sticky;top: 0;right: 0;left: 0;z-index: 1030;">
		<a href="GoToHomePage"  class="w3-bar-item w3-button w3-mobile"><i class="material-icons">home</i> HOME</a>
		<a href="Logout"  class="w3-bar-item w3-button w3-mobile w3-right"><i class="material-icons">logout</i> LOGOUT</a>
		<a href="GoToVendoPage" class="w3-bar-item w3-button w3-mobile"><i class="material-icons">sell</i> VENDO PAGE</a><br>
	</div>
	

<h1>Dettaglio asta</h1>
	<!-- dati asta -->
	<div class="w3-container">
	<div class="w3-panel w3-card-4">
	<ul class="w3-ul">
		<li class="w3-panel">Codice: ${Asta.astaId}</li>
		<li class="w3-panel">Prezzo iniziale: ${Asta.prezzoIniziale}€</li>
		<li class="w3-panel">Rialzo minimo: ${Asta.rialzoMinimo}€</li>
		<li class="w3-panel">Scadenza: ${Asta.scadenza}</li>
		<li class="w3-panel">OffertaMassima: ${Asta.offertaMassima}€</li>
		<c:if test="${Asta.chiusa == false}"><li class="w3-panel">APERTA</li></c:if>
		<c:if test="${Asta.chiusa == true}"> <!-- l'asta è chiusa -->
			<li class="w3-panel">CHIUSA</li>
			<c:if test="${aggiudicatario != null}"> <!-- l'asta è chiusa -->
				<li class="w3-panel">Nome aggiudicatario: ${aggiudicatario.nome}</li>
				<li class="w3-panel">Prezzo finale: ${offerta.prezzoOfferto}€</li>
				<li class="w3-panel">Indirizzo fisso di spedizione: ${aggiudicatario.indirizzo}</li>
			</c:if>	
			<c:if test="${aggiudicatario == null}"> <!-- l'asta è chiusa -->
				<li class="w3-panel w3-text-red">Nessuno si è aggiudicato l'asta :(</li>
			</c:if>
		</c:if>
	</ul>
	</div>
	</div>
	
	<h2> ARTICOLI </h2>
	<div class="w3-panel">
	<c:if test="${articoliAsta==null}"><h4>Non ci sono articoli...</h4></c:if>
	<c:if test="${articoliAsta!=null}">
		<table class="w3-table-all">
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
	
	
	<div><!-- l'asta APERTA -->
	<c:if test="${Asta.chiusa == false}"> 
		<c:if test="${offerteAsta==null}">
			<h4 class="w3-text-red">Non ci sono offerte...</h4>
		</c:if>
		<!-- LISTA OFFERTE -->
		<c:if test="${offerteAsta!=null}">
		<h3>LISTA DELLE OFFERTE </h3>
		<table class="w3-table-all">
			<thead>
				<tr class="w3-black">
					<th> Nome utente </th>
					<th> Prezzo offerto </th>
					<th> Data e ora dell'offerta </th>						
				</tr>
			</thead>
			<tbody>
				<c:forEach var="offerta" items="${offerteAsta}" varStatus = "off">
				<c:forEach var="nome" items="${nomeUtente}" varStatus = "name">
				<c:if test="${name.index == off.index}">
					<tr>
						<td><c:out value="${nome}"/></td>
						<td><c:out value="${offerta.prezzoOfferto}" />€</td>
						<td><c:out value="${offerta.dataOra}" /></td>
					</tr>
					</c:if>
					</c:forEach>
				</c:forEach>
			</tbody>
		</table>
		<br>
		</c:if>
		<form action="ChiudiAsta" method="POST">
			<input type="hidden" name="idAsta" value="${Asta.astaId}" />
			<button type="submit" class="w3-button w3-black">Chiudi Asta</button>
		</form>
	</c:if>
	</div>		
	
	
	<div>

	</div>
	</div>

	<br>

</body>
</html>