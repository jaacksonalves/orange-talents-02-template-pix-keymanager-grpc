package br.com.zup.edu.keymanager.chavepix.client.bcb

import br.com.zup.edu.TipoConta
import br.com.zup.edu.keymanager.chavepix.ChavePix
import br.com.zup.edu.keymanager.chavepix.TipoChave
import br.com.zup.edu.keymanager.chavepix.client.bcb.AccountType.Companion.toAccountType
import br.com.zup.edu.keymanager.chavepix.client.bcb.KeyType.Companion.toKeyType


data class CreatePixKeyRequest(
    val keyType: KeyType,
    val key: String?,
    val bankAccount: BankAccount,
    val owner: Owner
) {
    companion object {
        fun ChavePix.toBcb(): CreatePixKeyRequest {
            return CreatePixKeyRequest(
                keyType = toKeyType(tipoChave),
                key = chave,
                bankAccount = BankAccount(
                    participant = contaAssociada.instituicao.ispb,
                    branch = contaAssociada.agencia,
                    accountType = toAccountType(contaAssociada.tipoConta),
                    accountNumber = contaAssociada.numeroConta
                ),
                owner = Owner(
                    type = Type.toType("CPF"),
                    name = contaAssociada.titular.nomeTitular,
                    taxIdNumber = contaAssociada.titular.cpf
                )
            )
        }
    }
}

data class Owner(
    val type: Type,
    val name: String,
    val taxIdNumber: String
)

enum class Type {
    NATURAL_PERSON, LEGAL_PERSON;

    companion object {
        fun toType(tipo: String): Type {
            if (tipo == "CPF") return NATURAL_PERSON
            else throw IllegalArgumentException("Por enquanto só cadastramos CPF")
        }
    }
}

data class BankAccount(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: AccountType
)

enum class AccountType {
    CACC, SVGS;

    companion object {
        fun toAccountType(tipoConta: TipoConta): AccountType {
            return when (tipoConta) {
                TipoConta.CONTA_CORRENTE -> CACC
                TipoConta.CONTA_POUPANCA -> SVGS
                else -> throw IllegalArgumentException("Só aceitamos CC e CP por enquanto")
            }
        }

        fun toTipoConta(accountType: AccountType): TipoConta {
            return when (accountType) {
                CACC -> TipoConta.CONTA_CORRENTE
                SVGS -> TipoConta.CONTA_POUPANCA
            }
        }
    }
}

enum class KeyType {
    CPF,
    CNPJ,
    PHONE,
    EMAIL,
    RANDOM;

    companion object {
        fun toKeyType(tipoChave: TipoChave?): KeyType {
            return when (tipoChave) {
                TipoChave.CPF -> CPF
                TipoChave.CELULAR -> PHONE
                TipoChave.EMAIL -> EMAIL
                TipoChave.ALEATORIA -> RANDOM
                else -> throw IllegalArgumentException("Por enquanto não aceitamos CNPJ")
            }
        }

        fun toTipoChave(keyType: KeyType): TipoChave {
            return when (keyType) {
                CPF -> TipoChave.CPF
                CNPJ -> TipoChave.CNPJ
                PHONE -> TipoChave.CELULAR
                EMAIL -> TipoChave.EMAIL
                RANDOM -> TipoChave.ALEATORIA
            }
        }

    }


}


