package br.com.zup.edu.keymanager.chavepix

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class TipoChaveTest{

    @Nested
    inner class ChaveAleatoriaTest {

        @Test
        fun `DEVE ser valido quando chave ALEATORIA for nula ou vazia`() {
            val tipoChave = TipoChave.ALEATORIA

            assertTrue(tipoChave.valida(null))
            assertTrue(tipoChave.valida(""))
        }


        @Test
        fun `NAO deve ser valido quando chave ALEATORIA possuir valor`() {
            val tipoChave = TipoChave.ALEATORIA

            assertFalse(tipoChave.valida("chave"))
        }
    }


    @Nested
    inner class ChaveCpfTest{

        @Test
        fun `DEVE ser valido quando chave CPF for nula ou vazia`() {
            val tipoChave = TipoChave.CPF

            assertTrue(tipoChave.valida(null))
            assertTrue(tipoChave.valida(""))
        }


        @Test
        fun `NAO deve ser valido quando chave CPF possuir valor`() {
            val tipoChave = TipoChave.CPF

            assertFalse(tipoChave.valida("chave"))
        }

    }


    @Nested
    inner class ChaveCelularTest{

        @Test
        fun `DEVE ser valido quando tiver preenchimento E sem erros`(){
            val tipoChave = TipoChave.CELULAR

            assertTrue(tipoChave.valida("+5534998989898"))
            assertTrue(tipoChave.valida("+5511988558855"))
        }


        @Test
        fun `NAO deve ser valido quando nao for um numero de celular correto e completo`(){
            val tipoChave = TipoChave.CELULAR

            assertFalse(tipoChave.valida("teste"))
            assertFalse(tipoChave.valida(""))
            assertFalse(tipoChave.valida(null))
            assertFalse(tipoChave.valida("5534998989898"))
            assertFalse(tipoChave.valida("+55349989a9898"))
        }
    }


    @Nested
    inner class ChaveEmailTest{

        @Test
        fun `DEVE ser valido quando tiver preenchimento E sem erros`(){
            val tipoChave = TipoChave.EMAIL

            assertTrue(tipoChave.valida("teste@email"))
            assertTrue(tipoChave.valida("Teste@Email.com"))
        }


        @Test
        fun `NAO deve ser valido quando nao for um email invalido ou incompleto ou vazio`(){
            val tipoChave = TipoChave.EMAIL

            assertFalse(tipoChave.valida("teste"))
            assertFalse(tipoChave.valida(""))
            assertFalse(tipoChave.valida(null))
            assertFalse(tipoChave.valida("testeemail.com"))
            assertFalse(tipoChave.valida("@email.com"))
        }
    }
}