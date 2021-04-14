package br.com.zup.edu.keymanager

import br.com.zup.edu.RegistraChavePixRequest
import br.com.zup.edu.TipoConta
import br.com.zup.edu.keymanager.registra.NovaChavePixRequest

fun RegistraChavePixRequest.toModel(): NovaChavePixRequest {
    return NovaChavePixRequest(
        clienteId = clienteId,
        tipoChave = TipoChave.valueOf(tipoChave.name),
        chave = chave,
        tipoConta = TipoConta.valueOf(tipoConta.name)
    )

}