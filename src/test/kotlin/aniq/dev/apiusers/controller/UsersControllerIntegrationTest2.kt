package aniq.dev.apiusers.controller

import aniq.dev.apiusers.dto.UserDTO
import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.jsonpath.JsonPath
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.jdbc.JdbcTestUtils
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.time.LocalDateTime

// test decisions in kotest: Integration Tests -> FeatureSpec | Unit Tests -> BehaviorSpec
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class UsersControllerIntegrationTest2: FeatureSpec() {

    @Autowired
    lateinit var mockMvc: MockMvc
    @Autowired
    lateinit var serializer: ObjectMapper
    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    override fun extensions() = listOf(SpringExtension)

    init {
        afterTest {
            JdbcTestUtils.deleteFromTables(jdbcTemplate, "user_stack")
            JdbcTestUtils.deleteFromTables(jdbcTemplate, "users")
        }

        feature("Users List") {
            scenario("Listing Users in ascending order by one attribute") {
                // setup users
                val testSequence = (20 downTo 1)
                val usersToCompare: MutableList<UserDTO> = generateAndSaveUsersForOrderTesting(testSequence = testSequence)

                // Try to get list sorted by nick
                val listResponse = mockMvc.get("/users?sort=nick")
                    .andExpect {
                    // Assert sucessful and valid Json return
                    content {
                        status { is2xxSuccessful() }
                        contentType(MediaType.APPLICATION_JSON)
                    }
                }.andReturn().response.contentAsString

                listResponse.should(haveRecordsOfUsers())

                val usersList = JsonPath.compile("$.records").read<List<UserDTO>>(listResponse)!!
                usersList.toString().shouldEqualJson(
                    serializer.writeValueAsString(
                        usersToCompare.sortedWith(compareBy(UserDTO::nick)).take(15)
                    )
                )
            }

            scenario("Listing Users in descending order by one attribute") {
                // setup users
                val testSequence = (20 downTo 1)
                val usersToCompare: MutableList<UserDTO> = generateAndSaveUsersForOrderTesting(testSequence = testSequence)

                // Try to get list sorted by nick
                val listResponse = mockMvc.get("/users?sort=-nick")
                    .andExpect {
                        // Assert sucessful and valid Json return
                        content {
                            status { is2xxSuccessful() }
                            contentType(MediaType.APPLICATION_JSON)
                        }
                    }.andReturn().response.contentAsString

                listResponse.should(haveRecordsOfUsers())

                val usersList = JsonPath.compile("$.records").read<List<UserDTO>>(listResponse)!!
                usersList.toString().shouldEqualJson(
                    serializer.writeValueAsString(
                        usersToCompare.sortedWith(compareBy(UserDTO::nick).reversed()).take(15)
                    )
                )
            }

        }
    }

    private fun generateAndSaveUsersForOrderTesting(testSequence: IntProgression): MutableList<UserDTO> {
        val shuffledTestSequence = testSequence.shuffled().iterator()
        val usersToCompare = mutableListOf<UserDTO>()
        testSequence.forEach {
            val newUser = UserDTO(
                id = null,
                name = "Jonh Doe $it",
                nick = "SomeJoe ${shuffledTestSequence.next()}",
                birthDate = LocalDateTime.parse("1987-07-02T15:00:45"),
                stack = mutableSetOf()
            )
            val savedUser = mockMvc.post("/users") {
                contentType = MediaType.APPLICATION_JSON
                content = serializer.writeValueAsString(
                    newUser
                )
            }.andReturn().response.contentAsString
            usersToCompare.add(newUser.copy(id = serializer.readTree(savedUser).get("id").intValue()))
        }
        return usersToCompare
    }
}

// Matchers
fun haveRecordsOfUsers() = Matcher<String> {
    MatcherResult(
        runCatching {
            JsonPath.compile("$.records").read<List<UserDTO>>(it)
        }.fold(onSuccess = { true }, onFailure = { false }),
        { "Does not have a valid user records on result" },
        { "Has user records" }
    )
}
