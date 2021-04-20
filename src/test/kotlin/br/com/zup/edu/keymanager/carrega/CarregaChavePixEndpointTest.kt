package br.com.zup.edu.keymanager.carrega

import br.com.zup.edu.CarregaChavePixRequest
import br.com.zup.edu.CarregaChavePixServiceGrpc
import br.com.zup.edu.TipoConta
import br.com.zup.edu.keymanager.chavepix.*
import br.com.zup.edu.keymanager.chavepix.client.bcb.*
import com.github.dockerjava.zerodep.shaded.org.apache.hc.client5.http.HttpResponseException
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.http.exceptions.HttpException
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class CarregaChavePixEndpointTest(
    private val repository: ChavePixRepository,
    private val grpcClient: CarregaChavePixServiceGrpc.CarregaChavePixServiceBlockingStub
) {
    @Inject
    lateinit var bcbClient: BcbClient

    lateinit var chaveCpf: ChavePix
    lateinit var chaveEmail: ChavePix
    lateinit var chaveCelular: ChavePix
    lateinit var chaveAleatoria: ChavePix

    @MockBean(BcbClient::class)
    fun bcbClient(): BcbClient? {
        return Mockito.mock(BcbClient::class.java)
    }

    companion object {
        val CLIENTE_ID = UUID.randomUUID()
        val CHAVE_ALEATORIA = UUID.randomUUID()

    }

    private fun criaChavePix(tipoChave: TipoChave, chave: String): ChavePix {
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
                    titularId = CLIENTE_ID,
                    nomeTitular = "JACKSON ALVES",
                    cpf = "51974893081"
                )
            )
        )
    }

    private fun criaPixDetailResponse(chavePix: ChavePix): PixDetailResponse {
        return PixDetailResponse(
            keyType = KeyType.toKeyType(chavePix.tipoChave),
            key = chavePix.chave!!,
            bankAccount = BankAccountResponse(
                participant = chavePix.contaAssociada.instituicao.ispb,
                branch = chavePix.contaAssociada.agencia,
                accountNumber = chavePix.contaAssociada.numeroConta,
                accountType = AccountType.toAccountType(chavePix.contaAssociada.tipoConta)
            ),
            owner = OwnerResponse(
                type = Type.toType("CPF"),
                name = chavePix.contaAssociada.titular.nomeTitular,
                taxIdNumber = chavePix.contaAssociada.titular.cpf
            ), createdAt = chavePix.criadoEm
        )
    }

    @BeforeEach
    internal fun setUp() {
        chaveCpf = repository.save(criaChavePix(TipoChave.CPF, "51974893081"))
        chaveEmail = repository.save(criaChavePix(TipoChave.EMAIL, "jackson@email.com"))
        chaveCelular = repository.save(criaChavePix(TipoChave.CELULAR, "+5534998989898"))
        chaveAleatoria = repository.save(criaChavePix(TipoChave.ALEATORIA, CHAVE_ALEATORIA.toString()))
    }

    @AfterEach
    internal fun tearDown() {
        repository.deleteAll()
    }


    //Testes que devem carregar chave com sucesso
    @Test
    fun `DEVE carregar chave pix CPF por ID do titular e ID do pix`() {
        `when`(bcbClient.carregaPorChave(chaveCpf.chave!!))
            .thenReturn(HttpResponse.ok(criaPixDetailResponse(chaveCpf)))

        grpcClient.carrega(
            CarregaChavePixRequest.newBuilder()
                .setPixEClienteId(
                    CarregaChavePixRequest.FiltroPorPixEClienteId.newBuilder()
                        .setClienteId(chaveCpf.contaAssociada.titular.titularId.toString())
                        .setPixId(chaveCpf.id.toString())
                        .build()
                )
                .build()
        ).let {
            assertEquals(chaveCpf.id.toString(), it.pixId)
            assertEquals(chaveCpf.contaAssociada.titular.titularId.toString(), it.clienteId)
            assertEquals(chaveCpf.tipoChave.name, it.chavePix.tipoChave.name)
            assertEquals(chaveCpf.chave, it.chavePix.chave)
        }
    }


    @Test
    fun `DEVE carregar chave pix EMAIL por ID do titular e ID do pix`() {
        `when`(bcbClient.carregaPorChave(chaveEmail.chave!!))
            .thenReturn(HttpResponse.ok(criaPixDetailResponse(chaveEmail)))

        grpcClient.carrega(
            CarregaChavePixRequest.newBuilder()
                .setPixEClienteId(
                    CarregaChavePixRequest.FiltroPorPixEClienteId.newBuilder()
                        .setClienteId(chaveEmail.contaAssociada.titular.titularId.toString())
                        .setPixId(chaveEmail.id.toString())
                        .build()
                )
                .build()
        ).let {
            assertEquals(chaveEmail.id.toString(), it.pixId)
            assertEquals(chaveEmail.contaAssociada.titular.titularId.toString(), it.clienteId)
            assertEquals(chaveEmail.tipoChave.name, it.chavePix.tipoChave.name)
            assertEquals(chaveEmail.chave, it.chavePix.chave)
        }
    }


    @Test
    fun `DEVE carregar chave pix CELULAR por ID do titular e ID do pix`() {
        `when`(bcbClient.carregaPorChave(chaveCelular.chave!!))
            .thenReturn(HttpResponse.ok(criaPixDetailResponse(chaveCelular)))

        grpcClient.carrega(
            CarregaChavePixRequest.newBuilder()
                .setPixEClienteId(
                    CarregaChavePixRequest.FiltroPorPixEClienteId.newBuilder()
                        .setClienteId(chaveCelular.contaAssociada.titular.titularId.toString())
                        .setPixId(chaveCelular.id.toString())
                        .build()
                )
                .build()
        ).let {
            assertEquals(chaveCelular.id.toString(), it.pixId)
            assertEquals(chaveCelular.contaAssociada.titular.titularId.toString(), it.clienteId)
            assertEquals(chaveCelular.tipoChave.name, it.chavePix.tipoChave.name)
            assertEquals(chaveCelular.chave, it.chavePix.chave)
        }
    }


    @Test
    fun `DEVE carregar chave pix ALEATORIA por ID do titular e ID do pix`() {
        `when`(bcbClient.carregaPorChave(chaveAleatoria.chave!!))
            .thenReturn(HttpResponse.ok(criaPixDetailResponse(chaveAleatoria)))

        grpcClient.carrega(
            CarregaChavePixRequest.newBuilder()
                .setPixEClienteId(
                    CarregaChavePixRequest.FiltroPorPixEClienteId.newBuilder()
                        .setClienteId(chaveAleatoria.contaAssociada.titular.titularId.toString())
                        .setPixId(chaveAleatoria.id.toString())
                        .build()
                )
                .build()
        ).let {
            assertEquals(chaveAleatoria.id.toString(), it.pixId)
            assertEquals(chaveAleatoria.contaAssociada.titular.titularId.toString(), it.clienteId)
            assertEquals(chaveAleatoria.tipoChave.name, it.chavePix.tipoChave.name)
            assertEquals(chaveAleatoria.chave, it.chavePix.chave)
        }
    }


    @Test
    fun `DEVE carregar chave pix CPF por valor da chave`() {
        `when`(bcbClient.carregaPorChave(chaveCpf.chave!!))
            .thenReturn(HttpResponse.ok(criaPixDetailResponse(chaveCpf)))

        grpcClient.carrega(
            CarregaChavePixRequest.newBuilder()
                .setChavePix(chaveCpf.chave)
                .build()
        ).let {
            assertEquals(chaveCpf.id.toString(), it.pixId)
            assertEquals(chaveCpf.contaAssociada.titular.titularId.toString(), it.clienteId)
            assertEquals(chaveCpf.tipoChave.name, it.chavePix.tipoChave.name)
            assertEquals(chaveCpf.chave, it.chavePix.chave)
        }
    }


    @Test
    fun `DEVE carregar chave pix EMAIL por valor da chave`() {
        `when`(bcbClient.carregaPorChave(chaveEmail.chave!!))
            .thenReturn(HttpResponse.ok(criaPixDetailResponse(chaveEmail)))

        grpcClient.carrega(
            CarregaChavePixRequest.newBuilder()
                .setChavePix(chaveEmail.chave)
                .build()
        ).let {
            assertEquals(chaveEmail.id.toString(), it.pixId)
            assertEquals(chaveEmail.contaAssociada.titular.titularId.toString(), it.clienteId)
            assertEquals(chaveEmail.tipoChave.name, it.chavePix.tipoChave.name)
            assertEquals(chaveEmail.chave, it.chavePix.chave)
        }
    }


    @Test
    fun `DEVE carregar chave pix CELULAR por valor da chave`() {
        `when`(bcbClient.carregaPorChave(chaveCelular.chave!!))
            .thenReturn(HttpResponse.ok(criaPixDetailResponse(chaveCelular)))

        grpcClient.carrega(
            CarregaChavePixRequest.newBuilder()
                .setChavePix(chaveCelular.chave)
                .build()
        ).let {
            assertEquals(chaveCelular.id.toString(), it.pixId)
            assertEquals(chaveCelular.contaAssociada.titular.titularId.toString(), it.clienteId)
            assertEquals(chaveCelular.tipoChave.name, it.chavePix.tipoChave.name)
            assertEquals(chaveCelular.chave, it.chavePix.chave)
        }
    }


    @Test
    fun `DEVE carregar chave pix ALEATORIA por valor da chave`() {
        `when`(bcbClient.carregaPorChave(chaveAleatoria.chave!!))
            .thenReturn(HttpResponse.ok(criaPixDetailResponse(chaveAleatoria)))

        grpcClient.carrega(
            CarregaChavePixRequest.newBuilder()
                .setChavePix(chaveAleatoria.chave)
                .build()
        ).let {
            assertEquals(chaveAleatoria.id.toString(), it.pixId)
            assertEquals(chaveAleatoria.contaAssociada.titular.titularId.toString(), it.clienteId)
            assertEquals(chaveAleatoria.tipoChave.name, it.chavePix.tipoChave.name)
            assertEquals(chaveAleatoria.chave, it.chavePix.chave)
        }
    }


    @Test
    fun `DEVE carregar chave pix por valor chave quando nao existir no banco mas existir no BcbClient`() {
        val chaveTest = criaChavePix(TipoChave.EMAIL, "chave@email.com")

        `when`(bcbClient.carregaPorChave(chaveTest.chave!!))
            .thenReturn(HttpResponse.ok(criaPixDetailResponse(chaveTest)))

        grpcClient.carrega(
            CarregaChavePixRequest.newBuilder()
                .setChavePix(chaveTest.chave)
                .build()
        ).let {
            assertEquals("", it.pixId)
            assertEquals("", it.clienteId)
            assertEquals(chaveTest.tipoChave.name, it.chavePix.tipoChave.name)
            assertEquals(chaveTest.chave, it.chavePix.chave)
        }

        assertTrue(repository.findByChave(chaveTest.chave!!).isEmpty)

    }


    //Testes de cenários onde não devem carregar uma chave
    @Test
    fun `NAO deve carregar chave pix por ID caso NAO exista no BCB`() {
        `when`(bcbClient.carregaPorChave(chaveCpf.chave!!))
            .thenThrow(HttpStatusException(HttpStatus.NOT_FOUND, "erro"))

        assertThrows<StatusRuntimeException> {
            grpcClient.carrega(
                CarregaChavePixRequest.newBuilder()
                    .setPixEClienteId(
                        CarregaChavePixRequest.FiltroPorPixEClienteId.newBuilder()
                            .setClienteId(chaveCpf.contaAssociada.titular.titularId.toString())
                            .setPixId(chaveCpf.id.toString())
                            .build()
                    )
                    .build()
            )
        }.let {
            assertEquals(Status.FAILED_PRECONDITION.code, it.status.code)
            assertEquals("Não foi possível encontrar a chave no BCB, tente novamente", it.status.description)
        }

    }


    @Test
    fun `NAO deve carregar chave pix por valor chave caso NAO exista no BCB`() {
        `when`(bcbClient.carregaPorChave(chaveCpf.chave!!))
            .thenThrow(HttpStatusException(HttpStatus.NOT_FOUND, "erro"))

        assertThrows<StatusRuntimeException> {
            grpcClient.carrega(
                CarregaChavePixRequest.newBuilder()
                    .setChavePix(chaveCpf.chave)
                    .build()
            )
        }.let {
            assertEquals(Status.INVALID_ARGUMENT.code, it.status.code)
            assertEquals("Não foi possível encontrar a chave no BCB, tente novamente", it.status.description)
        }
    }


    @Test
    fun `NAO deve carregar chave pix por valor chave caso NAO conecte ao BCB`() {
        `when`(bcbClient.carregaPorChave(chaveCpf.chave!!))
            .thenThrow(HttpClientException("ERRO"))

        assertThrows<StatusRuntimeException> {
            grpcClient.carrega(
                CarregaChavePixRequest.newBuilder()
                    .setChavePix(chaveCpf.chave)
                    .build()
            )
        }.let {
            assertEquals(Status.FAILED_PRECONDITION.code, it.status.code)
            assertEquals("Não foi possível consultar a chave no BCB, tente novamente", it.status.description)
        }
    }


    @Test
    fun `NAO deve carregar chave pix por ID caso erro de preenchimento`() {
        assertThrows<StatusRuntimeException> {
            grpcClient.carrega(
                CarregaChavePixRequest.newBuilder()
                    .setPixEClienteId(
                        CarregaChavePixRequest.FiltroPorPixEClienteId.newBuilder()
                            .setPixId("")
                            .setClienteId("")
                            .build()
                    )
                    .build()
            )
        }.let {
            assertEquals(Status.INVALID_ARGUMENT.code, it.status.code)
        }
    }


    @Test
    fun `NAO deve carregar chave pix por chave caso erro de preenchimento`() {
        assertThrows<StatusRuntimeException> {
            grpcClient.carrega(
                CarregaChavePixRequest.newBuilder()
                    .setChavePix("")
                    .build()
            )
        }.let {
            assertEquals(Status.INVALID_ARGUMENT.code, it.status.code)
            assertEquals("Chave deve ser preenchida", it.status.description)
        }
    }


    @Test
    fun `NAO deve carregar chave pix caso nao haja nenhum preenchimento`() {

        assertThrows<StatusRuntimeException> {
            grpcClient.carrega(CarregaChavePixRequest.newBuilder().build())
        }.let {
            assertEquals(Status.INVALID_ARGUMENT.code, it.status.code)
            assertEquals("Chave Pix inválida ou não informada", it.status.description)
        }
    }

}