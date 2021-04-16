package br.com.zup.edu.keymanager.registra

import br.com.zup.edu.TipoConta
import br.com.zup.edu.keymanager.ChavePix
import br.com.zup.edu.keymanager.ChavePixRepository
import br.com.zup.edu.keymanager.TipoChave
import br.com.zup.edu.keymanager.client.bcb.BcbClient
import br.com.zup.edu.keymanager.client.bcb.CreatePixKeyRequest
import br.com.zup.edu.keymanager.client.bcb.CreatePixKeyRequest.Companion.toBcb
import br.com.zup.edu.keymanager.client.bcb.CreatePixKeyResponse
import br.com.zup.edu.keymanager.client.itau.ItauErpClient
import br.com.zup.edu.keymanager.compartilhado.exceptions.ChavePixExistenteException
import br.com.zup.edu.keymanager.compartilhado.exceptions.TipoChaveInvalidoException
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.http.client.exceptions.HttpClientResponseException
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

        //Conta não pode ser UNKNOWN, (Tipo de chave está sendo validada dentro do Enum TipoChave)
        if (novaChaveRequest.tipoConta == TipoConta.UNKNOWN_CONTA) {
            throw TipoChaveInvalidoException("Tipo de conta inválido")
        }

        //validando se a chave já existe, pois não é permitido cadastro de chaves duplicadas
        if (repository.existsByChave(novaChaveRequest.chave)) {
            throw ChavePixExistenteException("Chave ${novaChaveRequest.tipoChave}: ${novaChaveRequest.chave} já cadastrada")
        }

        //Conexão com Client Itau Erp
        val chavePix: ChavePix
        try {
            val clientResponse =
                itauErpClient.buscaContaPorTipo(novaChaveRequest.clienteId, novaChaveRequest.tipoConta.name)
            val contaAssociada = clientResponse.body()?.toModel(novaChaveRequest.tipoConta, novaChaveRequest.clienteId)
                ?: throw IllegalStateException("Cliente não encontrado")
            chavePix = novaChaveRequest.toModel(contaAssociada)
        } catch (e: HttpClientException) {
            throw IllegalStateException("Não foi possível conectar ao sistema Erp Itau, tente mais tarde")
        }

        //Verifica se já existe a chave CPF, pois a mesma não precisa ser inserida no request por ser cadastrada com o próprio CPF do cliente.
        // então passa direto no teste de cima
        if (chavePix.tipoChave == TipoChave.CPF && repository.existsByChave(chavePix.contaAssociada.titular.cpf)) {
            throw ChavePixExistenteException("Chave CPF já cadastrada")
        }

        //salvando no banco e conectando ao client do BCB para registro
        repository.save(chavePix)
        val bcbRequest = chavePix.toBcb()
        try {
            val bcbResponse = bcbClient.cadastraChave(bcbRequest)
            if (bcbResponse.status == HttpStatus.CREATED) {
                chavePix.atualizaChave(bcbResponse.body())
            }
        } catch (e: HttpClientResponseException) {
            e.printStackTrace()
            if (e.status == HttpStatus.UNPROCESSABLE_ENTITY) throw ChavePixExistenteException("Chave pix já cadastrada no BCB")

        } catch (e: HttpClientException) {
            e.printStackTrace()
            throw IllegalStateException("Não foi possível conectar ao sistema BCB, tente mais tarde")
        }

        return chavePix
    }
}
