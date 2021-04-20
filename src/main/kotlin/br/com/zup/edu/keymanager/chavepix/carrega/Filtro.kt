package br.com.zup.edu.keymanager.chavepix.carrega

import br.com.zup.edu.keymanager.chavepix.ChavePixRepository
import br.com.zup.edu.keymanager.chavepix.carrega.ChavePixInfo.Companion.toInfo
import br.com.zup.edu.keymanager.chavepix.client.bcb.BcbClient
import br.com.zup.edu.keymanager.chavepix.compartilhado.exceptions.ChavePixNaoEncontradaException
import br.com.zup.edu.keymanager.chavepix.validacao.ValidUUID
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientException
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Introspected
sealed class Filtro {

    abstract fun filtra(repository: ChavePixRepository, bcbClient: BcbClient): ChavePixInfo


    @Introspected
    data class PorPixEClienteId(
        @field:NotBlank @field:ValidUUID val clienteId: String,
        @field:NotBlank @field:ValidUUID val pixId: String
    ) : Filtro() {
        override fun filtra(repository: ChavePixRepository, bcbClient: BcbClient): ChavePixInfo {
            val chavePix =
                repository.findByIdAndContaAssociadaTitularTitularId(UUID.fromString(pixId), UUID.fromString(clienteId))
                    .orElseThrow {
                        ChavePixNaoEncontradaException("Chave Pix não encontrada")
                    }

            try {
                bcbClient.carregaPorChave(chavePix.chave!!)
            } catch (e: Exception) {
                throw IllegalStateException("Não foi possível consultar a chave no BCB, tente novamente")
            }

            return chavePix.toInfo()
        }
    }


    @Introspected
    data class PorChave(@field:NotBlank @field:Size(max = 77) val chave: String) : Filtro() {
        override fun filtra(repository: ChavePixRepository, bcbClient: BcbClient): ChavePixInfo {
            if (!conecta(bcbClient, chave)) {
                throw IllegalStateException("Chave não encontrada no BCB, tente novamente")
            }

            if (repository.existsByChave(chave)) {
                return repository.findByChave(chave).get().toInfo()
            }
            return bcbClient.carregaPorChave(chave).body()!!.toInfo()
        }
    }


    @Introspected
    object Invalido : Filtro() {
        override fun filtra(repository: ChavePixRepository, bcbClient: BcbClient): ChavePixInfo {
            throw IllegalArgumentException("Chave Pix inválida ou não informada")
        }
    }

}


// tenta conexão com BcbClient e retorna erro ou false caso não consiga conectar
fun conecta(bcbClient: BcbClient, chave: String): Boolean {
    try {
        if (bcbClient.carregaPorChave(chave).status == HttpStatus.OK) return true

    } catch (e: HttpClientException) {
        throw IllegalStateException("Não foi possível consultar a chave no BCB, tente novamente")

    }
    return false
}



