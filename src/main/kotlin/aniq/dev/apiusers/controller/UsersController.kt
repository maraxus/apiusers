package aniq.dev.apiusers.controller

import aniq.dev.apiusers.dto.UserDTO
import aniq.dev.apiusers.service.UserService
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

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

    @PutMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.OK)
    fun editUser(@Valid @RequestBody userInput: UserDTO,@PathVariable userId: Int): UserDTO {
        logger.info { "request received [PUT] /users/$userId" }
        return userService.editUser(userInput, userId)
    }

    @GetMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.OK)
    fun retrieveUser(@PathVariable userId: Int): UserDTO {
        logger.info { "request received [GET] /users/$userId" }
        return userService.retrieveUser(userId)
    }

    @GetMapping("/users")
    @ResponseStatus(HttpStatus.OK)
    fun retrieveAllUser(): List<UserDTO> {
        logger.info { "request received [GET] /users" }
        return userService.retrieveAllUser()
    }

    @DeleteMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun removeUser(@PathVariable userId: Int){
        logger.info { "request received [DELETE] /users/$userId" }
        return userService.removeUser(userId)
    }

}