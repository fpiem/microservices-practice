package it.polito.ap.walletservice.repository

import it.polito.ap.walletservice.model.Wallet
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface WalletRepository: MongoRepository<Wallet, String> {

    fun getWalletByUserId(userId: String): Wallet?

}