package br.com.zup.edu.keymanager.chavepix.carrega

import br.com.zup.edu.CarregaChavePixRequest
import br.com.zup.edu.CarregaChavePixResponse
import br.com.zup.edu.CarregaChavePixServiceGrpc
import br.com.zup.edu.keymanager.chavepix.ChavePixRepository
import br.com.zup.edu.keymanager.chavepix.client.bcb.BcbClient
import br.com.zup.edu.keymanager.chavepix.compartilhado.exceptions.handlers.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Validator

@ErrorHandler
@Singleton
class CarregaChavePixEndpoint(
    @Inject private val repository: ChavePixRepository,
    @Inject private val bcbClient: BcbClient,
    @Inject private val validator: Validator
) : CarregaChavePixServiceGrpc.CarregaChavePixServiceImplBase() {

    override fun carrega(request: CarregaChavePixRequest, responseObserver: StreamObserver<CarregaChavePixResponse>) {

        val chaveInfo = request.toModel(validator).filtra(repository, bcbClient)
        val carregaChavePixResponse: CarregaChavePixResponse = CarregaChavePixResponseConverter.toResponse(chaveInfo)

        responseObserver.onNext(carregaChavePixResponse)
        responseObserver.onCompleted()

    }
}


