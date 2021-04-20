package br.com.zup.edu.keymanager.chavepix.client.bcb

import java.time.LocalDateTime

data class CreatePixKeyResponse(
    val keyType: KeyType,
    val key: String,
    val bankAccount: BankAccountResponse,
    val owner: OwnerResponse,
    val createdAt: LocalDateTime
)

data class BankAccountResponse(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: AccountType
)

data class OwnerResponse(
    val type: Type,
    val name: String,
    val taxIdNumber: String
)
