### TODO List

- [ ] Do a TODO List

### DONE

- In UserDTO è stato inserito il ruolo in modo da disaccoppiare la logica interna dei vari microservizi (es.: si sarebbe potuto mettere solo mail ma in quel caso il CatalogService dovrebbe conoscere la logica interna di OrderSevice)
- OrderDTO non contiene UserDTO, quindi per modificare lo stato dell'ordine, CatalogService dovrà inviare sia un OrderDTO che un UserDTO. In alcuni casi così si evita di mandare delle informazioni ridondanti; inoltre, si ha una maggiore separazione delle varie componenti
- L'informazione dello shippingAddress viene separata dalle informazioni dello UserDTO, in questo modo UserDTO rimane "pulito" in tutte le fasi della comunicazione tra CatalogService e OrderService 
- In WarehouseController/Service editAlarm(...) con un productId non presente nella warehouse **non** crea una nuova entry nel database, ma editProduct sí, mi sembrava avesse piú senso semanticamente.
- Nessuno dei due crea una *warehouse* non esistente.
- Using `getWarehouseByWarehouseId()` as a service function (instead of invoking the repository each time) also allows us to cache the result of the query 