package br.com.zup.edu.keymanager

import br.com.zup.edu.*
import io.grpc.Status
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RegistraNovaChavePixEndPoint(@Inject private val service: NovaChavePixService) :
    KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceImplBase() {
    override fun registra(
        request: RegistraChavePixRequest,
        responseObserver: StreamObserver<RegistraChavePixResponse>
    ) {
        if (request.tipoConta == TipoConta.UNKNOWN_CONTA) {
            responseObserver.onError(
                Status.INVALID_ARGUMENT.withDescription("Tipo de Conta Inválido").asRuntimeException()
            )
        }
        if (request.tipoChave == TipoChave.UNKNOWN_CHAVE) {
            responseObserver.onError(
                Status.INVALID_ARGUMENT.withDescription("Tipo de Chave Inválido").asRuntimeException()
            )
        }

        val novaChavePix = request.toModel()
        val chaveCriada = service.registra(novaChavePix)

        responseObserver.onNext(
            RegistraChavePixResponse.newBuilder()
                .setPixId(chaveCriada.id.toString())
                .build()
        )

        responseObserver.onCompleted()

    }

}


