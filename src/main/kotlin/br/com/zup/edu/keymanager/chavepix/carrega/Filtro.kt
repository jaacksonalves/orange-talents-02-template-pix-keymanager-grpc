package br.com.zup.edu.keymanager.chavepix.carrega

import br.com.zup.edu.keymanager.chavepix.ChavePix
import br.com.zup.edu.keymanager.chavepix.ChavePixRepository
import br.com.zup.edu.keymanager.chavepix.carrega.ChavePixInfo.Companion.toInfo
import br.com.zup.edu.keymanager.chavepix.client.bcb.BcbClient
import br.com.zup.edu.keymanager.chavepix.compartilhado.exceptions.ChavePixNaoEncontradaException
import br.com.zup.edu.keymanager.chavepix.validacao.ValidUUID
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpException
import io.micronaut.http.exceptions.HttpStatusException
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Introspected
sealed class Filtro {

    abstract fun filtra(repository: ChavePixRepository, bcbClient: BcbClient): ChavePixInfo


    @Introspected
    data class PorPixEClienteId(
        @field:NotBlank @field:ValidUUID(message = "ClienteID inválido") val clienteId: String,
        @field:NotBlank @field:ValidUUID(message = "PixId inválido") val pixId: String
    ) : Filtro() {
        override fun filtra(repository: ChavePixRepository, bcbClient: BcbClient): ChavePixInfo {
            val chavePix =
                repository.findByIdAndContaAssociadaTitularTitularId(UUID.fromString(pixId), UUID.fromString(clienteId))
                    .orElseThrow {
                        ChavePixNaoEncontradaException("Chave Pix não encontrada")
                    }

            try {
                bcbClient.carregaPorChave(chavePix.chave!!)
            } catch (e: HttpStatusException) {
                throw IllegalStateException("Não foi possível encontrar a chave no BCB, tente novamente")
            } catch (e: HttpException) {
                throw IllegalStateException("Não foi possível consultar a chave no BCB, tente novamente")
            }

            return chavePix.toInfo()
        }
    }


    @Introspected
    data class PorChave(@field:NotBlank(message = "Chave deve ser preenchida") @field:Size(max = 77) val chave: String) :
        Filtro() {
        override fun filtra(repository: ChavePixRepository, bcbClient: BcbClient): ChavePixInfo {
            val chavePix: ChavePix
            if (chave.isNullOrBlank()){
                throw IllegalArgumentException("Chave deve ser preenchida")
            }
            try {
                val response = bcbClient.carregaPorChave(chave)
                if (response.status == HttpStatus.OK) {
                    chavePix = repository.findByChave(chave).orElseGet {
                        bcbClient.carregaPorChave(chave).body()!!.toModel()
                    }
                } else {
                    throw IllegalArgumentException("Não foi possível encontrar a chave no BCB, tente novamente")
                }
            } catch (e: HttpStatusException) {
                throw IllegalArgumentException("Não foi possível encontrar a chave no BCB, tente novamente")
            } catch (e: HttpException) {
                throw IllegalStateException("Não foi possível consultar a chave no BCB, tente novamente")
            }
            return chavePix.toInfo()
        }

    }


    @Introspected
    object Invalido : Filtro() {
        override fun filtra(repository: ChavePixRepository, bcbClient: BcbClient): ChavePixInfo {
            throw IllegalArgumentException("Chave Pix inválida ou não informada")
        }
    }

}