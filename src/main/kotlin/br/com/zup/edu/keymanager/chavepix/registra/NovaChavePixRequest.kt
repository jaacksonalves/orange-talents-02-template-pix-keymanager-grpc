package br.com.zup.edu.keymanager.chavepix.registra


import br.com.zup.edu.TipoConta
import br.com.zup.edu.keymanager.chavepix.ChavePix
import br.com.zup.edu.keymanager.chavepix.ContaAssociada
import br.com.zup.edu.keymanager.chavepix.TipoChave
import br.com.zup.edu.keymanager.chavepix.validacao.ValidPixKey
import br.com.zup.edu.keymanager.chavepix.validacao.ValidUUID
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ValidPixKey
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
                TipoChave.ALEATORIA -> ""
                TipoChave.CPF -> contaAssociada.titular.cpf
                else -> chave!!
            },
            contaAssociada = contaAssociada,
        )
    }

}
