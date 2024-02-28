package aniq.dev.apiusers.exception

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.*
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.orm.jpa.JpaSystemException
import org.springframework.stereotype.Component
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

private val logger = KotlinLogging.logger {}

@Component
@ControllerAdvice
class GlobalExceptionHandler: ResponseEntityExceptionHandler() {
    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        val errorCode = "${HttpStatus.BAD_REQUEST}-01"
        logger.error(
            "request=${request.getDescription(false)} --- MethodArgumentNotValidException observed  ${ex.message} - request: ${
                request.getDescription(
                    false
                )
            }", ex
        )
        val errors = ex.bindingResult.allErrors
            .map { error -> error.defaultMessage!! }
            .sorted()
        logger.info("errors: $errors")
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(
            ErrorResponse.forErrors(
                errors,
                errorCode
            )
        )
    }

    override fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        val errorCode = "${HttpStatus.BAD_REQUEST}-02"
        logger.error(
            "request=${request.getDescription(false)} --- HttpMessageNotReadableException observed  ${ex.message} - request: ${
                request.getDescription(
                    false
                )
            }", ex
        )

        val error: String = "Invalid request body"
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON)
            .body(ErrorResponse.forErrors(listOf(error), errorCode))
    }

    @ExceptionHandler(Exception::class)
    fun handleAllExceptions(ex: Exception, request: WebRequest): ResponseEntity<Any> {
        val errorCode = "${HttpStatus.INTERNAL_SERVER_ERROR}-01"
        logger.error("request=${request.getDescription(false)} --- Internal Server Error observed  error=${ex.message} ", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ErrorResponse.forErrors(
                listOf("Oh Sh*t someone made a mess here...we're working on that"),
                errorCode
            )
        )
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatch(
        ex: MethodArgumentTypeMismatchException,
        request: WebRequest
    ): ResponseEntity<Any> {
        val errorCode = "${HttpStatus.BAD_REQUEST}-03"
        logger.error(
            "request=${request.getDescription(false)} --- MethodArgumentTypeMismatchException observed  ${ex.message} - request: ${
                request.getDescription(
                    false
                )
            }", ex
        )
        val error =
            "Invalid argument type for parameter '${ex.name}'. Expected ${ex.requiredType?.simpleName}, but received ${ex.value}"
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(ErrorResponse.forErrors(listOf(error),errorCode))
    }

    @ExceptionHandler(UserNotFoundException::class)
    fun handleNotFoundException(exception: UserNotFoundException, webRequest: WebRequest): ResponseEntity<Any> {
        val errorCode = "${HttpStatus.NOT_FOUND}-01"
        logger.error("request=${webRequest.getDescription(false)} 404 not found  error=${exception.message}", exception)
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.forErrors(listOf("${exception.message}"),errorCode))
    }

    @ExceptionHandler(JpaSystemException::class)
    fun handleJpaSystemException(exception: JpaSystemException, webRequest: WebRequest): ResponseEntity<Any> {
        val errorCode = "${HttpStatus.UNPROCESSABLE_ENTITY}-01"
        logger.debug("the real cause: ${exception.mostSpecificCause}")
        logger.error("request=${webRequest.getDescription(false)} 422 failed validating data constraints for the input error=${exception.message}", exception)
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ErrorResponse.forErrors(listOf("${exception.message}"),errorCode))
    }

}

class ErrorReport(
    val code: String,
    val description: String
)

class ErrorResponse(
    val errorMessages: List<ErrorReport>
) {
    companion object {
        fun forErrors(errorMessages: List<String>, errorCode: String): ErrorResponse {
            return ErrorResponse(
                errorMessages = errorMessages.map { ErrorReport(errorCode, it) }
            )
        }
    }
}