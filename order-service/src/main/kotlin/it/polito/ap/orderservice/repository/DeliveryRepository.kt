package it.polito.ap.orderservice.repository

import it.polito.ap.orderservice.model.Delivery
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface DeliveryRepository : MongoRepository<Delivery, String> {
}