package br.com.zup.edu.keymanager.chavepix.client.bcb

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client

@Client("\${bcb.url}")
interface BcbClient {

    @Post("/api/v1/pix/keys", processes = [MediaType.APPLICATION_XML])
    fun cadastraChave(@Body request: CreatePixKeyRequest): HttpResponse<CreatePixKeyResponse>


    @Delete("/api/v1/pix/keys/{key}", processes = [MediaType.APPLICATION_XML])
    fun deletaChave(@PathVariable key: String, @Body request: DeletePixKeyRequest): HttpResponse<DeletePixKeyResponse>


    @Get("/api/v1/pix/keys/{key}", processes = [MediaType.APPLICATION_XML])
    fun carregaPorChave(@PathVariable key: String): HttpResponse<PixDetailResponse>

}