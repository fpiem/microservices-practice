package it.polito.ap.catalogservice.service

import it.polito.ap.catalogservice.model.User
import it.polito.ap.catalogservice.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class UserService(val userRepository: UserRepository) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(javaClass)
    }

    fun getUserByEmail(email: String): User? {
        LOGGER.info("Retrieving user with email: $email")
        val user = userRepository.findByEmail(email)
        user?.let {
            LOGGER.info("Retrieved user with email: $email")
        } ?: kotlin.run {
            LOGGER.info("Could not find user with email: $email")
        }
        return user
    }
}
