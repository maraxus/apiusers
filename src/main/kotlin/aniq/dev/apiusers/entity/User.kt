package aniq.dev.apiusers.entity

import jakarta.persistence.*
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "Users")
data class User (
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID?,

    @Column(length = 32, nullable = true)
    var nick: String?,

    @Column(nullable = false, length = 255, unique = true)
    var name: String,

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Column(name = "birth_date", nullable = false)
    var birthDate: LocalDateTime,

    @OneToMany(
        mappedBy = "user",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
    )
    var stack: MutableSet<Stack>? = mutableSetOf()
)
