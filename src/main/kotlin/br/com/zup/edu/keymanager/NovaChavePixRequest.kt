package br.com.zup.edu.keymanager

import br.com.zup.edu.TipoChave
import br.com.zup.edu.TipoConta
import br.com.zup.edu.keymanager.validacao.ValidUUID
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
data class NovaChavePixRequest(
    @ValidUUID
    @field:NotBlank val clienteId: String,
    @field:NotNull val tipoChave: TipoChave,
    @field:Size(max = 77) val chave: String?,
    @field:NotNull val tipoConta: TipoConta
) {
    fun toModel(contaAssociada: ContaAssociada): ChavePix {
        return ChavePix(
            tipoChave = tipoChave,
            chave = when (tipoChave) {
                TipoChave.ALEATORIA -> UUID.randomUUID().toString()
                TipoChave.CPF -> contaAssociada.titular.cpf
                else -> chave!!
            },
            contaAssociada = contaAssociada,

        )
    }

}
