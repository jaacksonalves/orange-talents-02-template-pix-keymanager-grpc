package br.com.zup.edu.keymanager.registra

import br.com.zup.edu.*
import br.com.zup.edu.keymanager.compartilhado.exceptions.TipoChaveInvalidoException
import br.com.zup.edu.keymanager.compartilhado.exceptions.handlers.ErrorHandler
import br.com.zup.edu.keymanager.toModel
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class RegistraNovaChavePixEndPoint(@Inject private val service: NovaChavePixService) :
    KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceImplBase() {
    override fun registra(
        request: RegistraChavePixRequest,
        responseObserver: StreamObserver<RegistraChavePixResponse>
    ) {

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

