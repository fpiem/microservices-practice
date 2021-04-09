### Useful links
https://www.overleaf.com/project/60702650f977ba657f126342

### Docker deployment
docker-compose -p ecommerce -f .\docker-compose.yml up -d --remove-orphans

### TODO List

- [X] un cliente dovrebbe poter ordinare solamente prodotti che sono presenti nel catalog: controllare che piazzando un ordine l'id di tutti i prodotti nel carrello sia presente nel catalog
- [X] logica per cominciare a fare l'invio dell'ordine dopo che é stato piazzato => REST calls nel catalog da un admin?
- [X] email in order controller - che admin ricevono le mail?
- [X] CustomerProductDTO, interfaccia nel catalog dovrebbe usare un DTO, al momento usa l'oggetto di modello
    - Probabilmente il DTO puó essere un mapping 1 a 1 dal Product di modello
    - ProductMapper per fare la conversione fra i due (dovrebbe essere 1 a 1 banalmente)
- [X] customer should be able to see their own wallet funds and transaction list from the catalog service (i diritti dovrebbero essere controllati nel catalog)
- [X] admins should be able to see anyone's wallet funds and transaction list from the catalog service (i diritti dovrebbero essere controllati nel catalog)
- [X] fix snake case in application.yml files
- [X] how to deploy the whole thing? docker-compose? - DA FARE
- [X] userId should be an ObjectId, separate from email
- [X] users should be able to cancel their own order
- [X] alarms and emails - requires adding an admin to the warehouse
- [X] alarms - aggiungere un check dopo le modifiche sulla quantitá per vedere se é scesa sotto la soglia di allarme
- [X] check quantitá di prodotti nel carrello quando si va da catalog ad order
- [X] caches
- [X] available product quantity should be visible from catalog
- [X] remove pairs of application.yml (FORSE NON SERVE) - sembra servano entrambi, una per docker una per il service
- [X] Order updates in the database need to be **atomic**

- chiave primaria orderId, poi warehouseId, e productId e quantity prelevata da quel warehouse + **stato** (delivered, not delivered)
- Serve un ACK message dall'orderservice? Tipo "confirm messages received" da Kafka per cambiare lo stato nel DB di pickups?
- Il vantaggio del db di pickup é che siamo in grado di tornare indietro se falliamo durante la selezione degli items dalla warehouse
- TUTTI gli admin devono ricevere notifiche quando si aggiorna un ordine? - abbiamo deciso contro in pratica
- Operazione di update quantitá prodotti nel catalog service inviata dal warehouse service - l'operazione é molto pesante, é previsto che venga fatta ad esempio di notte quando il carico sul servizio é basso

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
- In order-service, changeStatus è una PUT e non una PATCH perchè facilita la comunicazione dal catalog-service
- admin should be able to do anything / change status of any order even if it has been delivered
- descrivere le cache nel report
- si assume che gli admin possano accedere direttamente al warehouse service 
- In orderservice - @cacheable quando leggiamo un prodotto, @cacheput quando creiamo o modifichiamo un prodotto (=> al tempo di modifica il prodotto nuovo va direttamente in cache) e una @cacheevict quando ci arriva nel listener l'update dal warehouseservice. Questi valori meglio non metterli in cache perché staremmo cacheando l'intero db della warehouse circa
- Da catalog-service a wallet-service: senza paramentro vengono retrieved le info dell'utente loggato, altrimenti viene eseguito codice riservato agli admin (con il paramentro si indica di quale utente vogliamo le info)
- quando viene modificato lo stato di un ordine la mail viene inviata a: il buyer, 3 admin (random policy) e all'admin che modifica lo status se è lui a modificarlo (se è già presente tra gli admin selezionati dalla policy allora riceve una sola mail e non due)
- Eseguire OrderStatusRoutine.kt per simulare stato avanzamento ordine
- In catalog-service, le funzioni getAdminsEmail() e getEmailById() solo utilizzabili solo dagli admin ma in realtà non dovrebbero essere visibili all'esterno (solo da order-service)