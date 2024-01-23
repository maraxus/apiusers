package aniq.dev.apiusers.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Past
import jakarta.validation.constraints.Size
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDateTime

data class UserDTO(
    var id: Int?,
    @Size(max=32, message = "Size should not be higher than 32 characters")
    var nick: String?,
    @Size(max=255, message = "Size should not be higher than 255 characters")
    @NotBlank
    var name: String,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Past(message = "birth_date can't be a present, nor future date")
    var birth_date: LocalDateTime,
    var stack: Set<String>? = mutableSetOf()
)