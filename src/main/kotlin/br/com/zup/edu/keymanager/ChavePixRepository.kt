package br.com.zup.edu.keymanager

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChavePixRepository : JpaRepository<ChavePix, UUID> {
    fun existsByChave(chave: String?): Boolean

    fun existsByContaAssociadaTitularCpf(cpf: String): Boolean

    fun findByContaAssociadaTitularCpf(cpf: String): Optional<ChavePix>

    fun findByChave(chave: String): Optional<ChavePix>

    fun findByContaAssociadaTitularTitularId(id: String): Optional<ChavePix>

}
