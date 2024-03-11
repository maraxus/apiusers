package aniq.dev.apiusers.validator

import aniq.dev.apiusers.dto.StackDTO
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class StackValidator: ConstraintValidator<ValidStackConstraint, MutableSet<StackDTO>> {
    override fun isValid(stack: MutableSet<StackDTO>?, context: ConstraintValidatorContext?): Boolean {
        if (stack == null) return true
        val validName: (String) -> (Boolean) = { name: String -> name.length <= 32 }
        val validLevel: (Int) -> (Boolean) = { level: Int -> level in (1..100) }
        return stack.all { validLevel(it.level) && validName(it.name) }
    }
}