package aniq.dev.apiusers.validator

import aniq.dev.apiusers.dto.UserDTO
import jakarta.validation.Constraint
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [StackValidator::class])
@MustBeDocumented
annotation class ValidStackConstraint (
    val message: String = "Invalid format for stack list",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<UserDTO>> = []
)