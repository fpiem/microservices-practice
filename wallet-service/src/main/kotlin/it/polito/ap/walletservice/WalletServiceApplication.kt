package it.polito.ap.walletservice

import it.polito.ap.common.utils.TransactionMotivation
import it.polito.ap.walletservice.model.Transaction
import it.polito.ap.walletservice.model.Wallet
import it.polito.ap.walletservice.repository.WalletRepository
import org.bson.types.ObjectId
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WalletServiceApplication(
    walletRepository: WalletRepository
) {
    init {
//        walletRepository.deleteAll()
//        val wallet1 = Wallet(ObjectId("111111111111111111111111"))
//        val wallet2 = Wallet(ObjectId("222222222222222222222222"))
//        walletRepository.saveAll(listOf(wallet1, wallet2))

//        walletRepository.deleteAll()

//        val wallet1 = Wallet(
//            "user1", 420.69, mutableListOf(
//                Transaction(
//                    "admin1", 421.69, TransactionMotivation.ADMIN_RECHARGE
//                ),
//                Transaction(
//                    "order1", -1.0, TransactionMotivation.ORDER_PAYMENT
//                ),
//                Transaction(
//                    "order1", 1.0, TransactionMotivation.REFUND
//                )
//            )
//        )
//        val wallet2 = Wallet(
//            "user2", 69.69, mutableListOf(
//                Transaction(
//                    "admin2", 100.0, TransactionMotivation.ADMIN_RECHARGE,
//                ),
//                Transaction(
//                    "order2", -41.31, TransactionMotivation.ORDER_PAYMENT
//                ),
//                Transaction(
//                    "admin2", 10.0, TransactionMotivation.ADMIN_RECHARGE
//                ),
//            )
//        )

    }
}

fun main(args: Array<String>) {
    runApplication<WalletServiceApplication>(*args)
}
