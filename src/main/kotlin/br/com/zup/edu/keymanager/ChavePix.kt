package br.com.zup.edu.keymanager

import br.com.zup.edu.TipoChave
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
class ChavePix(
    @Enumerated(EnumType.STRING)
    val tipoChave: TipoChave,

    @Column(length = 77, unique = true)
    val chave: String,

    @Embedded
    val contaAssociada: ContaAssociada
) {
    @Id
    @GeneratedValue
    val id: UUID? = null

    @Column(updatable = false)
    val criadoEm: LocalDateTime = LocalDateTime.now()

}
