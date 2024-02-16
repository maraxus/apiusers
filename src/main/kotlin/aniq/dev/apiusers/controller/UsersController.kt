package aniq.dev.apiusers.controller

import aniq.dev.apiusers.dto.UserDTO
import aniq.dev.apiusers.entity.User
import aniq.dev.apiusers.service.UserService
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.*
import kotlin.jvm.optionals.getOrElse


const val PAGE_SIZE_DEFAULT = 15

private val logger = KotlinLogging.logger {}

@Validated
@RestController
@RequestMapping("/users")
class UsersController(val userService: UserService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun addUser(@Valid @RequestBody userInput: UserDTO): UserDTO {
        logger.info { "request received [POST] /users" }
        return userService.addUser(userInput)
    }

    @PutMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    fun editUser(@Valid @RequestBody userInput: UserDTO,@PathVariable userId: Int): UserDTO {
        logger.info { "request received [PUT] /users/$userId" }
        return userService.editUser(userInput, userId)
    }

    @GetMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    fun retrieveUser(@PathVariable userId: Int): UserDTO {
        logger.info { "request received [GET] /users/$userId" }
        return userService.retrieveUser(userId)
    }

    @GetMapping
    fun retrieveAllUser(
        @PathVariable(required = false,name = "page_size") pageSize: Optional<Int>,
        @PathVariable(required = false, name = "page") page: Optional<Int>,
        @PathVariable(required = false, name = "sort") sort: Optional<String>,
    ): ResponseEntity<PagedResults<UserDTO>> {

        val results = userService.retrieveAllUser(page.getOrElse { 0 }, pageSize.getOrElse{ 15 })
        val status = if (results.isLast or results.isEmpty) HttpStatus.OK else HttpStatus.PARTIAL_CONTENT
        val responseBody = PagedResults<UserDTO>(
            results.content.map { it.asDto() },
            results.number,
            results.size,
            results.totalElements)
        logger.info { "request received [GET] /users" }
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(responseBody)
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun removeUser(@PathVariable userId: Int){
        logger.info { "request received [DELETE] /users/$userId" }
        return userService.removeUser(userId)
    }

}

class PagedResults<T>(
    val records: List<T>,
    val page: Int,
    val pageSize: Int,
    val total: Long
)

private fun User.asDto(): UserDTO {
    return UserDTO(id, nick, name, birthDate, stack)
}
