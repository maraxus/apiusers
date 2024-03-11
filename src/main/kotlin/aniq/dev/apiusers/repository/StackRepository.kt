package aniq.dev.apiusers.repository

import aniq.dev.apiusers.entity.Stack
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface StackRepository: JpaRepository<Stack, Int> {
    @Query("SELECT s FROM Stack s WHERE s.user.id = :userId")
    fun findAllByUserId(userId: UUID): MutableSet<Stack>?
}
