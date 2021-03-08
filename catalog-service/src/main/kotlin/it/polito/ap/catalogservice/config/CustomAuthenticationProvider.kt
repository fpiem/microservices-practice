package it.polito.ap.catalogservice.config

import it.polito.ap.catalogservice.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class CustomAuthenticationProvider (val userRepository: UserRepository): AuthenticationProvider {

    companion object {
        private val LOGGER
                = LoggerFactory.getLogger(javaClass)
    }

    @Autowired
    lateinit var encoder: PasswordEncoder

    @Throws(AuthenticationException::class)
    override fun authenticate(authentication: Authentication): Authentication? {
        LOGGER.info("authentication user ${authentication.name},${authentication.credentials} -> ${encoder.encode(authentication.credentials.toString())}")
        val user = userRepository.findByEmailAndPassword(authentication.name, authentication.credentials.toString())
        user?.let {
            /*
            val authorities = setOf(SimpleGrantedAuthority(user.role.toString()))
            LOGGER.info("found user ${user.name}, ${user.surname}, $authorities")
            return UsernamePasswordAuthenticationToken(user, authentication.name, authorities)
            */
            return null // TODO far funzionare role
        } ?: kotlin.run {
            LOGGER.info("user not found into the DB")
            return null
        }
    }

    override fun supports(authentication: Class<*>?): Boolean {
        return authentication == UsernamePasswordAuthenticationToken::class.java
    }
}