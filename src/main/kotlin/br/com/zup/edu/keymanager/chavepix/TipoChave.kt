package br.com.zup.edu.keymanager.chavepix

import io.micronaut.validation.validator.constraints.EmailValidator

enum class TipoChave {

    UNKNOWN_CHAVE {
        override fun valida(chave: String?): Boolean {
            return false
        }
    },

    CPF {
        override fun valida(chave: String?) =
            chave.isNullOrBlank() //CPF não deve ser preenchido pois é o próprio CPF cadastrado do cliente
    },

    CELULAR {
        override fun valida(chave: String?): Boolean {
            if (chave.isNullOrBlank()) {
                return false
            }
            return chave.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())
        }
    },

    EMAIL {
        override fun valida(chave: String?): Boolean {
            if (chave.isNullOrBlank()) {
                return false
            }
            return EmailValidator().run {
                initialize(null)
                isValid(chave, null)
            }
        }
    },

    ALEATORIA {
        override fun valida(chave: String?) =
            chave.isNullOrBlank() // chave aleatória não deve ser preenchida pois é criada automaticamente
    },
    CNPJ {
        override fun valida(chave: String?): Boolean {
            return false
        }
    };

    abstract fun valida(chave: String?): Boolean




}