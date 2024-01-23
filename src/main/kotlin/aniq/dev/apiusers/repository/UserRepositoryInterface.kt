package aniq.dev.apiusers.repository

import aniq.dev.apiusers.entity.User
import org.springframework.data.repository.CrudRepository

interface UserRepositoryInterface: CrudRepository<User, Int> {

}