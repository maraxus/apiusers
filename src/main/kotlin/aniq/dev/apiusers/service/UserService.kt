package aniq.dev.apiusers.service

import aniq.dev.apiusers.dto.StackDTO
import aniq.dev.apiusers.dto.UserDTO
import aniq.dev.apiusers.entity.Stack
import aniq.dev.apiusers.entity.User
import aniq.dev.apiusers.exception.UserNotFoundException
import aniq.dev.apiusers.repository.StackRepository
import aniq.dev.apiusers.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.util.Optional
import java.util.UUID

@Service
class UserService(val userRepository: UserRepository, val stackRepository: StackRepository) {

    fun addUser(user: UserDTO): UserDTO {
        val userToCreate = user.let {
            User(name = it.name, nick = it.nick, birthDate = it.birthDate, stack = mutableSetOf(), id = null)
        }
        user.stack?.map {
            userToCreate.stack?.add(
                aniq.dev.apiusers.entity.Stack(
                    name = it.name,
                    level = it.level,
                    user = userToCreate,
                    id = null
                )
            )
        }

        return userRepository.saveAndFlush(userToCreate).asDto()
    }
    @Transactional
    fun editUser(changedUser: UserDTO, userId: UUID): UserDTO {
        val foundUser = userRepository.findById(userId)
            .orElseThrow{ UserNotFoundException("User with userId=$userId wasn't found") }

        foundUser.apply {
            name = changedUser.name
            nick = changedUser.nick
            birthDate = changedUser.birthDate

            changedUser.stack?.forEach { stackDTO ->
                foundUser.stack?.find { stack -> stack.name == stackDTO.name }.apply { this?.level = stackDTO.level }
                    ?: Stack(id = null, name = stackDTO.name, level = stackDTO.level, user = foundUser)
            }
        }
        return userRepository.save(foundUser).asDto()
    }
    @Transactional
    fun retrieveUser(userId: UUID): UserDTO {
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

    fun removeUser(userId: UUID) {
        userRepository.findById(userId)
            .takeIf { it.isPresent }
            ?: throw UserNotFoundException("User with userId=$userId wasn't found")
        userRepository.deleteById(userId)
    }

    fun retrieveUserStacks(userId: UUID): MutableSet<StackDTO> {
        val foundStacks = stackRepository.findAllByUserId(userId) ?: throw  UserNotFoundException("User with userId=$userId wasn't found")
        return foundStacks.map { it.toDto() }.toMutableSet()
    }
}

private fun Stack.toDto(): StackDTO {
    return StackDTO(name, level)
}

private fun User.asDto(): UserDTO {
    return UserDTO(id, nick, name, birthDate, stack?.map { StackDTO(it.name,it.level) }?.toSet())
}
