### TODO List

- [] fix snake case in application.yml files
- [] remove pairs of application.yml
- [] how to deploy the whole thing? docker-compose?
- [] usedId should be an ObjectId, separate from email
- [] users should be able to cancel their own order
- [] admin should be able to do anything / change status of any order that's not been delivered
- [] alarms and emails - requires adding an admin to the warehouse
- [] alarms - aggiungere un check dopo le modifiche sulla quantitá per vedere se é scesa sotto la soglia di allarme
- [] check quantitá di prodotti nel carrello quando si va da catalog ad order
- [] caches
- [] available product quantity should be visible from catalog
- [] logica per cominciare a fare l'invio dell'ordine dopo che é stato piazzato

- chiave primaria orderId, poi warehouseId, e productId e quantity prelevata da quel warehouse + **stato** (delivered, not delivered)
- Serve un ACK message dall'orderservice? Tipo "confirm messages received" da Kafka per cambiare lo stato nel DB di pickups?
- Il vantaggio del db di pickup é che siamo in grado di tornare indietro se falliamo durante la selezione degli items dalla warehouse
- Order updates in the database need to be **atomic**
- Nelle warehouse check se le quantitá sono sotto l'allarme
- TUTTI gli admin devono ricevere notifiche quando si aggiorna un ordine?

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
- In productService editProduct non crea un nuovo prodotto in caso si cerchi di modificare un prodotto non presente nel DB, questo perchè si è deciso di "potenziare" la addProduct facendo dei controlli riguardo l'esistenza del prodotto (nella prima versione con la add si poteva sovrascrivere i prodotti senza problemi)
- We were not able to create a warehouse product delivery list in a transactional way. Instead, we decided to first the contents of the warehouse, then trying to atomically update based on the read contents, and failing the order creation (and therefore rolling back) if this brings the quantity of any product below zero.