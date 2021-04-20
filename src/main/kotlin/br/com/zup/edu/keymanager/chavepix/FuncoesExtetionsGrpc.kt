package br.com.zup.edu.keymanager.chavepix

import br.com.zup.edu.RegistraChavePixRequest
import br.com.zup.edu.TipoConta
import br.com.zup.edu.keymanager.chavepix.registra.NovaChavePixRequest

fun RegistraChavePixRequest.toModel(): NovaChavePixRequest {
    return NovaChavePixRequest(
        clienteId = clienteId,
        tipoChave = TipoChave.valueOf(tipoChave.name),
        chave = chave,
        tipoConta = TipoConta.valueOf(tipoConta.name)
    )

}