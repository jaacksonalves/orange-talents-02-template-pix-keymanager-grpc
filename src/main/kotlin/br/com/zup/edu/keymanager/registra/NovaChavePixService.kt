package br.com.zup.edu.keymanager.registra

import br.com.zup.edu.TipoConta
import br.com.zup.edu.keymanager.ChavePix
import br.com.zup.edu.keymanager.ChavePixRepository
import br.com.zup.edu.keymanager.TipoChave
import br.com.zup.edu.keymanager.client.bcb.BcbClient
import br.com.zup.edu.keymanager.client.bcb.CreatePixKeyRequest
import br.com.zup.edu.keymanager.client.bcb.CreatePixKeyResponse
import br.com.zup.edu.keymanager.client.itau.ItauErpClient
import br.com.zup.edu.keymanager.compartilhado.exceptions.ChavePixExistenteException
import br.com.zup.edu.keymanager.compartilhado.exceptions.TipoChaveInvalidoException
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid


@Validated
@Singleton
class NovaChavePixService(
    @Inject private val repository: ChavePixRepository,
    @Inject private val itauErpClient: ItauErpClient,
    @Inject private val bcbClient: BcbClient
) {

    @Transactional
    fun registra(@Valid novaChaveRequest: NovaChavePixRequest): ChavePix {

        if (novaChaveRequest.tipoConta == TipoConta.UNKNOWN_CONTA) {
            throw TipoChaveInvalidoException("Tipo de conta inválido")
        }

        //validando se a chave já existe, pois não é permitido cadastro de chaves duplicadas
        if (repository.existsByChave(novaChaveRequest.chave)) {
            throw ChavePixExistenteException("Chave ${novaChaveRequest.tipoChave}: ${novaChaveRequest.chave} já cadastrada")
        }

        //conecta ao client Erp Itau
        val clientResponse =
            itauErpClient.buscaContaPorTipo(novaChaveRequest.clienteId, novaChaveRequest.tipoConta.name)
        val contaAssociada = clientResponse.body()?.toModel(novaChaveRequest.tipoConta, novaChaveRequest.clienteId)
            ?: throw IllegalStateException("Cliente não encontrado")

        //Cria nova chave e salva no banco após verificação
        val chavePix = novaChaveRequest.toModel(contaAssociada)
        //Verificação se a chave CPF já está cadastrada, pois a mesma não precisa ser enviada pelo request, passando pela verificação acima
        if (chavePix.tipoChave == TipoChave.CPF && repository.existsByChave(chavePix.contaAssociada.titular.cpf)) {
            throw ChavePixExistenteException("Chave CPF já cadastrada")
        }
        repository.save(chavePix)

        val responseBcb = bcbClient.cadastraChave(CreatePixKeyRequest(chavePix))

        when (responseBcb.status) {
            HttpStatus.UNPROCESSABLE_ENTITY -> throw IllegalArgumentException("Chave Pix já cadastrada no BCB")
            HttpStatus.CREATED -> chavePix.atualizaChave(responseBcb.body())
            else -> throw IllegalStateException("Erro ao conectar com o servidor BCB, por favor tente mais tarde")
        }

        return chavePix

    }


}
