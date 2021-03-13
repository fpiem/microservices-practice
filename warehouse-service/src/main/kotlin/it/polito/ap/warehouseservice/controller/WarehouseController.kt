package it.polito.ap.warehouseservice.controller

import it.polito.ap.common.dto.CartProductDTO
import it.polito.ap.common.dto.DeliveryDTO
import it.polito.ap.common.dto.WarehouseProductDTO
import it.polito.ap.warehouseservice.service.WarehouseService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/warehouses")
class WarehouseController(val warehouseService: WarehouseService) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(javaClass)
    }

    @GetMapping("/{warehouseId}")
    fun warehouseInventory(@PathVariable warehouseId: String): ResponseEntity<String> {
        LOGGER.info("Received request for $warehouseId")
        return ResponseEntity.ok("ciao")
    }

    @PutMapping("/{warehouseId}")
    fun editProduct(
        @PathVariable warehouseId: String,
        @RequestBody product: WarehouseProductDTO
    ): ResponseEntity<String> {
        LOGGER.info("Received request for edit ${product.productId} in $warehouseId")
        return ResponseEntity.ok("ciao")
    }

    @PutMapping("/{warehouseId}")
    fun editAlarm(
        @PathVariable warehouseId: String,
        @RequestParam product: WarehouseProductDTO
    ): ResponseEntity<String> {
        LOGGER.info("Received request for edit ${product.productId} alarm in $warehouseId")
        return ResponseEntity.ok("ciao")
    }

    @GetMapping("")
    fun deliveryList(@RequestBody cart: List<CartProductDTO>): ResponseEntity<List<DeliveryDTO>> {
        LOGGER.info("Received request for delivery list with $cart")
        return ResponseEntity.badRequest().body(null)
    }

}