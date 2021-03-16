package it.polito.ap.warehouseservice.controller

import it.polito.ap.common.dto.*
import it.polito.ap.warehouseservice.model.WarehouseProduct
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
        private val LOGGER = LoggerFactory.getLogger(javaClass)
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
            else -> {  // Warehouse not found
                val statusString = "Could not find warehouse $warehouseId"
                LOGGER.info(statusString)
                return ResponseEntity(statusString, HttpStatus.BAD_REQUEST)
            }
        }
    }

    @PutMapping("/{warehouseId}/alarm")
    fun editAlarm(
        @PathVariable warehouseId: String,
        @RequestBody warehouseAlarmDTO: WarehouseAlarmDTO
    ): ResponseEntity<String> {
        println("HERE")
        LOGGER.info("Received request for edit ${warehouseAlarmDTO.productId} alarm in $warehouseId")
        when (warehouseService.editAlarm(warehouseId, warehouseAlarmDTO)) {
            "could not find warehouse" -> {
                val statusString = "Could not find warehouse $warehouseId"
                LOGGER.info(statusString)
                return ResponseEntity(statusString, HttpStatus.BAD_REQUEST)
            }
            "could not find product" -> {
                val statusString = "Could not find product ${warehouseAlarmDTO.productId} in warehouse $warehouseId"
                LOGGER.info(statusString)
                return ResponseEntity(statusString, HttpStatus.BAD_REQUEST)
            }
            else -> {  // Success
                val statusString = "Updated alarm threshold for product ${warehouseAlarmDTO.productId} in warehouse $warehouseId"
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
        return ResponseEntity.ok(mapper.toWarehouseAlarmDTO(warehouseProduct))
    }

}