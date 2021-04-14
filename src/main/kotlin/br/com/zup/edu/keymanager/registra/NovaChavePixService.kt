package br.com.zup.edu.keymanager.registra

import br.com.zup.edu.TipoConta
import br.com.zup.edu.keymanager.ChavePix
import br.com.zup.edu.keymanager.ChavePixRepository
import br.com.zup.edu.keymanager.TipoChave
import br.com.zup.edu.keymanager.client.itau.ItauErpClient
import br.com.zup.edu.keymanager.compartilhado.exceptions.ChavePixExistenteException
import br.com.zup.edu.keymanager.compartilhado.exceptions.TipoChaveInvalidoException
import br.com.zup.edu.keymanager.compartilhado.exceptions.handlers.ErrorHandler
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

        if (novaChaveRequest.tipoConta == TipoConta.UNKNOWN_CONTA) {
            throw TipoChaveInvalidoException("Tipo de conta inválido")
        }

        //validando se a chave já existe, pois não é permitido cadastro de chaves duplicadas
        if (repository.existsByChave(novaChaveRequest.chave)) {
            throw ChavePixExistenteException("Chave já cadastrada")
        }

        val clientResponse =
            itauErpClient.buscaContaPorTipo(novaChaveRequest.clienteId, novaChaveRequest.tipoConta.name)
        val contaAssociada = clientResponse.body()?.toModel(novaChaveRequest.tipoConta, novaChaveRequest.clienteId)
            ?: throw java.lang.IllegalStateException("Cliente não encontrado")

        //Chave CPF é cadastrado automaticamente o CPF do cliente (não precisa passar no request), então verifica antes se já tem cadastrado, se não tiver, cadastra
        if (novaChaveRequest.tipoChave == TipoChave.CPF) {
            if (repository.existsByContaAssociadaTitularCpf(contaAssociada.titular.cpf)) {
                throw ChavePixExistenteException("Chave CPF já cadastrada")
            }
        }

        val chavePix = novaChaveRequest.toModel(contaAssociada)
        repository.save(chavePix)

        return chavePix

    }

}
