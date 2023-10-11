
(function() { 									//tipo di evento (CLICK)
  document.getElementById("login").addEventListener('click', (e) => { //listener (funzione) è il gestore dell'evento
    var form = e.target.closest("form");
    if (form.checkValidity()) {
		 //metodo post della servlet CheckLogin
      makeCall("POST", 'CheckLogin', e.target.closest("form"),  
        function(x) { // X è UN OGGETTO XMLHttpRequest
          if (x.readyState == XMLHttpRequest.DONE) {
            var message = x.responseText;
            switch (x.status) {
              case 200:  //richiesta andata a buon fine
            	sessionStorage.setItem('username', message);
                window.location.href = "Homepage.html";
                break;
              case 400: // bad request
                document.getElementById("error").textContent = message;
                break;
              case 401: // unauthorized
                document.getElementById("error").textContent = message;
                break;
              case 500: // server error
            	document.getElementById("error").textContent = message;
                break;
            }
          }
        }
      );
    } else {
    	 form.reportValidity();
    }
  });

})();