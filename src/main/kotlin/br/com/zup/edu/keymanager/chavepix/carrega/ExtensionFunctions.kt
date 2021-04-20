package br.com.zup.edu.keymanager.chavepix.carrega

import br.com.zup.edu.*
import br.com.zup.edu.CarregaChavePixRequest.FiltroCase
import br.com.zup.edu.CarregaChavePixRequest.FiltroCase.*
import br.com.zup.edu.keymanager.chavepix.client.bcb.PixDetailResponse
import com.google.protobuf.Timestamp
import java.time.ZoneId
import javax.validation.ConstraintViolationException
import javax.validation.Validator

fun CarregaChavePixRequest.toModel(validator: Validator): Filtro {

    val filtro = when (filtroCase!!) {
        PIXECLIENTEID -> pixEClienteId.let {
            Filtro.PorPixEClienteId(clienteId = it.clienteId, pixId = it.pixId)
        }
        CHAVEPIX -> Filtro.PorChave(chavePix)
        FILTRO_NOT_SET -> Filtro.Invalido
    }

    val violations = validator.validate(filtro)
    if (violations.isNotEmpty()) {
        throw ConstraintViolationException(violations)
    }

    return filtro

}


class CarregaChavePixResponseConverter {
    companion object {
        fun toResponse(chaveInfo: ChavePixInfo): CarregaChavePixResponse {
            return CarregaChavePixResponse.newBuilder()
                .setClienteId(chaveInfo.clienteId?.toString() ?: "")
                .setPixId(chaveInfo.pixId?.toString() ?: "")
                .setChavePix(
                    ChavePix.newBuilder()
                        .setTipoChave(TipoChave.valueOf(chaveInfo.tipoChave.name))
                        .setChave(chaveInfo.chave)
                        .setContaInfo(
                            ContaInfo.newBuilder()
                                .setTipoConta(TipoConta.valueOf(chaveInfo.tipoConta.name))
                                .setInstitucao(chaveInfo.contaInfo.instituicao.nomeInstituicao)
                                .setNomeTitular(chaveInfo.contaInfo.titularInfo.nomeTitular)
                                .setCpfTitular(chaveInfo.contaInfo.titularInfo.cpf)
                                .setAgencia(chaveInfo.contaInfo.agencia)
                                .setNumeroConta(chaveInfo.contaInfo.numeroConta)
                                .build()
                        )
                        .setCriadaEm(chaveInfo.registradaEm.let {
                            val registradaEm = it.atZone(ZoneId.of("GMT-3")).toInstant()
                            Timestamp.newBuilder()
                                .setSeconds(registradaEm.epochSecond)
                                .setNanos(registradaEm.nano)
                                .build()
                        })
                        .build()
                )
                .build()
        }
    }
}

