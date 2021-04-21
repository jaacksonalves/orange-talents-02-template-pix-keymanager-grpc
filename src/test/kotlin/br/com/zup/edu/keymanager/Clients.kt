package br.com.zup.edu.keymanager

import br.com.zup.edu.*
import io.grpc.ManagedChannel
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel

@Factory
class Clients {
    @Bean
    fun gprcRegistra(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceBlockingStub {
        return KeyManagerGrpcServiceGrpc.newBlockingStub(channel)
    }

    @Bean
    fun gprcRemove(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): RemoveChavePixServiceGrpc.RemoveChavePixServiceBlockingStub {
        return RemoveChavePixServiceGrpc.newBlockingStub(channel)
    }

    @Bean
    fun gprcCarrega(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): CarregaChavePixServiceGrpc.CarregaChavePixServiceBlockingStub {
        return CarregaChavePixServiceGrpc.newBlockingStub(channel)
    }

    @Bean
    fun gprcLista(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): ListaChavePixServiceGrpc.ListaChavePixServiceBlockingStub {
        return ListaChavePixServiceGrpc.newBlockingStub(channel)
    }
}

