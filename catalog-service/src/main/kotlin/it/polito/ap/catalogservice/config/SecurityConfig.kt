package it.polito.ap.catalogservice.config

import it.polito.ap.catalogservice.config.CustomAuthenticationProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@EnableWebSecurity
@Configuration
class SecurityConfig(val customAuthenticationProvider: CustomAuthenticationProvider) : WebSecurityConfigurerAdapter(){




    @Bean
    fun encoder(): PasswordEncoder {
        val enc = BCryptPasswordEncoder()
        return enc
    }

    @Throws(Exception::class)
    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.authenticationProvider(customAuthenticationProvider)
    }


    override fun configure(http: HttpSecurity) {
        http.httpBasic()
            .and()
            .authorizeRequests()
            .antMatchers(HttpMethod.POST, "/products").hasRole("ADMIN")
            //.antMatchers(HttpMethod.POST, "/products").hasAuthority("ADMIN")
            .and()
            .csrf().disable()
    }

}