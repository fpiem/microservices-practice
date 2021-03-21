package it.polito.ap.walletservice.model

import it.polito.ap.common.utils.TransactionMotivation
import org.bson.types.ObjectId

class Transaction(
    var issuerId: String,
    var amount: Double,
    var transactionMotivation: TransactionMotivation
) {
    init {
        // ObjectId() employed to generate a unique transaction identifier
        val transactionId = ObjectId().toString()
        val transactionTimestamp = System.currentTimeMillis()
    }
}