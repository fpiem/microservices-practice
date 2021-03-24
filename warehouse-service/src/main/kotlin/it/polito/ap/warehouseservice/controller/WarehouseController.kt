package it.polito.ap.warehouseservice.controller

import it.polito.ap.common.dto.*
import it.polito.ap.warehouseservice.service.WarehouseService
import it.polito.ap.warehouseservice.service.mapper.WarehouseMapper
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/warehouses")
class WarehouseController(val warehouseService: WarehouseService, var mapper: WarehouseMapper) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(WarehouseController::class.java)
    }

    @PostMapping("/{warehouseId}")
    fun createProduct(
        @PathVariable warehouseId: String, @RequestBody warehouseProductDTO: WarehouseProductDTO
    ): ResponseEntity<String> {
        LOGGER.info("Received request to add new product with Id ${warehouseProductDTO.productId} in warehouse $warehouseId")
        when (warehouseService.addProduct(warehouseId, warehouseProductDTO)) {
            "warehouse not found" -> {
                val statusString = "Could not find warehouse $warehouseId"
                LOGGER.info(statusString)
                return ResponseEntity(statusString, HttpStatus.BAD_REQUEST)
            }
            "negative product quantity" -> {
                val statusString = "Product quantity must be non-negative"
                LOGGER.info(statusString)
                return ResponseEntity(statusString, HttpStatus.BAD_REQUEST)
            }
            "negative alarm threshold" -> {
                val statusString = "Alarm threshold must be non-negative"
                LOGGER.info(statusString)
                return ResponseEntity(statusString, HttpStatus.BAD_REQUEST)
            }
            "product added" -> {
                val statusString = "Product ${warehouseProductDTO.productId} successfully added to $warehouseId"
                LOGGER.info(statusString)
                return ResponseEntity.ok(statusString)
            }
            else -> {
                val statusString = "Could not add product - generic error"
                LOGGER.info(statusString)
                return ResponseEntity(statusString, HttpStatus.BAD_REQUEST)
            }
        }
    }

    @GetMapping("/{warehouseId}")
    fun warehouseInventory(@PathVariable warehouseId: String): ResponseEntity<List<WarehouseProductDTO>> {
        // TODO: should something other than null be returned in case warehouse is not found?
        LOGGER.info("Received request for the inventory of warehouse $warehouseId")
        val inventory = warehouseService.warehouseInventory(warehouseId)
        inventory?.let {
            LOGGER.info("Retrieved inventory of warehouse $warehouseId")
            return ResponseEntity.ok(inventory)
        } ?: kotlin.run {
            LOGGER.info("Could not find warehouse $warehouseId")
            return ResponseEntity(null, HttpStatus.BAD_REQUEST)
        }
    }

    @PutMapping("/{warehouseId}/product")
    fun editProduct(
        @PathVariable warehouseId: String,
        @RequestBody warehouseProductDTO: WarehouseProductDTO
    ): ResponseEntity<String> {
        LOGGER.info("Received request for edit ${warehouseProductDTO.productId} in $warehouseId")
        when (warehouseService.editProduct(warehouseId, warehouseProductDTO)) {
            "product updated" -> {
                val statusString = "Updated product ${warehouseProductDTO.productId} in warehouse $warehouseId"
                LOGGER.info(statusString)
                return ResponseEntity.ok(statusString)
            }
            "product added" -> {
                val statusString = "Added product ${warehouseProductDTO.productId} to warehouse $warehouseId"
                LOGGER.info(statusString)
                return ResponseEntity.ok(statusString)
            }
            "alarm threshold not set" -> {
                val statusString = "Alarm threshold not set, could not create product ${warehouseProductDTO.productId}"
                LOGGER.info(statusString)
                return ResponseEntity(statusString, HttpStatus.BAD_REQUEST)
            }
            "insufficient product quantity" -> {
                val statusString = "Insufficient quantity of product ${warehouseProductDTO.productId} in warehouse $warehouseId"
                LOGGER.info(statusString)
                return ResponseEntity(statusString, HttpStatus.BAD_REQUEST)
            }
            "warehouse not found" -> {  // Warehouse not found
                val statusString = "Could not find warehouse $warehouseId"
                LOGGER.info(statusString)
                return ResponseEntity(statusString, HttpStatus.BAD_REQUEST)
            }
            else -> {  // Warehouse not found
                val statusString = "Could not update - general error"
                LOGGER.info(statusString)
                return ResponseEntity(statusString, HttpStatus.BAD_REQUEST)
            }
        }
    }

    @PutMapping("/{warehouseId}/alarm")
    fun editAlarm(
        @PathVariable warehouseId: String,
        @RequestBody warehouseProductDTO: WarehouseProductDTO
    ): ResponseEntity<String> {
        LOGGER.info("Received request for edit ${warehouseProductDTO.productId} alarm in $warehouseId")
        when (warehouseService.editAlarm(warehouseId, warehouseProductDTO)) {
            "warehouse not found" -> {
                val statusString = "Could not find warehouse $warehouseId"
                LOGGER.info(statusString)
                return ResponseEntity(statusString, HttpStatus.BAD_REQUEST)
            }
            "negative alarm threshold" -> {
                val statusString = "Alarm threshold must be non-negative"
                LOGGER.info(statusString)
                return ResponseEntity(statusString, HttpStatus.BAD_REQUEST)
            }
            "product not found" -> {
                val statusString = "Could not find product ${warehouseProductDTO.productId} in warehouse $warehouseId"
                LOGGER.info(statusString)
                return ResponseEntity(statusString, HttpStatus.BAD_REQUEST)
            }
            "alarm updated" -> {  // Success
                val statusString = "Updated alarm threshold for product ${warehouseProductDTO.productId} in warehouse $warehouseId"
                LOGGER.info(statusString)
                return ResponseEntity.ok(statusString)
            }
            else -> {
                val statusString = "Could not update - general error"
                LOGGER.info(statusString)
                return ResponseEntity(statusString, HttpStatus.BAD_REQUEST)
            }
        }
    }

    // TODO: info on orderId? Brutto, introduce coupling, ma non saprei come altro fare
    @GetMapping("/deliveries")
    fun deliveryList(@RequestBody cart: List<CartProductDTO>): ResponseEntity<List<DeliveryDTO>> {
        LOGGER.info("Received request for delivery list for cart $cart")
        return ResponseEntity.badRequest().body(null)
    }

}