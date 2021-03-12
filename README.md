### TODO List

- [ ] Do a TODO List


### DONE
- In UserDTO è stato inserito il ruolo in modo da disaccoppiare la logica interna dei vari microservizi (es.: si sarebbe potuto mettere solo mail ma in quel caso il CatalogService dovrebbe conoscere la logica interna di OrderSevice)
- OrderDTO non contiene UserDTO, quindi per modificare lo stato dell'ordine, CatalogService dovrà inviare sia un OrderDTO che un UserDTO. In alcuni casi così si evita di mandare delle informazioni ridondanti; inoltre, si ha una maggiore separazione delle varie componenti
- L'informazione dello shippingAddress viene separata dalle informazioni dello UserDTO, in questo modo UserDTO rimane "pulito" in tutte le fasi della comunicazione tra CatalogService e OrderService 
