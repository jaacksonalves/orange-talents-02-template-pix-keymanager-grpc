package br.com.zup.edu.keymanager.remove

import br.com.zup.edu.KeyManagerGrpcServiceGrpc
import br.com.zup.edu.RemoveChavePixRequest
import br.com.zup.edu.RemoveChavePixServiceGrpc
import br.com.zup.edu.TipoConta
import br.com.zup.edu.keymanager.*
import br.com.zup.edu.keymanager.registra.RegistraNovaChavePixEndPointTest
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
            assertEquals("Dados inválidos", it.status.description)
        }

        //Com pixId não batendo com o clienteID
        assertThrows<StatusRuntimeException> {
            grpcClient.remove(
                RemoveChavePixRequest.newBuilder()
                    .setClienteId(CLIENTE_CRIADO.contaAssociada.titular.titularId.toString())
                    .setPixId("10549f7e-0808-4f71-bdea-203600b9e535")
                    .build()
            )
        }.let {
            assertEquals(Status.NOT_FOUND.code, it.status.code)
            assertEquals("Chave não encontrada ou não cadastrada para esse cliente", it.status.description)
        }


    }
}