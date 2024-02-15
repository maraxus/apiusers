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
        logger.error("request=${request.getDescription(false)} --- MethodArgumentNotValidException observed  ${ex.message} - request: ${request.getDescription(false)}", ex)
        val errors = ex.bindingResult.allErrors
            .map { error -> error.defaultMessage!! }
            .sorted()
        logger.info("errors: $errors")
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(object {val errors = errors})
    }

    override fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        logger.error(
            "request=${request.getDescription(false)} --- HttpMessageNotReadableException observed  ${ex.message} - request: ${
                request.getDescription(
                    false
                )
            }", ex
        )

        val error: String = "Invalid request body"
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(object {
            val errors: List<String> = listOf(error)
        })
    }

    @ExceptionHandler(Exception::class)
    fun handleAllExceptions(ex: Exception, request: WebRequest): ResponseEntity<Any> {
        logger.error("request=${request.getDescription(false)} --- Internal Server Error observed  error=${ex.message} ", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(object {val errors = listOf("Oh Sh*t someone made a mess here...we're working on that")})
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatch(
        ex: MethodArgumentTypeMismatchException,
        request: WebRequest
    ): ResponseEntity<Any> {
        logger.error(
            "request=${request.getDescription(false)} --- MethodArgumentTypeMismatchException observed  ${ex.message} - request: ${
                request.getDescription(
                    false
                )
            }", ex
        )
        val error =
            "Invalid argument type for parameter '${ex.name}'. Expected ${ex.requiredType?.simpleName}, but received ${ex.value}"
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(object {
            val errors: List<String> = listOf(error)
        })
    }

    @ExceptionHandler(UserNotFoundException::class)
    fun handleNotFoundException(exception: UserNotFoundException, webRequest: WebRequest): ResponseEntity<Any> {
        logger.error("request=${webRequest.getDescription(false)} 404 not found  error=${exception.message}", exception)
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(object {val errors = listOf("${exception.message}")})
    }

    @ExceptionHandler(JpaSystemException::class)
    fun handleJpaSystemException(exception: JpaSystemException, webRequest: WebRequest): ResponseEntity<Any> {
        logger.debug("the real cause: ${exception.mostSpecificCause}")
        logger.error("request=${webRequest.getDescription(false)} 422 failed validating data constraints for the input error=${exception.message}", exception)
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(object {val errors = listOf("${exception.message}")})
    }

}
