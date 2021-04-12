package br.com.zup.edu.keymanager

import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class NovaChavePixService(
    @Inject private val repository: ChavePixRepository,
    @Inject private val itauErpClient: ItauErpClient
) {

    @Transactional
    fun registra(@Valid novaChaveRequest: NovaChavePixRequest): ChavePix {
        //validando se a chave já existe, pois não é permitido cadastro de chaves duplicadas
        if (repository.existsByChave(novaChaveRequest.chave)) {
            throw IllegalStateException("Chave já cadastrada")
        }

        val clientResponse =
            itauErpClient.buscaContaPorTipo(novaChaveRequest.clienteId, novaChaveRequest.tipoConta.name)
        val contaAssociada = clientResponse.body()?.toModel(novaChaveRequest.tipoConta, novaChaveRequest.clienteId)
            ?: throw java.lang.IllegalStateException("Cliente não encontrado")

        val chavePix = novaChaveRequest.toModel(contaAssociada)
        repository.save(chavePix)

        return chavePix

    }

}
