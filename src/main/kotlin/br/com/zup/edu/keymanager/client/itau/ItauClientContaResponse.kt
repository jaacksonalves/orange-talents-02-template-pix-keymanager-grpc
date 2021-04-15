package br.com.zup.edu.keymanager.client.itau

import br.com.zup.edu.TipoConta
import br.com.zup.edu.keymanager.ContaAssociada
import br.com.zup.edu.keymanager.Instituicao
import br.com.zup.edu.keymanager.Titular
import java.util.*

data class ItauClientContaResponse(
    val tipo: String,
    val instituicao: InstituicaoClientResponse,
    val agencia: String,
    val numero: String,
    val titular: TitularClientResponse

) {
    fun toModel(tipoConta: TipoConta, clienteId: String): ContaAssociada {
        return ContaAssociada(
            tipoConta = tipoConta,
            instituicao = instituicao.toModel(),
            agencia = agencia,
            numeroConta = numero,
            titular = titular.toModel(clienteId)

        )
    }

}

data class InstituicaoClientResponse(val nome: String, val ispb: String) {
    fun toModel(): Instituicao {
        return Instituicao(nomeInstituicao = nome, ispb = ispb)
    }
}

data class TitularClientResponse(val nome: String, val cpf: String) {
    fun toModel(clienteId: String): Titular {
        return Titular(titularId = UUID.fromString(clienteId), cpf = cpf, nomeTitular = nome)
    }
}