### TODO List

- [ ] Do a TODO List

### DONE

- In UserDTO è stato inserito il ruolo in modo da disaccoppiare la logica interna dei vari microservizi (es.: si sarebbe potuto mettere solo mail ma in quel caso il CatalogService dovrebbe conoscere la logica interna di OrderService)
- OrderDTO non contiene UserDTO, quindi per modificare lo stato dell'ordine, CatalogService dovrà inviare sia un OrderDTO che un UserDTO. In alcuni casi così si evita di mandare delle informazioni ridondanti; inoltre, si ha una maggiore separazione delle varie componenti
- L'informazione dello shippingAddress viene separata dalle informazioni dello UserDTO, in questo modo UserDTO rimane "pulito" in tutte le fasi della comunicazione tra CatalogService e OrderService 
- In WarehouseController/Service editAlarm(...) con un productId non presente nella warehouse **non** crea una nuova entry nel database, ma editProduct sí, mi sembrava avesse piú senso semanticamente.
- Nessuno dei due crea una *warehouse* non esistente.
- Using `getWarehouseByWarehouseId()` as a service function (instead of using a blocking findAndModify for both the search and the update) also allows us to cache the result of the query 
- Warehouse search is done using the `getWarehouseByWarehouseId()`, update is done with findAndModify. This allows us to have the warehouse blocked for less time, as well as having a cache.
- Nella classe Transaction, transactionTimestamp e transactionId sono separati. Id offre un identificativo globalmente univoco per la transazione, timestamp offre informazione di convenienza sulla data in cui la transazione é stata generata ma é potenzialmente non univoco nel caso in cui il DB venga duplicato fra piú istanze.
- Note: transactionTimestamp and transactionId are set once the Transaction object is obtained from the transactionDTO, they are not created in the transactionDTO. Questo é ok perché se il transactionService fallisce, comunque la transazione non é creata, e se ha successo, comunque la transazione é creata.