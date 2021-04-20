package br.com.zup.edu.keymanager.chavepix

import br.com.zup.edu.keymanager.chavepix.client.bcb.CreatePixKeyResponse
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
class ChavePix(
    @Enumerated(EnumType.STRING)
    val tipoChave: TipoChave,

    @Column(length = 77, unique = true)
    var chave: String?,

    @Embedded
    val contaAssociada: ContaAssociada
) {

    fun atualizaChave(body: CreatePixKeyResponse?) {
        if (tipoChave == TipoChave.ALEATORIA) {
            this.chave = body!!.key
        }
    }

    //tive que criar esse ToString pro teste passar
    override fun toString(): String {
        return "ChavePix(id=$id)"
    }


    @Id
    @GeneratedValue
    val id: UUID? = null

    @Column(updatable = false)
    val criadoEm: LocalDateTime = LocalDateTime.now()


}
