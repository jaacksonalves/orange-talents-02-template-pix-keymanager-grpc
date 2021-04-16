package br.com.zup.edu.keymanager.registra

import br.com.zup.edu.KeyManagerGrpcServiceGrpc
import br.com.zup.edu.RegistraChavePixRequest
import br.com.zup.edu.TipoChave
import br.com.zup.edu.TipoConta
import br.com.zup.edu.keymanager.*
import br.com.zup.edu.keymanager.client.bcb.*
import br.com.zup.edu.keymanager.client.bcb.CreatePixKeyRequest.Companion.toBcb
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
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject


@MicronautTest(transactional = false)
internal class RegistraNovaChavePixEndPointTest(
    private val repository: ChavePixRepository,
    private val grpcClient: KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceBlockingStub
) {

    @Inject
    lateinit var itauClient: ItauErpClient

    @Inject
    lateinit var bcbClient: BcbClient

    companion object {
        val CLIENTE_ID = UUID.randomUUID()
    }

    @MockBean(ItauErpClient::class)
    fun itauClient(): ItauErpClient? {
        return Mockito.mock(ItauErpClient::class.java)
    }

    @MockBean(BcbClient::class)
    fun bcbClient(): BcbClient? {
        return Mockito.mock(BcbClient::class.java)
    }

    @BeforeEach
    internal fun setUp() {
        repository.deleteAll()
    }


    private fun createPixKeyResponse(keyType: KeyType, key: String): CreatePixKeyResponse {
        return CreatePixKeyResponse(
            keyType = keyType,
            key = key,
            bankAccount = BankAccountResponse(
                participant = "60701190",
                branch = "0001",
                accountNumber = "202020",
                accountType = AccountType.CACC
            ),
            owner = OwnerResponse(
                type = Type.NATURAL_PERSON,
                name = "Jackson Alves",
                taxIdNumber = "91895790034"
            ), createdAt = LocalDateTime.now()
        )
    }


    private fun dadosDaContaResponse(): ItauClientContaResponse {
        return ItauClientContaResponse(
            tipo = "CONTA_CORRENTE",
            instituicao = InstituicaoClientResponse("UNIBANCO ITAU SA", "60701190"),
            agencia = "0001",
            numero = "202020",
            titular = TitularClientResponse("Jackson Alves", "91895790034")
        )
    }

    private fun novaChavePixEmail(): ChavePix {
        return ChavePix(
            tipoChave = br.com.zup.edu.keymanager.TipoChave.EMAIL,
            chave = "jackson@email.com",
            contaAssociada = ContaAssociada(
                tipoConta = TipoConta.CONTA_CORRENTE,
                instituicao = Instituicao(nomeInstituicao = "ITAU", ispb = "60701190"),
                agencia = "0001",
                numeroConta = "202020",
                titular = Titular(
                    titularId = CLIENTE_ID,
                    nomeTitular = "Jackson Alves",
                    cpf = "91895790034"
                )
            )
        )
    }

    private fun novaChavePixCpf(): ChavePix {
        return ChavePix(
            tipoChave = br.com.zup.edu.keymanager.TipoChave.CPF,
            chave = "91895790034",
            contaAssociada = ContaAssociada(
                tipoConta = TipoConta.CONTA_CORRENTE,
                instituicao = Instituicao(nomeInstituicao = "ITAU", ispb = "60701190"),
                agencia = "0001",
                numeroConta = "202020",
                titular = Titular(
                    titularId = CLIENTE_ID,
                    nomeTitular = "Jackson Alves",
                    cpf = "91895790034"
                )
            )
        )
    }

    fun novaChavePixCelular(): ChavePix {
        return ChavePix(
            tipoChave = br.com.zup.edu.keymanager.TipoChave.CELULAR,
            chave = "+5534999999999",
            contaAssociada = ContaAssociada(
                tipoConta = TipoConta.CONTA_CORRENTE,
                instituicao = Instituicao(nomeInstituicao = "ITAU", ispb = "60701190"),
                agencia = "0001",
                numeroConta = "202020",
                titular = Titular(
                    titularId = CLIENTE_ID,
                    nomeTitular = "Jackson Alves",
                    cpf = "91895790034"
                )
            )
        )
    }

    fun novaChavePixAleatoria(): CreatePixKeyRequest {
        return CreatePixKeyRequest(
            keyType = KeyType.RANDOM,
            key = UUID.randomUUID().toString(),
            bankAccount = BankAccount(
                participant = "60701190",
                branch = "0001",
                accountNumber = "202020",
                accountType = AccountType.CACC
            ), owner = Owner(Type.toType("CPF"), name = "Jackson Alves", taxIdNumber = "91895790034")
        )
    }


    //TESTES

    @Test
    fun `DEVE registrar chave pix CPF`() {
        //Mock client ItauErp
        `when`(itauClient.buscaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipoConta = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        //Mock client BCB
        `when`(bcbClient.cadastraChave(novaChavePixCpf().toBcb()))
            .thenReturn(HttpResponse.ok(createPixKeyResponse(keyType = KeyType.CPF, key = "91895790034")))

        //cliente (BloomRPC por exemplo)
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
    fun `DEVE registrar chave pix EMAIL`() {
        `when`(itauClient.buscaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipoConta = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        `when`(bcbClient.cadastraChave(novaChavePixEmail().toBcb()))
            .thenReturn(HttpResponse.ok(createPixKeyResponse(keyType = KeyType.EMAIL, key = "jackson@email.com")))

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
    fun `DEVE registrar chave pix CELULAR`() {
        `when`(itauClient.buscaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipoConta = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        `when`(bcbClient.cadastraChave(novaChavePixCelular().toBcb()))
            .thenReturn(HttpResponse.ok(createPixKeyResponse(keyType = KeyType.PHONE, key = "+5534999999999")))

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
    fun `DEVE registrar chave pix ALEATORIA`() {
        `when`(itauClient.buscaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipoConta = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        `when`(bcbClient.cadastraChave(novaChavePixAleatoria()))
            .thenReturn(
                HttpResponse.ok(
                    createPixKeyResponse(
                        keyType = KeyType.RANDOM,
                        key = UUID.randomUUID().toString()
                    )
                )
            )

        grpcClient.registra(
            RegistraChavePixRequest.newBuilder()
                .setClienteId(CLIENTE_ID.toString())
                .setTipoChave(TipoChave.ALEATORIA)
                .setTipoConta(TipoConta.CONTA_CORRENTE)
                .build()
        ).let { assertNotNull(it.pixId) }

        val chaveRegistrada = repository.findByContaAssociadaTitularTitularId(CLIENTE_ID).get()

        assertNotNull(chaveRegistrada.id)
        assertNotNull(chaveRegistrada.chave)
        assertTrue(chaveRegistrada.tipoChave == br.com.zup.edu.keymanager.TipoChave.ALEATORIA)
    }

    @Test
    fun `NAO deve registrar chave quando ja existe uma igual`() {
        `when`(itauClient.buscaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipoConta = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        repository.save(novaChavePixEmail())

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
            assertEquals(
                "Chave ${br.com.zup.edu.keymanager.TipoChave.EMAIL}: jackson@email.com já cadastrada",
                it.status.description
            )
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

    @Test
    fun `NAO deve registrar nova chave CPF para chave CPF ja cadastrado`() {
        `when`(itauClient.buscaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipoConta = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        repository.save(novaChavePixCpf())

        assertThrows<StatusRuntimeException> {
            grpcClient.registra(
                RegistraChavePixRequest.newBuilder()
                    .setClienteId(CLIENTE_ID.toString())
                    .setTipoChave(TipoChave.CPF)
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .build()
            )
        }.let {
            assertEquals(Status.ALREADY_EXISTS.code, it.status.code)
            assertEquals(
                "Chave CPF já cadastrada",
                it.status.description
            )
        }

    }


}

