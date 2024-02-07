package aniq.dev.apiusers.repository

import aniq.dev.apiusers.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepositoryInterface: JpaRepository<User, Int> 