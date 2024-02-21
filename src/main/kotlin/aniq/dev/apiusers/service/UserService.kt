package aniq.dev.apiusers.service

import aniq.dev.apiusers.dto.UserDTO
import aniq.dev.apiusers.entity.User
import aniq.dev.apiusers.exception.UserNotFoundException
import aniq.dev.apiusers.repository.UserRepositoryInterface
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.util.Optional

@Service
class UserService(val userRepository: UserRepositoryInterface) {

    fun addUser(user: UserDTO): UserDTO {
        val userToCreate = user.let {
            User(name = it.name, nick = it.nick, birthDate = it.birthDate, stack = it.stack ?: emptySet(), id = null)
        }
        return userRepository.saveAndFlush(userToCreate).asDto()
    }

    fun editUser(changedUser: UserDTO, userId: Int): UserDTO {
        val foundUser = userRepository.findById(userId)
            .takeIf { it.isPresent }?.get()
            ?: throw UserNotFoundException("User with userId=$userId wasn't found")

        foundUser.apply {
            name = changedUser.name
            nick = changedUser.nick
            birthDate = changedUser.birthDate
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

    fun retrieveAllUser(pageNumber: Int, pageSize: Int, sortQuery: Optional<List<Pair<String,String>>>): Page<User> {
        var sort = Sort.unsorted()
        sortQuery.ifPresent {
            val attributes = sortQuery.get().iterator()
            val sortingAttribute = attributes.next()
            sort = if (sortingAttribute.second == "DESCENDING") {
                Sort.by(sortingAttribute.first).descending()
            } else {
                Sort.by(sortingAttribute.first)
            }
            attributes.forEachRemaining {
                if (it.second == "ASCENDING") {
                    sort.and(Sort.by(sortingAttribute.first).descending())
                } else {
                    sort.and(Sort.by(sortingAttribute.first))
                }
            }
        }
        val pageable = PageRequest.of(pageNumber, pageSize, sort)
        return userRepository.findAll(pageable)
    }

    fun removeUser(userId: Int) {
        userRepository.findById(userId)
            .takeIf { it.isPresent }
            ?: throw UserNotFoundException("User with userId=$userId wasn't found")
        userRepository.deleteById(userId)
    }
}

private fun User.asDto(): UserDTO {
    return UserDTO(id, nick, name, birthDate, stack)
}
