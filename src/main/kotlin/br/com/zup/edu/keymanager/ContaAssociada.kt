package br.com.zup.edu.keymanager

import br.com.zup.edu.TipoConta
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
class Titular(val titularId: String, val nomeTitular: String, val cpf: String) {

}

@Embeddable
class Instituicao(val nomeInstituicao: String, val ispb: String) {

}


