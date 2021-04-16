package br.com.zup.edu.keymanager

import br.com.zup.edu.keymanager.client.bcb.CreatePixKeyResponse
import br.com.zup.edu.keymanager.client.bcb.KeyType
import io.micronaut.data.annotation.TypeDef
import io.micronaut.data.model.DataType
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
class ChavePix(
    @Enumerated(EnumType.STRING)
    val tipoChave: TipoChave,

    @Column(length = 77, unique = true)
    var chave: String,

    @Embedded
    val contaAssociada: ContaAssociada
) {
    fun atualizaChave(body: CreatePixKeyResponse?) {
        if (tipoChave == TipoChave.ALEATORIA) {
            this.chave = body!!.key
        }
    }


    @Id
    @GeneratedValue
    val id: UUID? = null

    @Column(updatable = false)
    val criadoEm: LocalDateTime = LocalDateTime.now()

}
