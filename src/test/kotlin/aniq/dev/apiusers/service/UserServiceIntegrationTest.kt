package aniq.dev.apiusers.service

import aniq.dev.apiusers.dto.UserDTO
import aniq.dev.apiusers.repository.UserRepositoryInterface
import jakarta.annotation.PostConstruct
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace
import java.time.LocalDateTime

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class UserServiceIntegrationTest {

    @Autowired
    lateinit var repository: UserRepositoryInterface

    lateinit var service: UserService

    @PostConstruct
    fun initializeService() {
        service = UserService(repository)
    }

    @Test
    fun addValidUser() {
        val user = UserDTOFactory.validNullId()
        val savedUser = service.addUser(user)
        assertTrue(savedUser.id is Int)
        assertTrue(user.copy(id = savedUser.id) == savedUser)
    }

    @Test
    fun saveInvalidUserTooLong() {
        val user  = UserDTOFactory.valueTooLong()
        assertThrows<Throwable> {
            service.addUser(user)
        }
//        val saved = service.addUser(user)
//        assertTrue(saved.id is Int)

    }

    @Test
    fun saveInvalidUserBlank() {
        val user = UserDTOFactory.nameNull()
        assertThrows<Throwable> {
            service.addUser(user)
        }
    }

    @Test
    fun saveInvalidUserNotUnique() {}

    @Test
    fun saveInvalidUserStackMemberTooLong() {}

    @Test
    fun saveInvalidUserStackEmptyButNotNull() {}

    @Test
    fun retrieveValidUser() {}

    @Test
    fun retrieveAllUser() {}

    @Test
    fun removeValidUser() {}

    @Test
    fun removeInvalidUserFails() {}

}

object UserDTOFactory {
    val exampleUser = UserDTO(
        null,
        "Blue pen",
        "Jonh Doe",
        LocalDateTime.parse("2087-07-02T15:00:45"),
        mutableSetOf("javascript", "Go")
    )

    fun validNullId(): UserDTO {
        return exampleUser
    }

    fun valueTooLong(): UserDTO {
        return exampleUser.copy(name = "Nome Longo".padEnd(280,'o'))
    }

    fun nameNull(): UserDTO {
        return exampleUser.copy(name = "")
    }
}
