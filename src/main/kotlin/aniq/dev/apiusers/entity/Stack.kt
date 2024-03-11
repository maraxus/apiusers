package aniq.dev.apiusers.entity

import jakarta.persistence.*
import java.util.Objects

@Entity
@Table(
    name = "user_stack",
    uniqueConstraints = [UniqueConstraint(name = "no_repeated_language_from_user", columnNames = ["name", "user_id"])]
)
data class Stack (
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Int?,
    @Column(nullable = false, length = 32)
    val name: String,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    @Column(nullable = false, name = "score")
    var level: Int
) {
    override fun toString(): String {
        return "[id=$id, name=$name, level=$level]"
    }

    override fun equals(other: Any?): Boolean {
        return other is Stack
                && id == other.id
                && name == other.name
                && level == other.level
                && user.id == other.user.id
    }

    override fun hashCode(): Int {
        return Objects.hash(id, name, level, user.id)
    }
}