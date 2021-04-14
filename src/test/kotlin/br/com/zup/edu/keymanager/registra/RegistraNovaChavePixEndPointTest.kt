package br.com.zup.edu.keymanager.registra

import br.com.zup.edu.KeyManagerGrpcServiceGrpc
import br.com.zup.edu.RegistraChavePixRequest
import br.com.zup.edu.TipoChave
import br.com.zup.edu.TipoConta
import br.com.zup.edu.keymanager.*
import br.com.zup.edu.keymanager.client.itau.InstituicaoClientResponse
import br.com.zup.edu.keymanager.client.itau.ItauClientContaResponse
import br.com.zup.edu.keymanager.client.itau.ItauErpClient
import br.com.zup.edu.keymanager.client.itau.TitularClientResponse
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.util.*
import javax.inject.Inject


@MicronautTest(transactional = false)
internal class RegistraNovaChavePixEndPointTest(
    private val repository: ChavePixRepository,
    private val grpcClient: KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceBlockingStub
) {

    @Inject
    lateinit var itauClient: ItauErpClient

    companion object {
        val CLIENTE_ID = UUID.randomUUID()
    }

    @MockBean(ItauErpClient::class)
    fun itauClient(): ItauErpClient? {
        return Mockito.mock(ItauErpClient::class.java)
    }

    @BeforeEach
    internal fun setUp() {
        repository.deleteAll()
    }

    private fun dadosDaContaResponse(): ItauClientContaResponse {
        return ItauClientContaResponse(
            tipo = "CONTA_CORRENTE",
            instituicao = InstituicaoClientResponse("UNIBANCO ITAU SA", "ITAU_UNIBANCO_ISPB"),
            agencia = "1010",
            numero = "202020",
            titular = TitularClientResponse("Jackson Alves", "91895790034")
        )
    }

    private fun novaChavePix(): ChavePix {
        return ChavePix(
            tipoChave = br.com.zup.edu.keymanager.TipoChave.EMAIL,
            chave = "jackson@email.com",
            contaAssociada = ContaAssociada(
                tipoConta = TipoConta.CONTA_CORRENTE,
                instituicao = Instituicao(nomeInstituicao = "ITAU", ispb = "ITAU_UNIBANCO"),
                agencia = "1010",
                numeroConta = "202020",
                titular = Titular(
                    titularId = CLIENTE_ID.toString(),
                    nomeTitular = "Jackson Alves",
                    cpf = "91895790034"
                )
            )
        )
    }


    //TESTES

    @Test
    fun `deve registrar chave pix CPF`() {
        `when`(itauClient.buscaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipoConta = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        grpcClient.registra(
            RegistraChavePixRequest.newBuilder()
                .setClienteId(CLIENTE_ID.toString())
                .setTipoChave(TipoChave.CPF)
                .setTipoConta(TipoConta.CONTA_CORRENTE)
                .build()
        ).let { assertNotNull(it.pixId) }

        val chaveRegistrada = repository.findByContaAssociadaTitularCpf(dadosDaContaResponse().titular.cpf).get()

        assertNotNull(chaveRegistrada.id)
        assertEquals(dadosDaContaResponse().titular.cpf, chaveRegistrada.contaAssociada.titular.cpf)
        assertTrue(chaveRegistrada.tipoChave == br.com.zup.edu.keymanager.TipoChave.CPF)
    }

    @Test
    fun `deve registrar chave pix EMAIL`() {
        `when`(itauClient.buscaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipoConta = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        grpcClient.registra(
            RegistraChavePixRequest.newBuilder()
                .setClienteId(CLIENTE_ID.toString())
                .setTipoChave(TipoChave.EMAIL)
                .setChave("jackson@email.com")
                .setTipoConta(TipoConta.CONTA_CORRENTE)
                .build()
        ).let { assertNotNull(it.pixId) }

        val chaveRegistrada = repository.findByChave("jackson@email.com").get()

        assertNotNull(chaveRegistrada.id)
        assertEquals("jackson@email.com", chaveRegistrada.chave)
        assertTrue(chaveRegistrada.tipoChave == br.com.zup.edu.keymanager.TipoChave.EMAIL)

    }

    @Test
    fun `deve registrar chave pix CELULAR`() {
        `when`(itauClient.buscaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipoConta = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        grpcClient.registra(
            RegistraChavePixRequest.newBuilder()
                .setClienteId(CLIENTE_ID.toString())
                .setTipoChave(TipoChave.CELULAR)
                .setChave("+5534999999999")
                .setTipoConta(TipoConta.CONTA_CORRENTE)
                .build()
        ).let { assertNotNull(it.pixId) }

        val chaveRegistrada = repository.findByChave("+5534999999999").get()

        assertNotNull(chaveRegistrada.id)
        assertEquals("+5534999999999", chaveRegistrada.chave)
        assertTrue(chaveRegistrada.tipoChave == br.com.zup.edu.keymanager.TipoChave.CELULAR)

    }

    @Test
    fun `deve registrar chave pix ALEATORIA`() {
        `when`(itauClient.buscaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipoConta = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        grpcClient.registra(
            RegistraChavePixRequest.newBuilder()
                .setClienteId(CLIENTE_ID.toString())
                .setTipoChave(TipoChave.ALEATORIA)
                .setTipoConta(TipoConta.CONTA_CORRENTE)
                .build()
        ).let { assertNotNull(it.pixId) }

        val chaveRegistrada = repository.findByContaAssociadaTitularTitularId(CLIENTE_ID.toString()).get()

        assertNotNull(chaveRegistrada.id)
        assertNotNull(chaveRegistrada.chave)
        assertTrue(chaveRegistrada.tipoChave == br.com.zup.edu.keymanager.TipoChave.ALEATORIA)
    }

    @Test
    fun `NAO deve registrar chave quando ja existe uma igual`() {
        `when`(itauClient.buscaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipoConta = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        repository.save(novaChavePix())

        assertThrows<StatusRuntimeException> {
            grpcClient.registra(
                RegistraChavePixRequest.newBuilder()
                    .setClienteId(CLIENTE_ID.toString())
                    .setTipoChave(TipoChave.EMAIL)
                    .setChave("jackson@email.com")
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .build()
            )
        }.let {
            assertEquals(Status.ALREADY_EXISTS.code, it.status.code)
            assertEquals("Chave já cadastrada", it.status.description)
        }
    }

    @Test
    fun `NAO deve registrar chave pix com parametros invalidos`() {
        `when`(itauClient.buscaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipoConta = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        //enviando tudo em branco
        assertThrows<StatusRuntimeException> {
            grpcClient.registra(RegistraChavePixRequest.newBuilder().build())
        }.let {
            assertEquals(Status.INVALID_ARGUMENT.code, it.status.code)
            assertEquals("Dados inválidos", it.status.description)
        }

        //preenchendo apenas Cliente ID formato invalido
        assertThrows<StatusRuntimeException> {
            grpcClient.registra(
                RegistraChavePixRequest.newBuilder()
                    .setClienteId("CLIENTE_ID.toString()")
                    .build()
            )
        }.let {
            assertEquals(Status.INVALID_ARGUMENT.code, it.status.code)
            assertEquals("Dados inválidos", it.status.description)
        }

        //preenchendo apenas Cliente ID correto
        assertThrows<StatusRuntimeException> {
            grpcClient.registra(
                RegistraChavePixRequest.newBuilder()
                    .setClienteId(CLIENTE_ID.toString())
                    .build()
            )
        }.let {
            assertEquals(Status.INVALID_ARGUMENT.code, it.status.code)
            assertEquals("Dados inválidos", it.status.description)
        }

        //preenchendo apenas Cliente ID correto e tipo conta
        assertThrows<StatusRuntimeException> {
            grpcClient.registra(
                RegistraChavePixRequest.newBuilder()
                    .setClienteId(CLIENTE_ID.toString())
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .build()
            )
        }.let {
            assertEquals(Status.INVALID_ARGUMENT.code, it.status.code)
            assertEquals("Dados inválidos", it.status.description)
        }

        //preenchendo apenas Cliente ID correto e tipo conta e tipo chave unknown
        assertThrows<StatusRuntimeException> {
            grpcClient.registra(
                RegistraChavePixRequest.newBuilder()
                    .setClienteId(CLIENTE_ID.toString())
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .setTipoChave(TipoChave.UNKNOWN_CHAVE)
                    .build()
            )
        }.let {
            assertEquals(Status.INVALID_ARGUMENT.code, it.status.code)
            assertEquals("Dados inválidos", it.status.description)
        }

        //preenchendo apenas Cliente ID correto e tipo conta UNKNOWN  e chave correta
        assertThrows<StatusRuntimeException> {
            grpcClient.registra(
                RegistraChavePixRequest.newBuilder()
                    .setClienteId(CLIENTE_ID.toString())
                    .setTipoConta(TipoConta.UNKNOWN_CONTA)
                    .setTipoChave(TipoChave.ALEATORIA)
                    .build()
            )
        }.let {
            assertEquals(Status.INVALID_ARGUMENT.code, it.status.code)
            assertEquals("Tipo de conta inválido", it.status.description)
        }
    }

}

