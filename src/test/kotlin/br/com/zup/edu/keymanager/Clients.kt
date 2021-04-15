package br.com.zup.edu.keymanager

import br.com.zup.edu.KeyManagerGrpcServiceGrpc
import br.com.zup.edu.RemoveChavePixServiceGrpc
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
}

