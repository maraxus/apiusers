package aniq.dev.apiusers.controller

import aniq.dev.apiusers.dto.UserDTO
import aniq.dev.apiusers.service.UserService
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.Valid
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties.Pageable
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.web.PageableDefault
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.data.web.SortHandlerMethodArgumentResolver
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer
import org.springframework.data.web.config.SortHandlerMethodArgumentResolverCustomizer
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.*


const val PAGE_SIZE_DEFAULT = 15

private val logger = KotlinLogging.logger {}

@Configuration
class PageableCustomizer {
    @Bean
    fun pageableCustomizer(): PageableHandlerMethodArgumentResolverCustomizer {
        return PageableHandlerMethodArgumentResolverCustomizer { p: PageableHandlerMethodArgumentResolver ->
            p.setSizeParameterName("page_size")

        }
    }

}

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
        @PathVariable(required = false,name = "page_size") pageSize: Optional<String>,
        @PathVariable(required = false, name = "page") page: Optional<String>,
        @PathVariable(required = false, name = "sort") sort: Optional<String>,
        @PageableDefault(size = PAGE_SIZE_DEFAULT) pageable: Pageable
    ): ResponseEntity<UserCollectionResponse> {

        val results = UserCollectionResponse(userService.retrieveAllUser())

        logger.info { "request received [GET] /users" }
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(results)
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun removeUser(@PathVariable userId: Int){
        logger.info { "request received [DELETE] /users/$userId" }
        return userService.removeUser(userId)
    }

}