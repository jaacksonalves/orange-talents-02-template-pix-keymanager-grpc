package br.com.zup.edu.keymanager.client.bcb

import br.com.zup.edu.TipoConta
import br.com.zup.edu.keymanager.ChavePix
import br.com.zup.edu.keymanager.TipoChave
import br.com.zup.edu.keymanager.client.bcb.AccountType.Companion.toAccountType
import br.com.zup.edu.keymanager.client.bcb.KeyType.Companion.toKeyType
import java.lang.IllegalArgumentException


data class CreatePixKeyRequest(
    val chavePix: ChavePix
) {
    val keyType: KeyType = toKeyType(chavePix.tipoChave)
    val key: String = chavePix.chave
    val bankAccount: BankAccount = BankAccount(chavePix)
    val owner: Owner = Owner(chavePix)
}

data class Owner(val chavePix: ChavePix) {
    val type: Type = Type.toType("CPF")
    val name: String = chavePix.contaAssociada.titular.nomeTitular
    val taxIdNumber: String = chavePix.contaAssociada.titular.cpf

}

enum class Type {
    NATURAL_PERSON, LEGAL_PERSON;

    companion object {
        fun toType(tipo: String): Type {
            if (tipo == "CPF") return NATURAL_PERSON
            else throw IllegalArgumentException("Por enquanto só cadastramos CPF")
        }
    }
}

data class BankAccount(val chavePix: ChavePix) {
    val participant: String = chavePix.contaAssociada.instituicao.ispb
    val branch: String = chavePix.contaAssociada.agencia
    val accountNumber: String = chavePix.contaAssociada.numeroConta
    val accountType: AccountType = toAccountType(chavePix.contaAssociada.tipoConta)

}

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
    }

}
