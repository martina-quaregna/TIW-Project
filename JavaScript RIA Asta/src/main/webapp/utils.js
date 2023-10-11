/**
 * AJAX call management
 * 		
 * 		method -> GET o POST
 * 		url -> SERVLET 
 * 		formElement -> form (null se GET)
 * 		cback -> funzione 
 */

	
	function makeCall(method, url, formElement, cback, reset = true) {
	    var req = new XMLHttpRequest(); 
	    
	    //memorizza la funzione che gestisce l'evento di cambio dello stato
	    req.onreadystatechange = function() {  
	      cback(req)  
	    }; 
	    
	    req.open(method, url);  //crea una richiesta con metodo e URL specificato
	    
	    if (formElement == null) { 
	      req.send();  //effettua l'invio della richiesta HTTP
	    } else {
	      req.send(new FormData(formElement));  //invia la richiesta con un request body
	    }
	    
	    if (formElement !== null && reset === true) {
	      formElement.reset();
	    }
	  }
