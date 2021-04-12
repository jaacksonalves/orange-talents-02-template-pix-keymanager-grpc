package br.com.zup.edu.keymanager

import br.com.zup.edu.TipoConta

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
        return Titular(titularId = clienteId, cpf = cpf, nomeTitular = nome)
    }
}