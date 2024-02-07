package aniq.dev.apiusers.service

import aniq.dev.apiusers.dto.UserDTO
import aniq.dev.apiusers.exception.UserNotFoundException
import aniq.dev.apiusers.repository.UserRepositoryInterface
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.orm.jpa.JpaSystemException
import java.time.LocalDateTime

@SpringBootTest
class UserServiceIntegrationTest {

    @Autowired
    lateinit var service: UserService

    @Autowired
    lateinit var userRepository: UserRepositoryInterface

    @AfterEach
    fun cleanUp() {
        userRepository.deleteAll()
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
        assertThrows<JpaSystemException> {
            service.addUser(user)
        }

    }

    @Test
    fun saveInvalidUserBlank() {
        val user = UserDTOFactory.nameNull()
        assertThrows<Throwable> {
            service.addUser(user)
        }
    }

    @Test
    fun addRepeatedUserName() {
        addValidUserAndReturnIt()
        val anotherUser = UserDTOFactory.validNullId()
        assertThrows<DataIntegrityViolationException> {
            service.addUser(anotherUser)
        }
    }

    private fun addValidUserAndReturnIt(): UserDTO {
        val user = UserDTOFactory.validNullId()
        return service.addUser(user)
    }

    @Test
    fun saveInvalidUserStackMemberTooLong() {
        val user = UserDTOFactory.stackWithTooLongMemberValue()
        assertThrows<JpaSystemException> {
            service.addUser(user)
        }

    }

    @Test
    fun retrieveValidUser() {
        val savedUser = addValidUserAndReturnIt()
        val retrievedUser: UserDTO = service.retrieveUser(savedUser.id!!)
        assertTrue(savedUser == retrievedUser)
    }

    @Test
    fun retrieveAllUser() {
        val validUsersToSave  = UserDTOFactory.validUsersToSave((2..10).random())
        val users = validUsersToSave.map { service.addUser(it) }
        assertTrue(users.size == validUsersToSave.size)
        assertTrue(
            users.map { it.apply { id = null } }
            .containsAll(validUsersToSave)
        )
    }

    @Test
    fun removeValidUser() {
        val userToDelete = addValidUserAndReturnIt().id
        service.removeUser(userToDelete!!)
        assertThrows<UserNotFoundException> {
            service.retrieveUser(userToDelete)
        }
    }

    @Test
    fun removeInvalidUserFails() {
        assertThrows<UserNotFoundException> {
            service.removeUser(-1)
        }
    }
}

object UserDTOFactory {
    private val exampleUser = UserDTO(
        null,
        "Blue pen",
        "John Doe",
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

    fun stackWithTooLongMemberValue(): UserDTO {
        return exampleUser.copy(stack = mutableSetOf("Javascript", "Go", "Blastoise".padEnd(44, 'e')))
    }

    fun validUsersToSave(lenght: Int): List<UserDTO> {
        return mutableListOf<UserDTO>().apply {
            (1..lenght).forEach {
                this.add(exampleUser.copy(name = "${exampleUser.name} $it"))
            }
        }
    }

}