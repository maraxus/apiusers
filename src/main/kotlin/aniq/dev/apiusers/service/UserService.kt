package aniq.dev.apiusers.service

import aniq.dev.apiusers.dto.UserDTO
import aniq.dev.apiusers.entity.User
import aniq.dev.apiusers.exception.UserNotFoundException
import aniq.dev.apiusers.repository.UserRepositoryInterface
import org.springframework.stereotype.Service

@Service
class UserService(val userRepository: UserRepositoryInterface) {

    fun addUser(user: UserDTO): UserDTO {
        val userToCreate = user.let {
            User(name = it.name, nick = it.nick, birth_date = it.birth_date, stack = it.stack ?: emptySet(), id = null)
        }
        return userRepository.save(userToCreate).asDto()
    }

    fun editUser(changedUser: UserDTO, userId: Int): UserDTO {
        val foundUser = userRepository.findById(userId)
            .takeIf { it.isPresent }?.get()
            ?: throw UserNotFoundException("User with userId=$userId wasn't found")

        foundUser.apply {
            name = changedUser.name
            nick = changedUser.nick
            birth_date = changedUser.birth_date
            stack = changedUser.stack ?: mutableSetOf()
        }
        return userRepository.save(foundUser).asDto()
    }

    fun retrieveUser(userId: Int): UserDTO {
        val foundUser = userRepository.findById(userId)
            .takeIf { it.isPresent }?.get()
            ?: throw UserNotFoundException("User with userId=$userId wasn't found")
        return foundUser.asDto()
    }

    fun retrieveAllUser(): List<UserDTO> = userRepository.findAll().map { user -> user.asDto() }

    fun removeUser(userId: Int) {
        userRepository.findById(userId)
            .takeIf { it.isPresent }
            ?: throw UserNotFoundException("User with userId=$userId wasn't found")
        userRepository.deleteById(userId)
    }
}

private fun User.asDto(): UserDTO {
    return UserDTO(id, nick, name, birth_date, stack)
}
