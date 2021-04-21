package br.com.zup.edu.keymanager.lista

import br.com.zup.edu.ListaChavePixRequest
import br.com.zup.edu.ListaChavePixServiceGrpc
import br.com.zup.edu.TipoConta
import br.com.zup.edu.keymanager.chavepix.*
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.util.*

@MicronautTest(transactional = false)
internal class ListaChavesPixEndPointTest(
    private val repository: ChavePixRepository,
    private val grpcClient: ListaChavePixServiceGrpc.ListaChavePixServiceBlockingStub
) {

    companion object {
        val CLIENTE_ID = UUID.randomUUID()
        val CHAVE = UUID.randomUUID()
    }

    private fun criaChavePix(tipoChave: TipoChave, chave: String, titularId: UUID): ChavePix {
        return ChavePix(
            tipoChave = tipoChave,
            chave = chave,
            ContaAssociada(
                tipoConta = TipoConta.CONTA_CORRENTE,
                instituicao = Instituicao(
                    nomeInstituicao = "ITAU UNIBANCO SA",
                    ispb = "60701190"
                ),
                agencia = "0001",
                numeroConta = "202020",
                titular = Titular(
                    titularId = titularId,
                    nomeTitular = "JACKSON ALVES",
                    cpf = "51974893081"
                )
            )
        )
    }

    @BeforeEach
    internal fun setUp() {
        repository.save(criaChavePix(TipoChave.CPF, "51974893081", CLIENTE_ID))
        repository.save(criaChavePix(TipoChave.EMAIL, "jackson@email.com", CLIENTE_ID))
        repository.save(criaChavePix(TipoChave.CELULAR, "+5534998989898", CLIENTE_ID))
        repository.save(criaChavePix(TipoChave.ALEATORIA, CHAVE.toString(), CLIENTE_ID))
        repository.save(criaChavePix(TipoChave.ALEATORIA, UUID.randomUUID().toString(), UUID.randomUUID()))
    }

    @AfterEach
    internal fun tearDown() {
        repository.deleteAll()
    }

    //Testes que listam chaves pix de um cliente por Id
    @Test
    fun `DEVE retornar lista de chaves de um usuario`() {
        grpcClient.lista(
            ListaChavePixRequest.newBuilder()
                .setClienteId(CLIENTE_ID.toString())
                .build()
        ).let {
            assertNotNull(it)
            assertEquals(4, it.chavesCount)
            assertEquals(CLIENTE_ID.toString(), it.clienteId)
//            println("print $it")
        }

    }


    //Testes que NAO listam chaves pix por erros ou falta de chaves pro cliente
    @Test
    fun `NAO deve retornar lista de chaves sem digitar ID do cliente`() {
        assertThrows<StatusRuntimeException> {
            grpcClient.lista(
                ListaChavePixRequest.newBuilder()
                    .build()
            ).let { assertEquals(0, it.chavesCount) }
        }.let {
            assertEquals(Status.INVALID_ARGUMENT.code, it.status.code)
            assertEquals("Deve preencher corretamente ClienteId", it.status.description)
        }
    }


    @Test
    fun `NAO deve retornar lista de chaves para ID inexistente ou sem chaves cadastradas`() {
        assertThrows<StatusRuntimeException> {
            grpcClient.lista(
                ListaChavePixRequest.newBuilder()
                    .setClienteId(UUID.randomUUID().toString())
                    .build()
            ).let { assertEquals(0, it.chavesCount) }
        }.let {
            assertEquals(Status.INVALID_ARGUMENT.code, it.status.code)
            assertEquals("Cliente não encontrado", it.status.description)
        }
    }
}