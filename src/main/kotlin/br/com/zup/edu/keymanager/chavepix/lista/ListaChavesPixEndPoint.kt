package br.com.zup.edu.keymanager.chavepix.lista

import br.com.zup.edu.*
import br.com.zup.edu.keymanager.chavepix.ChavePixRepository
import br.com.zup.edu.keymanager.chavepix.compartilhado.exceptions.handlers.ErrorHandler
import com.google.protobuf.Timestamp
import io.grpc.stub.StreamObserver
import java.time.ZoneId
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class ListaChavesPixEndPoint(@Inject private val repository: ChavePixRepository) :
    ListaChavePixServiceGrpc.ListaChavePixServiceImplBase() {

    override fun lista(request: ListaChavePixRequest, responseObserver: StreamObserver<ListaChavePixResponse>) {

        if (request.clienteId.isNullOrBlank()) {
            throw IllegalArgumentException("Deve preencher corretamente ClienteId")
        }
        if (!repository.existsByContaAssociadaTitularTitularId(UUID.fromString(request.clienteId))) {
            throw java.lang.IllegalArgumentException("Cliente não encontrado")
        }

        val chavesPorId = repository.findAllByContaAssociadaTitularTitularId(UUID.fromString(request.clienteId))

        val chavesResponse = chavesPorId.map {
            ListaChavePixResponse.ChavePixLista.newBuilder()
                .setPixId(it.id.toString())
                .setTipoChave(TipoChave.valueOf(it.tipoChave.name))
                .setChave(it.chave)
                .setTipoConta(TipoConta.valueOf(it.contaAssociada.tipoConta.name))
                .setCriadaEm(it.criadoEm.let {
                    val createdAt = it.atZone(ZoneId.of("GMT-3")).toInstant()
                    Timestamp.newBuilder()
                        .setSeconds(createdAt.epochSecond)
                        .setNanos(createdAt.nano)
                        .build()
                })
                .build()
        }

        responseObserver.onNext(
            ListaChavePixResponse.newBuilder()
                .setClienteId(request.clienteId)
                .addAllChaves(chavesResponse)
                .build()
        )
        responseObserver.onCompleted()
    }


}