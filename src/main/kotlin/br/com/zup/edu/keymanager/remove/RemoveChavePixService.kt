package br.com.zup.edu.keymanager.remove

import br.com.zup.edu.keymanager.ChavePixRepository
import br.com.zup.edu.keymanager.compartilhado.exceptions.ChavePixNaoEncontradaException
import br.com.zup.edu.keymanager.validacao.ValidUUID
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.constraints.NotBlank

@Validated
@Singleton
class RemoveChavePixService(@Inject private val repository: ChavePixRepository) {

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

        repository.delete(chavePix)

        return valorChave
    }

}
