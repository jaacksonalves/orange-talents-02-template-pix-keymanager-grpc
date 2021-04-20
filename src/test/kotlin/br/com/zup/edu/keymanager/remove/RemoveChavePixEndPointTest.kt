package br.com.zup.edu.keymanager.remove

import br.com.zup.edu.RemoveChavePixRequest
import br.com.zup.edu.RemoveChavePixServiceGrpc
import br.com.zup.edu.TipoConta
import br.com.zup.edu.keymanager.chavepix.*
import br.com.zup.edu.keymanager.chavepix.client.bcb.BcbClient
import br.com.zup.edu.keymanager.chavepix.client.bcb.DeletePixKeyRequest
import br.com.zup.edu.keymanager.chavepix.client.bcb.DeletePixKeyResponse
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class RemoveChavePixEndPointTest(
    private val repository: ChavePixRepository,
    private val grpcClient: RemoveChavePixServiceGrpc.RemoveChavePixServiceBlockingStub
) {

    companion object {
        val CLIENTE_ID = UUID.randomUUID()
    }

    private lateinit var CLIENTE_CRIADO: ChavePix

    @Inject
    lateinit var bcbClient: BcbClient

    @MockBean(BcbClient::class)
    fun bcbClient(): BcbClient? {
        return Mockito.mock(BcbClient::class.java)
    }

    @BeforeEach
    internal fun setUp() {
        CLIENTE_CRIADO = repository.save(
            ChavePix(
                tipoChave = TipoChave.CPF,
                chave = "91895790034",
                contaAssociada = ContaAssociada(
                    tipoConta = TipoConta.CONTA_CORRENTE,
                    instituicao = Instituicao(nomeInstituicao = "ITAU", ispb = "ITAU_UNIBANCO"),
                    agencia = "1010",
                    numeroConta = "202020",
                    titular = Titular(
                        titularId = CLIENTE_ID,
                        nomeTitular = "Jackson Alves",
                        cpf = "91895790034"
                    )
                )
            )
        )
    }

    @AfterEach
    internal fun tearDown() {
        repository.deleteAll()
    }

    //TESTES

    @Test
    fun `DEVE remover chave pix com pixId e titularId`() {
        `when`(
            bcbClient.deletaChave(
                key = CLIENTE_CRIADO.chave!!,
                request = DeletePixKeyRequest(
                    key = CLIENTE_CRIADO.chave!!,
                    participant = CLIENTE_CRIADO.contaAssociada.instituicao.ispb
                )
            )
        ).thenReturn(
            HttpResponse.ok(
                DeletePixKeyResponse(
                    key = CLIENTE_CRIADO.chave!!,
                    participant = CLIENTE_CRIADO.contaAssociada.instituicao.ispb,
                    deletedAt = LocalDateTime.now()
                )
            )
        )

        val mensagem = "${CLIENTE_CRIADO.tipoChave}: ${CLIENTE_CRIADO.chave}"

        grpcClient.remove(
            RemoveChavePixRequest.newBuilder()
                .setClienteId(CLIENTE_CRIADO.contaAssociada.titular.titularId.toString())
                .setPixId(CLIENTE_CRIADO.id.toString())
                .build()
        ).let {
            assertEquals(0, repository.findAll().size)
            assertEquals("Chave pix $mensagem removida", it.mensagem)
        }
    }

    @Test
    fun `NAO deve remover chave pix com parametros invalidos`() {
        `when`(
            bcbClient.deletaChave(
                key = CLIENTE_CRIADO.chave!!,
                request = DeletePixKeyRequest(
                    key = CLIENTE_CRIADO.chave!!,
                    participant = CLIENTE_CRIADO.contaAssociada.instituicao.ispb
                )
            )
        ).thenReturn(
            HttpResponse.ok(
                DeletePixKeyResponse(
                    key = CLIENTE_CRIADO.chave!!,
                    participant = CLIENTE_CRIADO.contaAssociada.instituicao.ispb,
                    deletedAt = LocalDateTime.now()
                )
            )
        )

        //Com clienteID formato invalido
        assertThrows<StatusRuntimeException> {
            grpcClient.remove(
                RemoveChavePixRequest.newBuilder()
                    .setClienteId("ID ERRADO")
                    .setPixId(CLIENTE_CRIADO.id.toString())
                    .build()
            )
        }.let {
            assertEquals(Status.INVALID_ARGUMENT.code, it.status.code)
            assertEquals(1, repository.findAll().size)
            assertEquals("Dados inválidos", it.status.description)
        }

        //Com pixId formato invalido
        assertThrows<StatusRuntimeException> {
            grpcClient.remove(
                RemoveChavePixRequest.newBuilder()
                    .setClienteId(CLIENTE_CRIADO.contaAssociada.titular.titularId.toString())
                    .setPixId("ID ERRADO")
                    .build()
            )
        }.let {
            assertEquals(Status.INVALID_ARGUMENT.code, it.status.code)
            assertEquals(1, repository.findAll().size)
            assertEquals("Dados inválidos", it.status.description)
        }

    }

    @Test
    fun `NAO  deve remover chave pix sem dados no request`() {
        `when`(
            bcbClient.deletaChave(
                key = CLIENTE_CRIADO.chave!!,
                request = DeletePixKeyRequest(
                    key = CLIENTE_CRIADO.chave!!,
                    participant = CLIENTE_CRIADO.contaAssociada.instituicao.ispb
                )
            )
        ).thenReturn(
            HttpResponse.ok(
                DeletePixKeyResponse(
                    key = CLIENTE_CRIADO.chave!!,
                    participant = CLIENTE_CRIADO.contaAssociada.instituicao.ispb,
                    deletedAt = LocalDateTime.now()
                )
            )
        )

        assertThrows<StatusRuntimeException> {
            grpcClient.remove(RemoveChavePixRequest.newBuilder().build())
        }.let {
            assertEquals(Status.INVALID_ARGUMENT.code, it.status.code)
            assertEquals(1, repository.findAll().size)
            assertEquals("Dados inválidos", it.status.description)
        }
    }

    @Test
    internal fun `NAO deve remover chave pix se id pix nao pretence id titular`() {
        `when`(
            bcbClient.deletaChave(
                key = CLIENTE_CRIADO.chave!!,
                request = DeletePixKeyRequest(
                    key = CLIENTE_CRIADO.chave!!,
                    participant = CLIENTE_CRIADO.contaAssociada.instituicao.ispb
                )
            )
        ).thenReturn(
            HttpResponse.ok(
                DeletePixKeyResponse(
                    key = CLIENTE_CRIADO.chave!!,
                    participant = CLIENTE_CRIADO.contaAssociada.instituicao.ispb,
                    deletedAt = LocalDateTime.now()
                )
            )
        )

        assertThrows<StatusRuntimeException> {
            grpcClient.remove(
                RemoveChavePixRequest.newBuilder()
                    .setClienteId(CLIENTE_CRIADO.contaAssociada.titular.titularId.toString())
                    .setPixId("10549f7e-0808-4f71-bdea-203600b9e535")
                    .build()
            )
        }.let {
            assertEquals(Status.NOT_FOUND.code, it.status.code)
            assertEquals(1, repository.findAll().size)
            assertEquals("Chave não encontrada ou não cadastrada para esse cliente", it.status.description)
        }
    }

    @Test
    fun `NAO deve remover chave pix quando algum erro do BCBClient`() {
        `when`(
            bcbClient.deletaChave(
                key = CLIENTE_CRIADO.chave!!,
                request = DeletePixKeyRequest(
                    key = CLIENTE_CRIADO.chave!!,
                    participant = CLIENTE_CRIADO.contaAssociada.instituicao.ispb
                )
            )
        ).thenThrow(HttpClientException("error"))


        assertThrows<StatusRuntimeException> {
            grpcClient.remove(
                RemoveChavePixRequest.newBuilder()
                    .setClienteId(CLIENTE_CRIADO.contaAssociada.titular.titularId.toString())
                    .setPixId(CLIENTE_CRIADO.id.toString())
                    .build()
            )
        }.let {
            assertEquals(1, repository.findAll().size)
            assertEquals("Não foi possível deletar chave no servidor do BCB, tente novamente", it.status.description)
        }
    }
}