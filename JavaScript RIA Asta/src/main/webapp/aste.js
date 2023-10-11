 {
	//def. componenti della pagine
	let ricercaKeyword, personalMessage, paginaAcquisto, 
		paginaOfferta, paginaVendo,  dettaglioAsta,
		creaAsta, creaArticolo, idAstaCorrente ;
	
	let nomeUtente; 
	 		
	let pageOrchestrator = new PageOrchestrator();  
	
	//WINDOW: finestra del browser
	window.addEventListener("load", () => {
		 if (sessionStorage.getItem("username") == null) {
	     	 window.location.href = "index.html";
	     } else {
			 pageOrchestrator.start();  //inizializza i componenti e li mostra nella pagina
			 pageOrchestrator.refresh();
		 }
	}, false);
	 
	//Serve per mostrare il messaggio di benvenuto all'utente
	function PersonalMessage(_username) {
	    this.username = _username;
	    nomeUtente=_username;
	    this.show = function(messagecontainer) {
	      messagecontainer.textContent = this.username;
	    }
	}
	 
	 
	function PaginaAcquisto(obj){ //dichiarazione della funzione
		this.alert = obj['alert'];
		this.offerteContainer = obj['offerteContainer'];
		this.asteAperteBody = obj['asteAperteBody'];
		this.asteAperte = obj['asteAperte'];
		this.asteAperteAcq = obj['asteAperteAcq'];
		
		var idAste;
		
		this.show = function(){ //espressione che ritorna una funzione  -> assegna a una variabile una funzione
			var self = this;
			
			//Mostra le  ASTE APERTE SU CUI L'UTENTE HA CLICCATO
			idAste = JSON.parse(localStorage.getItem(nomeUtente+"_list")); 
			var idAsteDate = JSON.parse(localStorage.getItem(nomeUtente+"_listDate")); //array
			if(idAste!=null){
				for(var i=0; i<idAste.length; i++){
					if(new Date(idAsteDate[i])<new Date()){ //asta tempo scaduto
						idAste.splice(i, 1); //rimuove l'asta dall'elenco
						idAsteDate.splice(i, 1);
					}
				}
				localStorage.setItem(nomeUtente+"_list", JSON.stringify(idAste)); //memorizza l'elenco
				localStorage.setItem(nomeUtente+"_listDate", JSON.stringify(idAsteDate));
			}
			
			
			makeCall("GET", "GoToAcquistoPage", null, 
				function(req){ //FUNZIONE DI CALLBACK: AGGIORNA LA PAGINA
					if(req.readyState == 4){ //XMLHttpRequest proprietà readyState -> stato della richiesta = 4 è DONE: richiesta completata
						var message = req.responseText; //testo risposta dal server
						if(req.status == 200){
							var data = JSON.parse(req.responseText);  //req è XMLHttpRequest ->  responseText: risposta dal server (in formato JSON)
							var offerteToShow = data.offerteAggiudicate;
							var articoliToShow = data.articoliAggiudicati;
							var asteToShow = data.asteAperte;
					
							self.update(offerteToShow,articoliToShow);
							self.showAsteVisitate(asteToShow);
							
						} else if(req.status == 403 ||req.status == 412){ // forbidden e precondition failed (sessione inesistente)
							window.location.href = "index.html"; //reindirizzamento dell'utente alla pagina di login
							window.sessionStorage.removeItem('username');

						} else {
							self.alert.textContent = message;
						}
					}
				}
			);
		};
		
		//OFFERTE AGGIUDICATE
		this.update = function(offerteList, articoliList){
			var listRow, idCell, finalPriceCell, tab;
			var riga, codCell, nomeCell,descCell, priceCell, immCell, imageCell;
			var head, cod, nome, desc, price, image;
			
			this.offerteContainer.innerHTML = ""; //Svuoto la tabella per poterla aggiornare
			var self = this; //l'aggiornamento 
			if(offerteList==null){
				var mess = document.createElement("h4");
				mess.className = "w3-text-red";
				mess.textContent = "Non ci sono offerte aggiudicate!";
				self.offerteContainer.appendChild(mess);
			} else if(articoliList==null){
				var mess = document.createElement("h4");
				mess.className = "w3-text-red";
				mess.textContent = "Non ci sono articoli!";
				self.offerteContainer.appendChild(mess);
			} else{ 	//costruzione di lista + tabella
				offerteList.forEach(function(offerta, i){				
					listRow = document.createElement("li");
					listRow.className = "w3-panel w3-light-grey";
					
					idCell = document.createElement("div");
					idCell.textContent = "Asta aggiudicata numero: " + offerta.astaId;
					listRow.appendChild(idCell);
	
					finalPriceCell = document.createElement("div");
					finalPriceCell.textContent = "Prezzo finale: " +offerta.prezzoOfferto + "€";
					listRow.appendChild(finalPriceCell);
					
					div = document.createElement("div");
					div.className = "w3-panel";
					
					tab = document.createElement("table");
					tab.className="w3-table-all";
					
					head = document.createElement("tr");
					cod = document.createElement("th");
					cod.textContent = "Codice";
					head.appendChild(cod);
					nome = document.createElement("th");
					nome.textContent = "Nome";
					head.appendChild(nome);
					desc = document.createElement("th");
					desc.textContent = "Descrizione";
					head.appendChild(desc);
					price = document.createElement("th");
					price.textContent = "Prezzo";
					head.appendChild(price);
					image = document.createElement("th");				
					image.textContent = "Immagine";
					head.appendChild(image);
	
					tab.appendChild(head);
					
					var articoli = articoliList[i];
					articoli.forEach(function(articolo){
						riga = document.createElement("tr");
						codCell = document.createElement("td");
						codCell.textContent = articolo.codice;
						riga.appendChild(codCell);
						nomeCell = document.createElement("td");
						nomeCell.textContent = articolo.nome;
						riga.appendChild(nomeCell);
						descCell = document.createElement("td");
						descCell.textContent = articolo.descrizione;
						riga.appendChild(descCell);
						priceCell = document.createElement("td");
						priceCell.textContent = articolo.prezzo+"€";
						riga.appendChild(priceCell);
						immCell = document.createElement("td");
						riga.appendChild(immCell);
						imageCell = document.createElement("img");
						imageCell.src =  "data:image/jpeg;base64," + articolo.immagine;
						imageCell.width = "150";
						immCell.appendChild(imageCell);
						
						tab.appendChild(riga);
					}); 
					div.appendChild(tab);	
					self.offerteContainer.appendChild(listRow);
					self.offerteContainer.appendChild(div);
				});
				self.offerteContainer.style.visibility = "visible";
			}
		}
		
		//ASTE VISITATE
		this.showAsteVisitate = function(asteAperte){
			var self = this;
			this.asteAperteBody.innerHTML="";
						
			asteAperte.forEach(function(asta){
				if(idAste!=null){ 
					var stampa = false; 
					for(var i=0; i<idAste.length; i++){
						if(asta.astaId == idAste[i]){ //se l'asta è tra quelle visitate verrà "stampata"
							stampa = true;
						}
					}
					if(stampa==true){
						var riga = document.createElement("tr");
					
						var codiceCell = document.createElement("td");
						codiceCell.textContent = asta.astaId;
						riga.appendChild(codiceCell);
						
						var prezzoInizialeCell = document.createElement("td");
						prezzoInizialeCell.textContent = asta.prezzoIniziale+"€";
						riga.appendChild(prezzoInizialeCell);
					
						var rialzoMinimoCell = document.createElement("td");
						rialzoMinimoCell.textContent = asta.rialzoMinimo+"€";
						riga.appendChild(rialzoMinimoCell);
						
						var scadenzaCell = document.createElement("td");
						scadenzaCell.textContent = asta.scadenza;
						riga.appendChild(scadenzaCell);
						
						var offertaMaxCell = document.createElement("td");
						offertaMaxCell.textContent = asta.offertaMassima+"€";
						riga.appendChild(offertaMaxCell);
						
						var linkcell = document.createElement("td");
					    var anchor = document.createElement("a");
				        linkcell.appendChild(anchor);
				        linkText = document.createTextNode("Vedi dettaglio offerta");
				        anchor.appendChild(linkText);
				        anchor.setAttribute('astaId', asta.astaId); // setto attributo dell'ancora con id dell'asta di questa riga
				        anchor.addEventListener("click", (e) => {
				     		paginaOfferta.show(e.target.getAttribute("astaId")); // the list must know the details container
				      		setStorageEvent(nomeUtente, "acquisto");
				      		updateStorageList(nomeUtente, e.target.getAttribute("astaId")); //aggiorna la lista di aste su cui l'utente ha cliccato
				      		idAstaCorrente = asta.astaId;
				      		
				        }, false);
			       		anchor.href = "#id_pagOff";
				        riga.appendChild(linkcell);
						
						self.asteAperteBody.appendChild(riga);
					}
				}
			});
		}
		
		//Bottone "vendo"
		this.registerEvents= function(orchestrator){
			document.getElementById("id_goToVendo").addEventListener('click', ()=>{
				this.alert.innerHTML = "";
				setStorageEvent(nomeUtente, "acquisto");
				showVendo(orchestrator);
			});
		}
	}
	
	function RicercaKeyword(obj){
		this.alert = obj['alert'];
		this.ricercaForm = obj['ricercaForm'];
		this.keyword = obj['keyword'];
		this.asteAperteKeywordBody = obj['asteAperteKeywordBody'];
		this.asteAperteKeyword = obj['asteAperteKeyword'];
		this.asteAperteKeywordAcq = obj['asteAperteKeywordAcq'];
		this.asteAperteKeywordNotFound = obj['asteAperteKeywordNotFound'];
		
		this.registerEvents = function() {
			this.ricercaForm.querySelector("input[type='button']").addEventListener('click', (e)=>{ //funzione di gestione evento (handle event)
				if(this.keyword.value==null || this.keyword.value.length==0){  //controllo parametri
					this.asteAperteKeyword.style.display = "none";
				  	this.asteAperteKeywordBody.innerHTML="";
	               	this.alert.textContent = "Inserisci una chiave di ricerca valida"; //messaggio di errore
				} else{
					var form = e.target.closest("form");
					this.alert.innerHTML = "";
		        	if (form.checkValidity()) {
		         		var self = this;
		         		makeCall("POST", 'RicercaKeyword', form,
				            function(req) { //FUNZIONE DI CALLBACK 
				            
				            
				              if (req.readyState == 4) { //la richiesta è completata
					          	if (req.status == 200) { //richiesta completata con successo					
					            	var asteAperteKeyword = JSON.parse(req.responseText);
									self.update(asteAperteKeyword);
									setStorageEvent(sessionStorage.getItem('username'), "acquisto");
									
					            } else if (req.status == 403 || req.status == 412) { // errore di autorizzazione --> rimanda a index
				                	window.location.href = "index.html"; //reindirizzamento dell'utente a una risorsa
									window.sessionStorage.removeItem('username');
	
				                }else {
									self.asteAperteKeyword.style.display = "none";
								  	self.asteAperteKeywordBody.innerHTML="";
								    var message = req.responseText; //messaggio di risposta
					               	self.alert.textContent = message; //messaggio di errore
					            }
				              }
				              
				        	});
		         	} else{
						 form.reportValidity(); //se c'è un ERRORE nell'input --> mostra errore
					}
				}
			});
			
		}	
	
		// tabella di ASTE APERTE con KEYWORD		
		this.update = function(asteAperteKeyword){
			var self = this;
			this.asteAperteKeywordBody.innerHTML="";
			if(asteAperteKeyword==null){
				self.asteAperteKeywordNotFound.style.display="block";
				self.asteAperteKeywordNotFound.textContent = "Non ci sono aste aperte!";
				self.asteAperteKeywordAcq.style.visibility ="hidden";
				
			} else{
				self.asteAperteKeywordNotFound.innerHTML = "";
				self.asteAperteKeywordAcq.style.visibility ="visible";
				
				asteAperteKeyword.forEach(function(asta){
					var riga = document.createElement("tr");
					
					var codiceCell = document.createElement("td");
					codiceCell.textContent = asta.astaId;
					riga.appendChild(codiceCell);
					
					var prezzoInizialeCell = document.createElement("td");
					prezzoInizialeCell.textContent = asta.prezzoIniziale+"€";
					riga.appendChild(prezzoInizialeCell);
				
					var rialzoMinimoCell = document.createElement("td");
					rialzoMinimoCell.textContent = asta.rialzoMinimo+"€";
					riga.appendChild(rialzoMinimoCell);
					
					var scadenzaCell = document.createElement("td");
					scadenzaCell.textContent = asta.scadenza;
					riga.appendChild(scadenzaCell);
					
					var offertaMaxCell = document.createElement("td");
					offertaMaxCell.textContent = asta.offertaMassima+"€";
					riga.appendChild(offertaMaxCell);
					
					var linkcell = document.createElement("td");
				    var anchor = document.createElement("a");
			        linkcell.appendChild(anchor);
			        linkText = document.createTextNode("Vedi dettaglio offerta");
			        anchor.appendChild(linkText);
			        anchor.setAttribute('astaId', asta.astaId); // setto attributo dell'ancora con id dell'asta di questa riga
			        anchor.addEventListener("click", (e) => { //click su un'asta aperta -> mostra la pagina offerta
			     		paginaOfferta.show(e.target.getAttribute("astaId")); // the list must know the details container
			      		setStorageEvent(nomeUtente, "acquisto");
			      		updateStorageList(nomeUtente, e.target.getAttribute("astaId")); //aggiorna la lista di aste su cui l'utente ha cliccato
						idAstaCorrente = asta.astaId;
			        }, false);
			        anchor.href = "#id_pagOff";
			        riga.appendChild(linkcell);
					
					self.asteAperteKeywordBody.appendChild(riga);
				});
			}
		}	
		
	}
	
	function PaginaOfferta(obj){
		this.alert = obj['alert'];
		this.pagOff = obj['pagOff'];
		this.offertePagOff = obj['offertePagOff'];
		this.listaOffertePagOff = obj['listaOffertePagOff'];
		this.listaOffertePagOffBody = obj['listaOffertePagOffBody'];
		this.inserisciOfferta = obj['inserisciOfferta'];
		this.inserisciOffertaForm = obj['inserisciOffertaForm'];
		this.offertaInserita = obj['offertaInserita'];
		this.articoliPagOff = obj['articoliPagOff'];
		this.articoliTabPagOff = obj['articoliTabPagOff'];
		this.articoliTabPagOffBody = obj['articoliTabPagOffBody'];
		this.astaId = obj['astaId'];
		this.offertePagOffNotFound = obj['offertePagOffNotFound'];
		this.articoliPagOffNotFound = obj['articoliPagOffNotFound'];

		//mostra la pagina offerta
		this.show = function(idAsta){
			this.pagOff.style.display = "block";
			
			this.alert.innerHTML = "";
					
			this.astaId.textContent = idAsta;
			this.astaId.style.visibility = "hidden";
			
			this.showOfferte(idAsta);
			this.showArticoli(idAsta);
		}
	
		//nasconde la pagina
		this.reset = function(){
			this.pagOff.style.display = "none";
			this.articoliTabPagOffBody.innerHTML = "";
			this.listaOffertePagOffBody.innerHTML = "";
		}
		
		
		//crea tabella con gli articoli dell'asta
		this.showArticoli = function(idAsta){
			var self = this;
			makeCall("GET", "GetArticoliAsta?idAsta=" + idAsta, null, 
				function(req){
					if(req.readyState == 4){ //XMLHttpRequest proprietà readyState -> stato della richiesta = 4 è DONE: richiesta completata
						var message = req.responseText; //testo risposta dal server
						if(req.status == 200){
							var articoliAsta = JSON.parse(req.responseText);
								
							if(articoliAsta == null){ 
								self.articoliPagOffNotFound.textContent = "Non ci sono articoli";
							    self.articoliPagOffNotFound.style.display="block";
								self.articoliTabPagOff.style.visibility ="hidden";
								
							} else{
								self.articoliPagOffNotFound.innerHTML = "";
								self.articoliTabPagOff.style.visibility ="visible";
							
								//riempire tabella degli articoli 
								self.articoliTabPagOffBody.innerHTML="";
								articoliAsta.forEach(function(articolo){
									var riga = document.createElement("tr");
									
									var codiceCell = document.createElement("td");
									codiceCell.textContent = articolo.codice;
									riga.appendChild(codiceCell);
									
									var nomeCell = document.createElement("td");
									nomeCell.textContent = articolo.nome;
									riga.appendChild(nomeCell);
								
									var descrizioneCell = document.createElement("td");
									descrizioneCell.textContent = articolo.descrizione;
									riga.appendChild(descrizioneCell);
									
									var prezzoCell = document.createElement("td");
									prezzoCell.textContent = articolo.prezzo+"€";
									riga.appendChild(prezzoCell);
									
									immCell = document.createElement("td");
									riga.appendChild(immCell);
									imageCell = document.createElement("img");
									imageCell.src =  "data:image/jpeg;base64," + articolo.immagine;
									imageCell.width = "150";
									immCell.appendChild(imageCell);
									
									self.articoliTabPagOffBody.appendChild(riga);
								});
								self.articoliTabPagOffBody.style.visibility = "visible";					
							}
							
						} else if(req.status == 403 ||req.status == 412){
							window.location.href = "index.html"; //reindirizzamento dell'utente a una risorsa
							window.sessionStorage.removeItem('username');
							
						} else {
							self.alert.textContent = message;
							self.pagOff.style.display = "none";

						}
					}
				}
			);	
		}
		
		//crea lista di offerte dell'asta
		this.showOfferte = function(idAsta){
			var self = this;
			makeCall("GET", "GetOfferteAsta?idAsta=" + idAsta, null, 
				function(req){
					if(req.readyState == 4){ //XMLHttpRequest proprietà readyState -> stato della richiesta = 4 è DONE: richiesta completata
						var message = req.responseText; //testo risposta dal server
						if(req.status == 200){
							var data = JSON.parse(req.responseText);
							var offerteAsta = data.offerteAsta;
							var asta = data.asta;
							
							self.listaOffertePagOffBody.innerHTML = "";
							
							//mostra il prezzo minimo che l'utente può offrire
							var rialzoMin = document.getElementById("id_rialzoMinimoOff");
							rialzoMin.innerHTML="";
							if(asta.offertaMassima==0){
								var int = asta.prezzoIniziale + asta.rialzoMinimo;
								rialzoMin.textContent = "NB: prezzo minimo: " + int + "€";
							} else{
								var int = asta.offertaMassima + asta.rialzoMinimo;
								rialzoMin.textContent = "NB: prezzo minimo: " + int + "€";
							}
							
							 
							if(offerteAsta == null || offerteAsta == undefined){ 
								self.offertePagOffNotFound.textContent = "Non ci sono offerte";
							    self.offertePagOffNotFound.style.display="block";
								self.listaOffertePagOff.style.visibility ="hidden";
								
							} else{
								self.offertePagOffNotFound.innerHTML = "";
								self.listaOffertePagOff.style.visibility ="visible";
											
								//riempire tabella delle offerte 
								self.listaOffertePagOffBody.innerHTML="";
								offerteAsta.forEach(function(offerta){
									var riga = document.createElement("tr");
									
									var codiceCell = document.createElement("td"); 
									codiceCell.textContent = offerta.userId;
									riga.appendChild(codiceCell);
									
									var prezzoCell = document.createElement("td");
									prezzoCell.textContent = offerta.prezzoOfferto+"€";
									riga.appendChild(prezzoCell);
								
									var dataOraCell = document.createElement("td");
									dataOraCell.textContent = offerta.dataOra;
									riga.appendChild(dataOraCell);			
									
									self.listaOffertePagOffBody.appendChild(riga);
								});
								self.listaOffertePagOffBody.style.visibility = "visible";	
							}				
						} else if(req.status == 403 ||req.status == 412){
							window.location.href = "index.html"; 
							window.sessionStorage.removeItem('username');
							
						} else {
							self.alert.textContent = message;

						}
					}
				}
			);	
		}
		
		//form di inserimento offerta
		this.registerEvents = function() { 
			this.inserisciOffertaForm.querySelector("input[type='button']").addEventListener('click', (e)=>{
				var form = e.target.closest("form");
				this.alert.innerHTML = "";
				if(this.offertaInserita.value==null || this.offertaInserita.value.length==0 || isNaN(this.offertaInserita.value)){
				  	this.alert.textContent = "Inserisci un'offerta valida!"; //messaggio di errore
				} else{
		        	if (form.checkValidity()) {
		         		var self = this;
		         		idAsta = idAstaCorrente; //prendo l'attributo
		         		form.querySelector("input[type = 'hidden']").value = idAsta; //setto valore del form
	         			makeCall("POST", 'CreateOfferta', form,
				            function(req) {
				              if (req.readyState == 4) { //la richiesta è completata
					          	if (req.status == 200) { //richiesta completata con successo
									self.showOfferte(idAstaCorrente); //aggiorna le offerte 
									paginaAcquisto.show(); //ricarica la pagina per aggiornare la tabella delle aste
									setStorageEvent(nomeUtente, "acquisto");
									
					            } else if (req.status == 403 ||req.status == 412) { // errore di autorizzazione --> rimanda a index
				               		window.location.href = "index.html"; //reindirizzamento dell'utente a una risorsa
									window.sessionStorage.removeItem('username');
	
				                }else {
								    var message = req.responseText; //messaggio di risposta
					               	self.alert.textContent = message; //messaggio di errore
					            }
				              }
				        	});
		         	} else{
						 form.reportValidity(); //se c'è un ERRORE nell'input
					}
				}
			});
		}	
	}
	
	function PaginaVendo(obj){
		this.alert = obj['alert'];
		this.pagVendo = obj['pagVendo'];
		this.asteNonChiuseList = obj['asteUserNonChiuseVendo'];
		this.asteChiuseList = obj['asteUserChiuseVendo'];
		this.articoliDisponibili = obj['articoliDisponibili'];
		this.articoloDisponibile = obj['articoloDisponibile'];
		this.articoloDispNome = obj['articoloDispNome'];
		this.asteNonChiuseNotFound = obj['asteNonChiuseNotFound'];
		this.asteChiuseNotFound = obj['asteChiuseNotFound'];
		
		//mostra la pagina vendo 
		this.show = function(){
			var self = this;
			makeCall("GET", "GoToVendoPage", null, 
				function(req){
					if(req.readyState == 4){ //XMLHttpRequest proprietà readyState -> stato della richiesta = 4 è DONE: richiesta completata
						var message = req.responseText; //testo risposta dal server
						if(req.status == 200){
							var data = JSON.parse(req.responseText);
							var tempoMancanteNonChiuse = data.tempoMancanteNonChiuse;
							var articoliAsteNonChiuse = data.articoliAsteNonChiuse;
							var articoliAsteChiuse = data.articoliAsteChiuse;
							var asteNonChiuse = data.asteNonChiuse;
							var asteChiuse = data.asteChiuse;
							var articoliDisponibili = data.articoliDisponibiliList;

							self.showAsteNonChiuse(asteNonChiuse, tempoMancanteNonChiuse, articoliAsteNonChiuse);
							self.showAsteChiuse(asteChiuse, articoliAsteChiuse);
							self.updateArticoliDisponibili(articoliDisponibili);

						} else if(req.status == 403 ||req.status == 412){
							window.location.href = "index.html"; //reindirizzamento dell'utente a una risorsa
							window.sessionStorage.removeItem('username');
							
						} else {
							self.alert.textContent = message;
						}
					}
				}
			);
		};
		
		//tabella aste non chiuse
		this.showAsteNonChiuse = function(asteNonChiuse, tempoMancanteNonChiuse, articoliAsteNonChiuse){
			var listRow, idCell, tempoMancanteCell, scadenzaCell, offertaMaxCell, tab;
			var riga, codCell, nomeCell;
			var head, cod, nome;
			
			//Svuoto la tabella per poterla aggiornare
			this.asteNonChiuseList.innerHTML = "";
			var self = this; 		
			if(asteNonChiuse==null){
				self.asteNonChiuseNotFound.style.display = "block";
				self.asteNonChiuseNotFound.textContent = "Non ci sono aste non chiuse";
				self.asteNonChiuseList.visibility = "hidden";
			} else{
				self.asteNonChiuseNotFound.innerHTML = "";
				self.asteNonChiuseList.visibility = "visible";
				asteNonChiuse.forEach(function(asta, i){//Voglio iterare su tutte le offerte				
					listRow = document.createElement("li");
					listRow.className = "w3-panel w3-light-grey";
							
					idCell = document.createElement("div");
	 				var anchor = document.createElement("a");
			        idCell.appendChild(anchor);
			        linkText = document.createTextNode("Id asta: " + asta.astaId);
			        anchor.appendChild(linkText);
			        anchor.className = "w3-text-blue w3-large";
			        anchor.setAttribute('astaId', asta.astaId); 
			        anchor.addEventListener("click", (e) => {  //click su un'asta non chiusa
			            var inputHidden = document.getElementById('id_astaId'); 
						inputHidden.setAttribute('idAsta',  asta.astaId); 
			            dettaglioAsta.show(e.target.getAttribute("astaId")); 
			            setStorageEvent(nomeUtente, "acquisto");

			        }, false);
			        anchor.href = "#id_dettaglioAsta";
		        	listRow.appendChild(idCell);
	
					tempoMancanteCell = document.createElement("div");
					tempoMancanteCell.textContent = "Tempo mancante: " + tempoMancanteNonChiuse[i][0] + "giorni e " + tempoMancanteNonChiuse[i][1] + "ore " ;
					listRow.appendChild(tempoMancanteCell);
					
					scadenzaCell = document.createElement("div");
					scadenzaCell.textContent = "Scadenza: " + asta.scadenza;
					listRow.appendChild(scadenzaCell);
					
					offertaMaxCell = document.createElement("div");
					offertaMaxCell.textContent = "Offerta massima: " + asta.offertaMassima+"€";
					listRow.appendChild(offertaMaxCell);
					
					tab = document.createElement("table");
					tab.className="w3-table w3-white w3-bordered w3-border";
										
					head = document.createElement("tr");
					cod = document.createElement("th");
					cod.textContent = "Codice";
					head.appendChild(cod);
					nome = document.createElement("th");
					nome.textContent = "Nome";
					head.appendChild(nome);
					
					tab.appendChild(head);
					
					var articoli = articoliAsteNonChiuse[i];
					articoli.forEach(function(articolo){
						riga = document.createElement("tr");
						codCell = document.createElement("td");
						codCell.textContent = articolo.codice;
						riga.appendChild(codCell);
						nomeCell = document.createElement("td");
						nomeCell.textContent = articolo.nome;
						riga.appendChild(nomeCell);
						
						tab.appendChild(riga);
					}); 
					listRow.appendChild(tab);	
					self.asteNonChiuseList.appendChild(listRow);
				});
				self.asteNonChiuseList.style.visibility = "visible";
			
			}
		}
		
		//tabella aste chiuse 		
		this.showAsteChiuse = function(asteChiuse, articoliAsteChiuse){
			var listRow, idCell, scadenzaCell, offertaMaxCell, tab;
			var riga, codCell, nomeCell;
			var head, cod, nome;
			
			//Svuoto la tabella per poterla aggiornare
			this.asteChiuseList.innerHTML = "";
			var self = this; 
			if(asteChiuse==null){
				self.asteChiuseNotFound.style.display = "block";
				self.asteChiuseNotFound.textContent = "Non ci sono aste chiuse";
				self.asteChiuseList.visibility = "hidden";
			} else{
				self.asteChiuseNotFound.innerHTML = "";
				self.asteChiuseList.visibility = "visible";
				asteChiuse.forEach(function(asta, i){//Voglio iterare su tutte le offerte				
					listRow = document.createElement("li");
					listRow.className = "w3-panel w3-light-grey";

					idCell = document.createElement("div");
	 				var anchor = document.createElement("a");
			        idCell.appendChild(anchor);
			        linkText = document.createTextNode("Id asta: " + asta.astaId);
			        anchor.appendChild(linkText);
			        anchor.className = "w3-text-green w3-large";
			        anchor.setAttribute('astaId', asta.astaId); // set a custom HTML attribute
			        anchor.addEventListener("click", (e) => { //click su un'asta chiusa
			            var inputHidden = document.getElementById('id_astaId'); 
						inputHidden.setAttribute('idAsta',  asta.astaId); 
			            dettaglioAsta.show(e.target.getAttribute("astaId"), asta.chiusa); // the list must know the details container
			            setStorageEvent(nomeUtente, "acquisto");

			        }, false);
			        anchor.href = "#id_dettaglioAsta";
		        	listRow.appendChild(idCell);
					
					scadenzaCell = document.createElement("div");
					scadenzaCell.textContent = "Scadenza: " + asta.scadenza;
					listRow.appendChild(scadenzaCell);
					
					offertaMaxCell = document.createElement("div");
					offertaMaxCell.textContent = "Offerta massima: " + asta.offertaMassima+"€";
					listRow.appendChild(offertaMaxCell);
					
					
					tab = document.createElement("table");
					tab.className="w3-table w3-white  w3-bordered w3-border";

					head = document.createElement("tr");
					cod = document.createElement("th");
					cod.textContent = "Codice";
					head.appendChild(cod);
					nome = document.createElement("th");
					nome.textContent = "Nome";
					head.appendChild(nome);
					
					tab.appendChild(head);
					
					var articoli = articoliAsteChiuse[i];
					articoli.forEach(function(articolo){
						riga = document.createElement("tr");
						codCell = document.createElement("td");
						codCell.textContent = articolo.codice;
						riga.appendChild(codCell);
						nomeCell = document.createElement("td");
						nomeCell.textContent = articolo.nome;
						riga.appendChild(nomeCell);
						
						tab.appendChild(riga);
					}); 
					listRow.appendChild(tab);	
					self.asteChiuseList.appendChild(listRow);
				});
				self.asteChiuseList.style.visibility = "visible";
			}
		}
		
		// aggiorna gli articoli disponibili dell'utente (nel checkbox della creazione asta)
		this.updateArticoliDisponibili = function(articoliDisponibiliList){
			this.articoliDisponibili.innerHTML = "";
			this.articoloDisponibile.innerHTML = "";
			
			this.articoloDispNome.checked = false; 
			
			var self = this; 
			if(articoliDisponibiliList!=null){
				articoliDisponibiliList.forEach(function(articolo){		
					var checkbox = document.createElement('input');
					checkbox.type = 'checkbox';
					checkbox.value = articolo.codice;
					checkbox.name = "articolo";
					checkbox.id = "checkbox_" + articolo.codice;
				
					var nomeCheckbox = document.createElement('label');
	                
	                nomeCheckbox.appendChild(checkbox);
	                nomeCheckbox.appendChild(document.createTextNode("  " + articolo.nome))
						
					self.articoliDisponibili.appendChild(nomeCheckbox);
					self.articoliDisponibili.appendChild(document.createElement("br"));
					
				});
			} else if(articoliDisponibiliList==null){
				self.articoliDisponibili.textContent = "Non ci sono articoli disponibili, creali!";
			}
		}
		
		//BOTTONE ACQUISTO
		this.registerEvents= function(orchestrator) { 
			document.getElementById("id_goToAcquisto").addEventListener('click', (e)=>{
				this.alert.innerHTML = "";
				showAcquisto(orchestrator);
				setStorageEvent(nomeUtente, "acquisto");
			});
		}
	}
		
		
	function CreaArticolo(obj){
		this.alert = obj['alert'];
		this.creaArticolo = obj['creaArticolo'];
		this.creaArticoloForm = obj['creaArticoloForm'];
		this.nome = obj['nome'];
		this.descrizione = obj['descrizione'];
		this.prezzo = obj['prezzo'];
		this.dropzone = obj['dropzone'];
		this.immagine = obj['immagine'];
		
		// necessario per il caricamento del file
		this.uploadFile = function(){						
			this.dropzone.addEventListener('click', () =>{
				this.immagine.click();
			});
			this.immagine.addEventListener('click', () => {
			  this.immagine.click();
			});
		}
		
		// form di creazione articolo
		this.registerEvents= function(pagVendo) { 
			this.creaArticoloForm.querySelector("input[type='button']").addEventListener('click', (e)=>{ //LISTERNER (gestore evento)
				var form = e.target.closest("form");
				this.alert.innerHTML = "";
				//controllo parametri lato client
				if(this.nome.value==null || this.nome.value.length==0 || this.descrizione.value==null || this.descrizione.value.length==0 ||
						this.prezzo.value==null || this.prezzo.value.length==0 || isNaN(this.prezzo.value)){
				  	this.alert.textContent = "Inserisci dei parametri corretti"; //messaggio di errore
				} else{
		        	if (form.checkValidity()) {
		         		var self = this;
		         		this.uploadFile();
		         	
		         		const file = this.immagine.files[0];
	   					const formData = new FormData();
	  					formData.append('immagine', file);
		         		
		         		//per iterare col SERVER 
		         		makeCall("POST", 'CreaArticolo', form,
				            function(req) { //callback (interazione tra client e server asincrona)
				              if (req.readyState == 4) { //la richiesta è completata
					          	if (req.status == 200) { //richiesta completata con successo
									pagVendo.show();
									setStorageEvent(nomeUtente, "acquisto");
									
					            } else if (req.status == 403 ||req.status == 412) { // errore di autorizzazione --> rimanda a index
				               		window.location.href = "index.html"; //reindirizzamento dell'utente a una risorsa
									window.sessionStorage.removeItem('username');
				                }else {
								    var message = req.responseText; //messaggio di risposta
					               	self.alert.textContent = message; //messaggio di errore
					            }
				              }
				        	});
				        	
		         	} else{
						 form.reportValidity(); //se c'è un ERRORE nell'input
					}
				}
			});
		}
	}	
	
	
	function CreaAsta(obj){
		this.alert = obj['alert'];
		this.creaAsta = obj['creaAsta'];
		this.creaAstaForm = obj['creaAstaForm'];
		this.rialzoMinimo = obj['rialzoMinimo'];
		this.scadenza = obj['scadenza'];
		this.articoliDisponibili = obj['articoliDisponibili'];
		this.articoloDisponibile = obj['articoloDisponibile'];
		
		//form crea asta
		this.registerEvents = function(pagVendo) { 
			this.creaAstaForm.querySelector("input[type='button']").addEventListener('click', (e)=>{
				var form = e.target.closest("form");
				this.alert.innerHTML = "";
				if(this.scadenza.value==null || this.scadenza.value.length==0 || this.rialzoMinimo<=0 ||
						this.rialzoMinimo.value==null || this.rialzoMinimo.value.length==0){
				  	this.alert.textContent = "Inserisci dei parametri corretti"; //messaggio di errore
				} else{
		        	if (form.checkValidity()) {
		         		var self = this;
		         		//GESTIRE CHECKBOX PER PRENDERE ELEMENTI IN INPUT
		         		var checkboxList = document.getElementsByName('articolo');
	  					var selezionati = [];
						checkboxList.forEach(function(checkbox) {
						 	if (checkbox.checked) {
						    	selezionati.push(checkbox.value);
						    }
						});
		         		
		         		makeCall("POST", 'CreaAsta', form,
				            function(req) {
				              if (req.readyState == 4) { //la richiesta è completata
					          	if (req.status == 200) { //richiesta completata con successo
									pagVendo.show();
									setStorageEvent(nomeUtente, "vendo");
									
					            } else if (req.status == 403 ||req.status == 412) { // errore di autorizzazione --> rimanda a index
				               		window.location.href = "index.html"; //reindirizzamento dell'utente a una risorsa
									window.sessionStorage.removeItem('username');
	
				                }else {
								    var message = req.responseText; //messaggio di risposta
					               	self.alert.textContent = message; //messaggio di errore
					            }
				              }
				        	});
		         	} else{
						 form.reportValidity(); //se c'è un ERRORE nell'input
					}
				}
			});
		}
	}
	
		
	function DettaglioAsta(obj){
		this.alert = obj['alert'];
		this.pagDettAsta = obj['pagDettAsta'];
		this.isAperta = obj['aperta'];
		this.isChiusa = obj['chiusa'];	
		this.astaId	= obj['astaId'];
		this.idAstaDett = obj['idAstaDett'];
		this.prezzoIniziale = obj['prezzoIniziale'];
		this.rialzoMinimo = obj['rialzoMinimo'];
		this.scadenza = obj['scadenza'];
		this.offertaMassima = obj['offertaMassima'];
		this.chiusaBoolean = obj['chiusaBoolean'];
		this.articoliAsta = obj['articoliAsta'];
		this.articoliAstaTab = obj['articoliAstaTab'];
		this.articoliAstaBody = obj['articoliAstaBody'];
		this.offerteAsta = obj['offerteAsta'];
		this.offerteAstaTab = obj['offerteAstaTab'];
		this.offerteAstaBody = obj['offerteAstaBody'];
		this.aggiudicatario = obj['aggiudicatario'];
		this.prezzoFinale = obj['prezzoFinale'];
		this.indirizzo = obj['indirizzo'];
		this.aggNotFound = obj['aggNotFound'];
		this.offerteDettNotFound = obj['offerteDettNotFound'];
		
		//nasconde la pagina di dettaglio asta
		this.reset = function(){
			this.pagDettAsta.style.display = "none";
			this.articoliAstaBody.innerHTML = "";
			this.offerteAstaBody.innerHTML = "";
			this.isAperta.style.display = "none";
			this.isChiusa.style.display = "none";
		}
		
		//mostra la pagina: asta chiusa o aperta?
		this.show = function(idAsta, chiusa){
			this.pagDettAsta.style.display = "block";
			this.articoliAstaBody.innerHTML = "";
			this.offerteAstaBody.innerHTML = "";
			
			this.idAstaDett.innerHTML = "";
			this.prezzoIniziale.innerHTML = "";
			this.rialzoMinimo.innerHTML = "";
			this.scadenza.innerHTML = "";
			this.offertaMassima.innerHTML = "";
			this.chiusaBoolean.innerHTML = "";
			
			this.alert.innerHTML="";
			
			this.astaId.textContent = idAsta;
			this.astaId.style.visibility = "hidden";
			
			if(chiusa){ //l'asta è chiusa
				this.isAperta.style.display = "none";//nascondo aperta
				this.isChiusa.style.display = "block";//mostro chiusa
				this.showChiusa(idAsta);
				
			} else if(!chiusa){ //l'asta è non chiusa, aperta
				this.isChiusa.style.display = "none";
				this.isAperta.style.display = "block";
				this.showAperta(idAsta);
			}
			
			//mostro articoli asta!
			var self = this;
			makeCall("GET", "GetDettaglioAsta?idAsta=" + idAsta, null, 
				function(req){
					if(req.readyState == 4){ //XMLHttpRequest proprietà readyState -> stato della richiesta = 4 è DONE: richiesta completata
						var message = req.responseText; //testo risposta dal server
						if(req.status == 200){
							var articoliAsta = JSON.parse(req.responseText);
													
							//riempire tabella degli articoli 
							self.articoliAstaBody.innerHTML="";
							articoliAsta.forEach(function(articolo){
								var riga = document.createElement("tr");
								
								var codiceCell = document.createElement("td");
								codiceCell.textContent = articolo.codice;
								riga.appendChild(codiceCell);
								
								var nomeCell = document.createElement("td");
								nomeCell.textContent = articolo.nome;
								riga.appendChild(nomeCell);
							
								var descrizioneCell = document.createElement("td");
								descrizioneCell.textContent = articolo.descrizione;
								riga.appendChild(descrizioneCell);
								
								var prezzoCell = document.createElement("td");
								prezzoCell.textContent = articolo.prezzo+"€";
								riga.appendChild(prezzoCell);
								
								immCell = document.createElement("td");
								riga.appendChild(immCell);
								imageCell = document.createElement("img");
								imageCell.src =  "data:image/jpeg;base64," + articolo.immagine;
								imageCell.width = "150";
								immCell.appendChild(imageCell);
								
								
								self.articoliAstaBody.appendChild(riga);
							});
							self.articoliAstaBody.style.visibility = "visible";	
											
							
						} else if(req.status == 403 ||req.status == 412){
							window.location.href = "index.html"; //reindirizzamento dell'utente a una risorsa
							window.sessionStorage.removeItem('username');
							
						} else {
							self.alert.textContent = message;
						}
					}
				});		
		}
		
		//mostra la pagina quando l'asta è chiusa
		this.showChiusa = function(idAsta){
			var self = this;
			makeCall("GET", "GetAstaChiusa?idAsta=" + idAsta, null, 
				function(req){
					if(req.readyState == 4){ //XMLHttpRequest proprietà readyState -> stato della richiesta = 4 è DONE: richiesta completata
						var message = req.responseText; //testo risposta dal server
						if(req.status == 200){
							var data = JSON.parse(req.responseText);
							var asta = data.asta;
							var aggiudicatario = data.aggiudicatario;
							var offerta = data.offerta;
							
							self.aggiudicatario.innerHTML = "";
							self.prezzoFinale.innerHTML = "";
							self.indirizzo.innerHTML = "";
							
							//riempire lista dati asta
							self.idAstaDett.textContent = "Id dell'asta: " +asta.astaId;
							self.prezzoIniziale.textContent = "Prezzo iniziale: "+asta.prezzoIniziale+"€";
							self.rialzoMinimo.textContent = "Rialzo minimo: " + asta.rialzoMinimo+"€";
							self.scadenza.textContent = "Scadenza: " + asta.scadenza;
							self.offertaMassima.textContent = "Offerta massima: " + asta.offertaMassima+"€";
							self.chiusaBoolean.textContent = "CHIUSA";
												
							if(aggiudicatario == null || asta==null ||  offerta==null){
								self.aggNotFound.innerHTML="";
								self.aggNotFound.style.display = "block";
								self.aggNotFound.textContent = "Nessuno si è aggiudicato l'asta :( "
								self.aggiudicatario.style.visibility = "hidden";
								self.indirizzo.style.visibility = "hidden";
								self.prezzoFinale.style.visibility = "hidden";
							} else{
								//dati aggiudicatario, prezzo finale e indirizzo
								self.aggNotFound.innerHTML="";
								self.aggNotFound.textContent = "Dati utente aggiudicatario:"

								self.aggiudicatario.style.visibility = "visible";
								self.indirizzo.style.visibility = "visible";
								self.prezzoFinale.style.visibility = "visible";
								self.aggiudicatario.textContent = "Nome aggiudicatario: " + aggiudicatario.nome;
								self.indirizzo.textContent = "Indirizzo fisso di spedizione: " + aggiudicatario.indirizzo;
								self.prezzoFinale.textContent = "Prezzo finale: " + offerta.prezzoOfferto+"€";
							}
							
						} else if(req.status == 403 ||req.status == 412){
							window.location.href = "index.html"; //reindirizzamento dell'utente a una risorsa
							window.sessionStorage.removeItem('username');

						} else {
							self.aggiudicatario.innerHTML = "";
							self.prezzoFinale.innerHTML = "";
							self.indirizzo.innerHTML = "";
							
							self.alert.textContent = message;
						}
					}
				});		
		}
		
		//pagina quando l'asta è aperta
		this.showAperta = function(idAsta){
			var self = this;
			makeCall("GET", "GetAstaAperta?idAsta=" + idAsta, null, 
				function(req){
					if(req.readyState == 4){ //XMLHttpRequest proprietà readyState -> stato della richiesta = 4 è DONE: richiesta completata
						var message = req.responseText; //testo risposta dal server
						if(req.status == 200){
							var data = JSON.parse(req.responseText);
							var offerteAsta = data.offerteAsta;
							var nome = data.nomeUtente;
							var asta = data.asta;
																		
							//riempire lista dati asta
							self.idAstaDett.textContent = "Id dell'asta: " +asta.astaId;
							self.prezzoIniziale.textContent = "Prezzo iniziale: "+asta.prezzoIniziale+"€";
							self.rialzoMinimo.textContent = "Rialzo minimo: " + asta.rialzoMinimo+"€";
							self.scadenza.textContent = "Scadenza: " + asta.scadenza;
							self.offertaMassima.textContent = "Offerta massima: " + asta.offertaMassima+"€";
							self.chiusaBoolean.textContent = "APERTA";
							
							//tab offerte
							self.offerteAstaBody.innerHTML = "";
							if(offerteAsta==null){
								self.offerteDettNotFound.textContent = "Non ci sono offerte!";
								self.offerteDettNotFound.style.display = "block";
								self.offerteAstaTab.style.visibility = "hidden";
								
							} else{
								self.offerteDettNotFound.innerHTML="";
								self.offerteAstaTab.style.visibility = "visible";
								
								offerteAsta.forEach(function(offerta, i){//Voglio iterare su tutte le offerte				
									var riga = document.createElement("tr");
								
									var nomeCell = document.createElement("td");
									nomeCell.textContent = nome[i];
									riga.appendChild(nomeCell);
									
									var prezzoCell = document.createElement("td");
									prezzoCell.textContent = offerta.prezzoOfferto+"€";
									riga.appendChild(prezzoCell);
								
									var dataOraCell = document.createElement("td");
									dataOraCell.textContent = offerta.dataOra;
									riga.appendChild(dataOraCell);
									
									self.offerteAstaBody.appendChild(riga);											
								});
								self.offerteAstaBody.style.visibility = "visible";	
							}
																		
							
						} else if(req.status == 403 ||req.status == 412){
							window.location.href = "index.html"; //reindirizzamento dell'utente a una risorsa
							window.sessionStorage.removeItem('username');

						} else {
							self.alert.textContent = message;
						}
					}
				});		
		}
		
		//bottone di chiudi asta (quando asta è aperta)		
		this.registerEvents = function() { 
			this.isAperta.querySelector("input[type='button']").addEventListener('click', ()=>{
				this.alert.innerHTML = "";
	         	var self = this;
	         	var valoreAstaId = this.astaId.textContent;
				
	         	makeCall("POST", 'ChiudiAsta?idAsta='+valoreAstaId, null,
		        function(req) {
	              if (req.readyState == 4) { //la richiesta è completata
		          	if (req.status == 200) { //richiesta completata con successo
						self.show(valoreAstaId, true);
						paginaVendo.show();
						setStorageEvent(nomeUtente, "acquisto");
						

		            } else if (req.status == 403 ||req.status == 412) { // errore di autorizzazione --> rimanda a index
	               		window.location.href = "index.html"; //reindirizzamento dell'utente a una risorsa
						window.sessionStorage.removeItem('username');

	                }else {
						var message = req.responseText; //messaggio di risposta
		               	self.alert.textContent = message; //messaggio di errore
		            }
	              }
	        	});
			});
		}	
		
	}

	
	function PageOrchestrator(){
		var alertContainer = document.getElementById("id_alert");
		
		this.start = function(){			
			personalMessage = new PersonalMessage(sessionStorage.getItem('username'));
			
			
			paginaAcquisto = new PaginaAcquisto({  //crea un oggetto con i seguenti attributi
												alert: alertContainer, 
												offerteContainer: document.getElementById("id_offerteAggAcq"),
												asteAperteBody: document.getElementById("id_astaAperteKeywordAcqBody"),
												asteAperte: document.getElementById("id_asteAperteKeyword"),
												asteAperteAcq: document.getElementById("id_asteAperteKeywordAcq")});
			
			ricercaKeyword = new RicercaKeyword({
									alert: alertContainer,
									ricercaForm: document.getElementById("id_ricercaKeyword"),
									keyword: document.getElementById("id_keyword"),
									asteAperteKeywordBody: document.getElementById("id_astaAperteKeywordAcqBody"),
									asteAperteKeyword: document.getElementById("id_asteAperteKeyword"),
									asteAperteKeywordAcq: document.getElementById("id_asteAperteKeywordAcq"),
									asteAperteKeywordNotFound: document.getElementById("id_asteAperteNotFound")});
						
			paginaOfferta = new PaginaOfferta({
										alert: document.getElementById("id_alertCreaOff"),
										pagOff: document.getElementById("id_pagOff"),
										offertePagOff: document.getElementById("id_offertePagOff"),
										listaOffertePagOff: document.getElementById("id_listaOffertePagOff"),
										listaOffertePagOffBody: document.getElementById("id_listaOffertePagOffBody"),
										inserisciOfferta: document.getElementById("id_inserisciOfferta"),
										inserisciOffertaForm: document.getElementById("id_inserisciOffertaForm"),
										offertaInserita: document.getElementById("id_offertaInserita"),
										articoliPagOff: document.getElementById("id_articoliPagOff"),
										articoliTabPagOff: document.getElementById("id_articoliTabPagOff"),
										articoliTabPagOffBody: document.getElementById("id_articoliTabPagOffBody"),
										astaId: document.getElementById("id_astaId"),
										offertePagOffNotFound: document.getElementById("id_offertePagOffNotFound"),
										articoliPagOffNotFound: document.getElementById("id_articoliPagOffNotFound")});
													
			paginaVendo = new PaginaVendo({alert: alertContainer, 
											   pagVendo: document.getElementById("id_paginaVendo"),
											   asteUserNonChiuseVendo: document.getElementById("id_asteUserNonChiuseVendo"),
											   asteUserChiuseVendo: document.getElementById("id_asteUserChiuseVendo"),
											   articoliDisponibili: document.getElementById("id_articoliDisponibiliCA"),
											   articoloDisponibile: document.getElementById("id_articoloCA"),
											   articoloDispNome: document.getElementById("id_nomeArticoloCA"),
											   asteNonChiuseNotFound: document.getElementById("id_asteNonChiuseNotFound"),
											   asteChiuseNotFound: document.getElementById("id_asteChiuseNotFound")
											   });
											  	 			
			dettaglioAsta = new DettaglioAsta({
									alert: document.getElementById("id_alertChiudi"),
									pagDettAsta: document.getElementById("id_dettaglioAsta"),
									aperta: document.getElementById("id_aperta"),
									chiusa: document.getElementById("id_chiusa"),
									astaId: document.getElementById("id_astaId"),
									idAstaDett: document.getElementById("id_astaIdDettAsta"),
									prezzoIniziale: document.getElementById("id_prezzoInizialeDettAsta"),
									rialzoMinimo: document.getElementById("id_rialzoMinimoDettAsta"),
									scadenza: document.getElementById("id_scadenzaDettAsta"),
									offertaMassima: document.getElementById("id_offertaMassimaDettAsta"),
									chiusaBoolean: document.getElementById("id_isChiusaDettAsta"),
									articoliAsta: document.getElementById("id_articoliDettAsta"),
									articoliAstaTab: document.getElementById("id_articoliTabDettAsta"),
									articoliAstaBody: document.getElementById("id_articoliTabDettAstaBody"),
									offerteAsta: document.getElementById("id_offerteDettAsta"),
									offerteAstaTab: document.getElementById("id_listaOfferteDettAsta"),
									offerteAstaBody: document.getElementById("id_listaOfferteDettAstaBody"),
									aggiudicatario: document.getElementById("id_aggiudicatarioDettAsta"),
									prezzoFinale: document.getElementById("id_prezzoFinDettAsta"),
									indirizzo: document.getElementById("id_indirizzoDettAsta"),
									aggNotFound: document.getElementById("id_aggNotFound"),
									offerteDettNotFound: document.getElementById("id_offerteDettNotFound")});
			
			
			creaAsta = new CreaAsta({
								alert: document.getElementById("id_alertCreaAsta"),
								creaAsta: document.getElementById("id_creaAsta"),
								creaAstaForm: document.getElementById("id_creaAstaForm"),
								rialzoMinimo: document.getElementById("id_rialzoMinimoCA"),
								scadenza: document.getElementById("id_scadenzaCA"),
								articoliDisponibili: document.getElementById("id_articoliDisponibiliCA"),
								articoloDisponibile: document.getElementById("id_articoloCA")});
												
			creaArticolo = new CreaArticolo({
								alert: document.getElementById("id_alertCreaArt"),
								creaArticolo: document.getElementById("id_creaArticolo"),
								creaArticoloForm: document.getElementById("id_creaArticoloForm"),
								nome: document.getElementById("id_nomeArticoloVendo"),
								descrizione: document.getElementById("id_descrizioneArticoloVendo"),
								prezzo: document.getElementById("id_prezzoArticoloVendo"),
								dropzone: document.querySelector('.upImmagineDropzone'), 
								immagine: document.querySelector('.fileUploadInput')	
								});
																				
			
			//CHECK: QUALE PAGINA MOSTRO?
			checkStorage(sessionStorage.getItem('username')); 
			
			var data = new Date(localStorage.getItem(nomeUtente+"_scadenza"));
			if(data<(new Date())){ //verifico se sono scaduti i 30 giorni
				createStorage(nomeUtente);
			}
			var event = localStorage.getItem(sessionStorage.getItem('username')+"_evento"); //quale pagina mostrare
			
			if(event == "acquisto"){ //vado in acquisto
				showAcquisto(this);	
			} else if(event=="vendo"){ //vado in vendo
				showVendo(this);
			}
			
			//BOTTONE DI LOGOUT
			document.querySelector("a[href='Logout']").addEventListener('click', () => {
					window.location.href = "index.html"; //reindirizzamento dell'utente a una risorsa
					window.sessionStorage.removeItem('username');	            
	        });
		}
		
		this.refresh = function (){
			paginaOfferta.reset(); // pagina offerta NON VISIBILE 
			dettaglioAsta.reset(); // pagina dettaglio asta non visibile			
		}
	}
	
	
	//LOCAL STORAGE: 
	function createStorage(nome){ 
		var date = new Date();
		date.setMonth(date.getMonth()+1);		
		
		localStorage.setItem(nome+"_scadenza", date.toISOString()); //toISOString -> viene convertita in una stringa
		localStorage.setItem(nome+"_evento", "acquisto"); 
	}
	
	function checkStorage(nome){
		if(localStorage.getItem(nome+"_evento")==null || localStorage.getItem(nome+"_evento")==undefined){
			createStorage(nome);	
		}
	}
	
	//aggiorna le ASTE
	function updateStorageList(nome, id){//uso JSON.stringify(listAste) per settare e JSON.parse() per convertirlo in un array
		//lista date delle aste
		var listAsteDate = JSON.parse(localStorage.getItem(nome+"_listDate"));
		if(listAsteDate==null || listAsteDate==undefined){
			listAsteDate = [];
		}
		
		//lista aste
		var listAste = JSON.parse(localStorage.getItem(nome+"_list"));

		if(listAste==null || listAste==undefined){
			listAste = []; //se non esiste la creo			
		} else if(listAste.indexOf(id)!=-1){ //se esiste la lista e se già contiene l'asta 
			var index = listAste.indexOf(id);
			var date = new Date();
			date.setMonth(date.getMonth()+1); //aggiorno la data
			listAsteDate[index] = date;
		} else if(listAste.indexOf(id)==-1){ //se l'asta su cui l'utente ha cliccato non è nell'array allora la aggiungo
			listAste.push(id); //aggiundo id dell'asta alla lista di aste che l'utente ha cliccato
			var date = new Date();
			date.setMonth(date.getMonth()+1);
			listAsteDate.push(date); //aggiungo la data			
		}
		
		localStorage.setItem(nome+"_list", JSON.stringify(listAste));
		localStorage.setItem(nome+"_listDate", JSON.stringify(listAsteDate));
	}

	
	function setStorageEvent(nome, evento){
		localStorage.setItem(nome+"_evento", evento);
	}
	
	//se l'evento è acquisto mostro la pagina	
	function showAcquisto(orchestrator){
		ricercaKeyword.registerEvents();
		personalMessage.show(document.getElementById("id_username"));
		paginaAcquisto.show();
		paginaAcquisto.registerEvents(orchestrator);
		paginaOfferta.registerEvents();
		document.getElementById("id_paginaAcquisto").style.display = "block";
		document.getElementById("id_paginaVendo").style.display = "none";
	}
	
	//Se l'ultima azione è la creazione dell'asta mostra vendo	
	function showVendo(orchestrator){
		personalMessage.show(document.getElementById("id_usernameV"));
		paginaVendo.show();
		paginaVendo.registerEvents(orchestrator);
		dettaglioAsta.registerEvents();
		creaAsta.registerEvents(paginaVendo);
		creaArticolo.registerEvents(paginaVendo);
		document.getElementById("id_paginaAcquisto").style.display = "none";
		document.getElementById("id_paginaVendo").style.display = "block";
	}
 }