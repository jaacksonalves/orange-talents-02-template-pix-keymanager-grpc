package br.com.zup.edu.keymanager.chavepix.registra

import br.com.zup.edu.TipoConta
import br.com.zup.edu.keymanager.chavepix.ChavePix
import br.com.zup.edu.keymanager.chavepix.ChavePixRepository
import br.com.zup.edu.keymanager.chavepix.TipoChave
import br.com.zup.edu.keymanager.chavepix.client.bcb.BcbClient
import br.com.zup.edu.keymanager.chavepix.client.bcb.CreatePixKeyRequest.Companion.toBcb
import br.com.zup.edu.keymanager.chavepix.client.itau.ItauErpClient
import br.com.zup.edu.keymanager.chavepix.compartilhado.exceptions.ChavePixExistenteException
import br.com.zup.edu.keymanager.chavepix.compartilhado.exceptions.TipoChaveInvalidoException
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
            val contaAssociada = clientResponse.body()!!.toModel(novaChaveRequest.tipoConta, novaChaveRequest.clienteId)
            chavePix = novaChaveRequest.toModel(contaAssociada)
        } catch (e: Exception) {
            throw IllegalStateException("Sistema Itau não retornou dados, tente novamente")
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
            if (e.status == HttpStatus.UNPROCESSABLE_ENTITY) throw ChavePixExistenteException("Chave pix já cadastrada no BCB")

        } catch (e: HttpClientException) {
            throw IllegalStateException("Não foi possível conectar ao sistema BCB, tente mais tarde")
        }

        return chavePix
    }
}
