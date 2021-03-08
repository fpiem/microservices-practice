package it.polito.ap.catalogservice.repository

import it.polito.ap.catalogservice.model.User
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : MongoRepository<User, String> {
    fun findByEmailAndPassword(email: String, password: String): User?
    fun findByEmail(email: String): User?
}