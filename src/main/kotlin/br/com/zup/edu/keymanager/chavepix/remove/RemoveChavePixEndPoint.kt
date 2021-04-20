package br.com.zup.edu.keymanager.chavepix.remove

import br.com.zup.edu.RemoveChavePixRequest
import br.com.zup.edu.RemoveChavePixResponse
import br.com.zup.edu.RemoveChavePixServiceGrpc
import br.com.zup.edu.keymanager.chavepix.compartilhado.exceptions.handlers.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class RemoveChavePixEndPoint(@Inject private val service: RemoveChavePixService) :
    RemoveChavePixServiceGrpc.RemoveChavePixServiceImplBase() {

    override fun remove(request: RemoveChavePixRequest, responseObserver: StreamObserver<RemoveChavePixResponse>) {
        val chaveRemovida = service.remove(pixId = request.pixId, clienteId = request.clienteId)

        responseObserver.onNext(
            RemoveChavePixResponse.newBuilder()
                .setMensagem("Chave pix $chaveRemovida removida")
                .build()
        )

        responseObserver.onCompleted()

    }
}