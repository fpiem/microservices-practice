### TODO List

- [ ] Do a TODO List

### DONE

- In UserDTO è stato inserito il ruolo in modo da disaccoppiare la logica interna dei vari microservizi (es.: si sarebbe potuto mettere solo mail ma in quel caso il CatalogService dovrebbe conoscere la logica interna di OrderService)
- OrderDTO non contiene UserDTO, quindi per modificare lo stato dell'ordine, CatalogService dovrà inviare sia un OrderDTO che un UserDTO. In alcuni casi così si evita di mandare delle informazioni ridondanti; inoltre, si ha una maggiore separazione delle varie componenti
- L'informazione dello shippingAddress viene separata dalle informazioni dello UserDTO, in questo modo UserDTO rimane "pulito" in tutte le fasi della comunicazione tra CatalogService e OrderService 
- In WarehouseController/Service editAlarm(...) con un productId non presente nella warehouse **non** crea una nuova entry nel database, ma editProduct sí, mi sembrava avesse piú senso semanticamente.
- Nessuno dei due crea una *warehouse* non esistente.
- Using `getWarehouseByWarehouseId()` as a service function (instead of invoking the repository each time) also allows us to cache the result of the query 
- Nella classe Transaction, transactionTimestamp e transactionId sono separati. Id offre un identificativo globalmente univoco per la transazione, timestamp offre informazione di convenienza sulla data in cui la transazione é stata generata ma é potenzialmente non univoco nel caso in cui il DB venga duplicato fra piú istanze.
- Note: transactionTimestamp and transactionId are set once the Transaction object is obtained from the transactionDTO, they are not created in the transactionDTO. Questo é ok perché se il transactionService fallisce, comunque la transazione non é creata, e se ha successo, comunque la transazione é creata.
- In productService editProduct non crea un nuovo prodotto in caso si cerchi di modificare un prodotto non presente nel DB, questo perchè si è deciso di "potenziare" la addProduct facendo dei controlli riguardo l'esistenza del prodotto (nella prima versione con la add si poteva sovrascrivere i prodotti senza problemi)