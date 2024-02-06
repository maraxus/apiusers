package aniq.dev.apiusers.entity

import jakarta.persistence.*
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDateTime

@Entity
@Table(name = "Users")
data class User (
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Int?,

    @Column(length = 32, nullable = true)
    var nick: String?,

    @Column(nullable = false, length = 255, unique = true)
    var name: String,

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Column(name = "birth_date")
    var birthDate: LocalDateTime,

    @ElementCollection
    @Column(name = "stack", length = 32)
    var stack: Set<String>? = mutableSetOf()
)
