<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">

<title>ACQUISTO</title>

<link rel="stylesheet" href="https://www.w3schools.com/w3css/4/w3.css">
<link rel="stylesheet" href="https://fonts.googleapis.com/icon?family=Material+Icons">

</head>
<body>	
	<div class="w3-bar w3-black" style="position:sticky;top: 0;right: 0;left: 0;z-index: 1030;">
		<a href="GoToHomePage"  class="w3-bar-item w3-button w3-mobile"><i class="material-icons">home</i> HOME</a>
		<a href="Logout"  class="w3-bar-item w3-button w3-mobile w3-right"><i class="material-icons">logout</i> LOGOUT</a>
	</div>
	
	<h1>PAGINA ACQUISTO</h1>
	<div class="w3-container">

	<c:if test="${offerteAggiudicate==null}">
		<h3 class="w3-text-red">Non c'è nessun offerta aggiudicata :(</h3>
	</c:if>
	<c:if test="${offerteAggiudicate!=null}">
		<h2>OFFERTE AGGIUDICATE</h2>
		<div class="w3-panel">
		<ul  class="w3-ul">
			<c:forEach items="${offerteAggiudicate}" var="offerta" varStatus="i">
				<li class="w3-panel w3-light-grey">Asta aggiudicata numero: ${offerta.astaId}<br>prezzo finale: ${offerta.prezzoOfferto}€</li>
				<div class="w3-panel">
				<table class="w3-table-all">
					<thead>
						<tr>
							<th>Codice</th>
							<th>Nome</th>
							<th>Descrizione</th>
							<th>Prezzo articolo</th>
							<th> Immagine </th>		
						</tr>
					</thead>
					<tbody>
						<c:forEach var="articoliAsta" items="${articoliAggiudicati}" varStatus="j">
							<tr>
								<c:forEach var="articolo" items="${articoliAsta}" varStatus="row">
									<c:if test="${i.index == j.index}">
										<tr>
											<td><c:out value="${articolo.codice}" /></td>
											<td><c:out value="${articolo.nome}" /></td>
											<td><c:out value="${articolo.descrizione}" /></td>
											<td><c:out value="${articolo.prezzo}" />€</td>
											<td><img width="150" src="data:image/jpeg;base64,${articolo.immagine}"/></td>
										</tr>
									</c:if>
								</c:forEach>
							</tr>
						</c:forEach>
					</tbody>
				</table>
				</div>
			</c:forEach>
		</ul>
		</div>
	</c:if>

	<h2> RICERCA PER PAROLA CHIAVE </h2>
	<div class="w3-panel">
	<form action="GoToAcquistoPage" method="POST" class="ricerca">
		<input id="search" type="text" name="keyword" placeholder="inserisci parola chiave" required class="w3-input w3-border w3-animate-input" style="width:50%"> 
		<input id="submit" type="submit" value="Cerca" class="w3-button w3-black"> 
	</form>
	
	
	<!--  ERRORI DI RICERCA -->
	<h4>
		<c:if test = "${erroreNotFound != null}">  <!-- non ci sono aste aperte con keyword -->
			<span class="w3-tag w3-red"><c:out value="ATTENZIONE: ${erroreNotFound}"></c:out></span>
		</c:if>
		<c:if test = "${asteAperte == null}">
			<span class="w3-tag w3-red"><c:out value="ATTENZIONE: non ci sono aste aperte al momento.."></c:out></span>
		</c:if>
	</h4>
	</div>
	
	<!-- appena entro nella pagina mostro tutte le aste aperte!  -->
	<c:if test = "${asteAperteKeyword == null && asteAperte!=null}">
		<h2> ASTE APERTE </h2>
		<div class="w3-panel">
			<table  class="w3-table-all">
				<thead>
					<tr>
						<th>Codice</th>
						<th>prezzo iniziale</th>
						<th>rialzo minimo</th>
						<th>scadenza</th>
						<th>offerta massima </th>
						<th></th>
					</tr>
				</thead>
				<tbody>
					<c:forEach items="${asteAperte}" var="asta" varStatus="as">
						<tr>
							<c:url value="/GoToOffertaPage" var="offURL">
								<c:param name="idAsta" value="${asta.astaId}" />
							</c:url>
							<td><c:out value="${asta.astaId}"/></td>
							<td><c:out value="${asta.prezzoIniziale}"/>€</td>
							<td><c:out value="${asta.rialzoMinimo}"/>€</td>
							<td><c:out value="${asta.scadenza}"/></td>
							<td><c:out value="${asta.offertaMassima}"/>€</td>
							<td><a href="${offURL}"> CREA OFFERTA </a></td>
						</tr>
					</c:forEach>
				</tbody>
			</table>
			</div>
	</c:if>
	
	
	<c:if test = "${asteAperteKeyword != null}">
	<h2> ASTE APERTE con parola chiave </h2>
		<div class="w3-panel">
		<table  class="w3-table-all">
			<thead>
				<tr>
					<th>Codice</th>
					<th>prezzo iniziale</th>
					<th>rialzo minimo</th>
					<th>scadenza</th>
					<th>offerta massima </th>
					<th></th>
				</tr>
			</thead>
			<tbody>
				<c:forEach items="${asteAperteKeyword}" var="asta" varStatus="as">
					<tr>
						<c:url value="/GoToOffertaPage" var="offURL">
							<c:param name="idAsta" value="${asta.astaId}" />
						</c:url>
						<td><c:out value="${asta.astaId}"/></td>
						<td><c:out value="${asta.prezzoIniziale}"/>€</td>
						<td><c:out value="${asta.rialzoMinimo}"/>€</td>
						<td><c:out value="${asta.scadenza}"/></td>
						<td><c:out value="${asta.offertaMassima}"/>€</td>
						<td><a href="${offURL}"> CREA OFFERTA </a></td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
		</div>
	</c:if>

	<br>
	</div>
</body>
</html>