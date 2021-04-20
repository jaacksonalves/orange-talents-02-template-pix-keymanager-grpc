package br.com.zup.edu.keymanager.chavepix.client.itau

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("\${itau.url}")
interface ItauErpClient {

//    @Get("/api/v1/clientes/{clienteId}/contas{tipoConta}")
    @Get("/api/v1/clientes/{clienteId}/contas?tipo={tipoConta}")
    fun buscaContaPorTipo(
        @PathVariable clienteId: String,
        @QueryValue tipoConta: String
    ): HttpResponse<ItauClientContaResponse>

}
