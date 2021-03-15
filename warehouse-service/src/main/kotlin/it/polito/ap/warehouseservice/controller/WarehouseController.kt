package it.polito.ap.warehouseservice.controller

import it.polito.ap.common.dto.CartProductDTO
import it.polito.ap.common.dto.DeliveryDTO
import it.polito.ap.common.dto.WarehouseAlarmDTO
import it.polito.ap.common.dto.WarehouseProductDTO
import it.polito.ap.warehouseservice.model.Warehouse
import it.polito.ap.warehouseservice.model.WarehouseProduct
import it.polito.ap.warehouseservice.service.WarehouseService
import it.polito.ap.warehouseservice.service.mapper.WarehouseMapper
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/warehouses")
class WarehouseController(val warehouseService: WarehouseService, var mapper: WarehouseMapper) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(javaClass)
    }

    @GetMapping("/{warehouseId}")
    fun warehouseInventory(@PathVariable warehouseId: String): ResponseEntity<String> {
        LOGGER.info("Received request for $warehouseId")
        return ResponseEntity.ok("ciao")
    }

    @PutMapping("/{warehouseId}/product")
    fun editProduct(
        @PathVariable warehouseId: String,
        @RequestBody product: WarehouseProductDTO
    ): ResponseEntity<String> {
        LOGGER.info("Received request for edit ${product.productId} in $warehouseId")
        return ResponseEntity.ok("ciao")
    }

    @PutMapping("/{warehouseId}/alarm")
    fun editAlarm(
        @PathVariable warehouseId: String,
        @RequestParam productAlarm: WarehouseAlarmDTO
    ): ResponseEntity<String> {
        LOGGER.info("Received request for edit ${productAlarm.productId} alarm in $warehouseId")
        when (warehouseService.editAlarm(warehouseId, productAlarm)) {
            "could not find warehouse" -> {
                val statusString = "Could not find warehouse $warehouseId"
                LOGGER.info(statusString)
                return ResponseEntity(statusString, HttpStatus.BAD_REQUEST)
            }
            "could not find product" -> {
                val statusString = "Could not find product ${productAlarm.productId} in warehouse $warehouseId"
                LOGGER.info(statusString)
                return ResponseEntity(statusString, HttpStatus.BAD_REQUEST)
            }
            else -> {  // Success
                val statusString = "Updated alarm threshold for product ${productAlarm.productId} in warehouse $warehouseId"
                LOGGER.info(statusString)
                return ResponseEntity.ok(statusString)
            }
        }
    }

    @GetMapping("")
    fun deliveryList(@RequestBody cart: List<CartProductDTO>): ResponseEntity<List<DeliveryDTO>> {
        LOGGER.info("Received request for delivery list for cart $cart")
        return ResponseEntity.badRequest().body(null)
    }

    @GetMapping("/test")
    fun test(@RequestBody warehouseProduct: WarehouseProduct): ResponseEntity<WarehouseAlarmDTO> {
        return ResponseEntity.ok(mapper.toAlarmDTO(warehouseProduct))
    }

}