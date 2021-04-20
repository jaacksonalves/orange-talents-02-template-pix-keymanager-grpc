package br.com.zup.edu.keymanager.chavepix.remove

import br.com.zup.edu.keymanager.chavepix.ChavePixRepository
import br.com.zup.edu.keymanager.chavepix.client.bcb.BcbClient
import br.com.zup.edu.keymanager.chavepix.client.bcb.DeletePixKeyRequest
import br.com.zup.edu.keymanager.chavepix.compartilhado.exceptions.ChavePixNaoEncontradaException
import br.com.zup.edu.keymanager.chavepix.validacao.ValidUUID
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.constraints.NotBlank

@Validated
@Singleton
class RemoveChavePixService(
    @Inject private val repository: ChavePixRepository,
    @Inject private val bcbClient: BcbClient
) {

    @Transactional
    fun remove(
        @NotBlank @ValidUUID(message = "Pix Id com formato inválido") pixId: String?,
        @NotBlank @ValidUUID(message = "Cliente Id com formato inválido") clienteId: String?
    ): String {
        //Verifica se a chave existe e está cadastrada para esse cliente informado
        val chavePix =
            repository.findByIdAndContaAssociadaTitularTitularId(UUID.fromString(pixId), UUID.fromString(clienteId))
                .orElseThrow { ChavePixNaoEncontradaException("Chave não encontrada ou não cadastrada para esse cliente") }

        val valorChave = "${chavePix.tipoChave}: ${chavePix.chave}"

        try {
            bcbClient.deletaChave(
                chavePix.chave!!,
                DeletePixKeyRequest(key = chavePix.chave!!, participant = chavePix.contaAssociada.instituicao.ispb)
            )
        } catch (e: Exception) {
            throw IllegalStateException("Não foi possível deletar chave no servidor do BCB, tente novamente")
        }

        repository.delete(chavePix)

        return valorChave
    }

}
