package it.polito.ap.warehouseservice.controller

import it.polito.ap.common.dto.CartProductDTO
import it.polito.ap.common.dto.DeliveryDTO
import it.polito.ap.common.dto.WarehouseAlarmDTO
import it.polito.ap.common.dto.WarehouseProductDTO
import it.polito.ap.warehouseservice.model.WarehouseProduct
import it.polito.ap.warehouseservice.service.WarehouseService
import it.polito.ap.warehouseservice.service.mapper.WarehouseMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/warehouses")
class WarehouseController(val warehouseService: WarehouseService) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(javaClass)
    }

    @Autowired
    lateinit var mapper: WarehouseMapper

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
        return ResponseEntity.ok("ciao")
    }

    @GetMapping("")
    fun deliveryList(@RequestBody cart: List<CartProductDTO>): ResponseEntity<List<DeliveryDTO>> {
        LOGGER.info("Received request for delivery list with $cart")
        return ResponseEntity.badRequest().body(null)
    }

    @GetMapping("/test")
    fun test(@RequestBody warehouseProductDTO: WarehouseProductDTO): ResponseEntity<WarehouseProduct> {
        // val mapper = Mappers.getMapper(WarehouseMapper::class.java)
        return ResponseEntity.ok(mapper.toModel(warehouseProductDTO))
    }

}