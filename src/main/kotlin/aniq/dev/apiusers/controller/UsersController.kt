package aniq.dev.apiusers.controller

import aniq.dev.apiusers.controller.response.PagedResults
import aniq.dev.apiusers.dto.StackDTO
import aniq.dev.apiusers.dto.UserDTO
import aniq.dev.apiusers.entity.User
import aniq.dev.apiusers.service.UserService
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.transaction.Transactional
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.*
import kotlin.jvm.optionals.getOrElse
import kotlin.jvm.optionals.getOrNull

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
    @Transactional
    fun editUser(@Valid @RequestBody userInput: UserDTO,@PathVariable userId: UUID): UserDTO {
        logger.info { "request received [PUT] /users/$userId" }
        return userService.editUser(userInput, UUID.fromString(userId.toString()))
    }

    @GetMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    fun retrieveUser(@PathVariable userId: UUID): UserDTO {
        logger.info { "request received [GET] /users/$userId" }
        return userService.retrieveUser(UUID.fromString(userId.toString()))
    }

    @GetMapping("/{userId}/stacks")
    @ResponseStatus(HttpStatus.OK)
    fun retrieveUserStacks(@PathVariable userId: UUID): MutableSet<StackDTO> {
        logger.info { "request received [GET] /users/$userId/stacks" }
        return userService.retrieveUserStacks(userId)
    }

    @GetMapping
    fun retrieveAllUser(
        @RequestParam(required = false,name = "page_size") pageSize: Optional<Int>,
        @RequestParam(required = false, name = "page") page: Optional<Int>,
        @RequestParam(required = false, name = "sort") sort: Optional<String>,
    ): ResponseEntity<PagedResults<UserDTO>> {
        val results = userService.retrieveAllUser(
            pageNumber = page.getOrElse { 0 },
            pageSize = pageSize.getOrElse { PAGE_SIZE_DEFAULT },
            sortQuery = buildSortQuery(sort)
        )
        val status = if (results.isLast or results.isEmpty) HttpStatus.OK else HttpStatus.PARTIAL_CONTENT
        val responseBody = PagedResults(
            results.content.map { it.asDto() },
            results.number,
            results.size,
            results.totalElements)
        logger.info { "request received [GET] /users" }
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(responseBody)
    }

    private fun buildSortQuery(sortParam: Optional<String>): Optional<List<Pair<String, String>>> {
        val orderDirection: (String)->(Pair<String,String>) =
            orderDirection@{ queryAttribute:String ->
                val firstChar = queryAttribute.first().takeIf { it == '-' || it == '+' }
                return@orderDirection when (firstChar) {
                    '-' -> Pair(queryAttribute.drop(1), "DESCENDING")
                    '+' -> Pair(queryAttribute.drop(1), "ASCENDING")
                    else -> Pair(queryAttribute, "ASCENDING")
                }
        }
        return Optional.ofNullable<List<Pair<String, String>>>(sortParam.getOrNull()?.split(',')?.toList()?.map(orderDirection))
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun removeUser(@PathVariable userId: UUID){
        logger.info { "request received [DELETE] /users/$userId" }
        return userService.removeUser(UUID.fromString(userId.toString()))
    }

}

private fun User.asDto(): UserDTO {
    return UserDTO(id, nick, name, birthDate, stack?.map { StackDTO(it.name,it.level) }?.toSet())
}
