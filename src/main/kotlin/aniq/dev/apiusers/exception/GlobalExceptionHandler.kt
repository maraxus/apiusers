package aniq.dev.apiusers.exception

import com.fasterxml.jackson.module.kotlin.jsonMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
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
        val responseBody = jsonMapper().writeValueAsString(object {val errors = errors})

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(responseBody)
    }

    @ExceptionHandler(Exception::class)
    fun handleAllExceptions(ex: Exception, request: WebRequest): ResponseEntity<Any> {
        logger.error("request=${request.getDescription(false)} --- Internal Server Error observed  error=${ex.message} ", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(object {val errors = listOf("Oh Sh*t someone made a mess here...we're working on that")})
    }

    @ExceptionHandler(UserNotFoundException::class)
    fun handleNotFoundException(exception: UserNotFoundException, webRequest: WebRequest): ResponseEntity<Any> {
        logger.error("request=${webRequest.getDescription(false)} 404 not found  error=${exception.message}", exception)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(object {val errors = listOf("${exception.message}")})
    }
}
