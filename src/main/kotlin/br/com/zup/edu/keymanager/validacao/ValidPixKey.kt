package br.com.zup.edu.keymanager.validacao

import br.com.zup.edu.keymanager.registra.NovaChavePixRequest
import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import javax.inject.Singleton
import javax.validation.Constraint

@MustBeDocumented
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidPixKeyValidator::class])
annotation class ValidPixKey(val message: String = "Chave pix inválida (\${validatedValue.tipo})"){}


@Singleton
class ValidPixKeyValidator : ConstraintValidator<ValidPixKey, NovaChavePixRequest> {
    override fun isValid(
        value: NovaChavePixRequest?,
        annotationMetadata: AnnotationValue<ValidPixKey>,
        context: ConstraintValidatorContext
    ): Boolean {
        if (value?.tipoChave == null){
            return false
        }
        return value.tipoChave.valida(value.chave)
    }
}