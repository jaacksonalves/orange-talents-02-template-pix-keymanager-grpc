package br.com.zup.edu.keymanager.chavepix

import br.com.zup.edu.TipoConta
import java.util.*
import javax.persistence.Embeddable
import javax.persistence.Embedded
import javax.persistence.EnumType
import javax.persistence.Enumerated

@Embeddable
class ContaAssociada(
    @Enumerated(EnumType.STRING)
    val tipoConta: TipoConta,
    @Embedded
    val instituicao: Instituicao,
    val agencia: String,
    val numeroConta: String,
    @Embedded
    val titular: Titular
) {
}


@Embeddable
class Titular(var titularId: UUID?, var nomeTitular: String, var cpf: String) {

}

@Embeddable
class Instituicao(var nomeInstituicao: String, var ispb: String) {

}


