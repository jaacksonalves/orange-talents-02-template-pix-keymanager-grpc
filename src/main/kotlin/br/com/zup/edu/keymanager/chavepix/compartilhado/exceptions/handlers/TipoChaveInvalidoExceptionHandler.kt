package br.com.zup.edu.keymanager.chavepix.compartilhado.exceptions.handlers

import br.com.zup.edu.keymanager.chavepix.compartilhado.exceptions.TipoChaveInvalidoException
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class TipoChaveInvalidoExceptionHandler : ExceptionHandler<TipoChaveInvalidoException> {

    override fun handle(e: TipoChaveInvalidoException): ExceptionHandler.StatusWithDetails {
        return ExceptionHandler.StatusWithDetails(
            Status.INVALID_ARGUMENT
                .withDescription(e.message)
                .withCause(e)
        )
    }

    override fun supports(e: Exception): Boolean {
        return e is TipoChaveInvalidoException
    }

}
