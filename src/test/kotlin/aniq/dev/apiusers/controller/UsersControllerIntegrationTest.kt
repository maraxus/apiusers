package aniq.dev.apiusers.controller

import aniq.dev.apiusers.dto.StackDTO
import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.jsonpath.JsonPath
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.junit.jupiter.api.AfterEach
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.jdbc.JdbcTestUtils
import org.springframework.test.web.servlet.*
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class UsersControllerIntegrationTest (){

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var serializer: ObjectMapper

    @AfterEach
    fun setDown(@Autowired jdbcTemplate: JdbcTemplate) {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "user_stack")
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "users")
    }

    companion object {
        val validName = "Jonh Doe"
        val validNick = "Blue pen"
        val validbirthDate = "1987-07-02T15:00:45"
        val validStack = mutableListOf(StackDTO("javascript",70) , StackDTO("Go", 30))
    }

    @Test
    fun postShouldSaveAValidUserAndReturnIt() {
        mockMvc.post("/users") {
            contentType = MediaType.APPLICATION_JSON
            content = serializer.writeValueAsString(mockUserInput())
        }
            .andExpect { status { isCreated() } }
            .andExpect { content { contentType(MediaType.APPLICATION_JSON) } }
            .andExpect { jsonPath("$.id") { isString() } }

    }

    protected fun mockUserInput(type: Any? = null): Any {
        when(type) {
            "modified" -> return object {
                val name = "$validName Bar"
                val nick = validNick
                val birth_date = validbirthDate
                val stack = validStack
            }
            "wrongStack" -> return object {
                val name = validName
                val nick = validNick
                val birth_date = validbirthDate
                val stack = 324
            }

            "wrongDate" -> return object {
                val name = validName
                val nick = validNick
                val stack = validStack
                val birth_date = "asdfa"
            }

            "noName" -> return object {
                val nick = validNick
                val birth_date = validbirthDate
                val stack = validStack
            }

            "tooLongNick" -> return object {
                val name = validName
                val nick = validNick.padEnd(33, 'X')
                val birth_date = validbirthDate
                val stack = validStack
            }

            "futureBirthDate" -> return object {
                val name = validName
                val nick = validNick
                val birth_date = validbirthDate.replaceFirst("1987", "2087")
                val stack = validStack
            }

            is Int -> return object {
                val name = "$validName $type"
                val nick = validNick
                val birth_date = validbirthDate
                val stack = validStack
            }

            //valid
            else -> return object {
                val name = validName
                val nick = validNick
                val birth_date = validbirthDate
                val stack = validStack
            }

        }
    }

    @Test
    fun postShouldFailWithTooLongInput() {
        mockMvc.post("/users") {
            contentType = MediaType.APPLICATION_JSON
            content = serializer.writeValueAsString(mockUserInput("tooLongNick"))
        }.andExpect { status { is4xxClientError() } }
    }

    @Test
    fun postShouldFailWithInsufficientInput() {
        mockMvc.post("/users") {
            contentType = MediaType.APPLICATION_JSON
            content = serializer.writeValueAsString(mockUserInput("noName"))
        }.andExpect { status { is4xxClientError() } }
    }

    @Test
    fun postShouldFailWithWrongDateType() {
        mockMvc.post("/users") {
            contentType = MediaType.APPLICATION_JSON
            content = serializer.writeValueAsString(mockUserInput("wrongDate"))
        }.andExpect { status { is4xxClientError() } }
    }

    @Test
    fun postShouldFailWithFutureDateType() {
        mockMvc.post("/users") {
            contentType = MediaType.APPLICATION_JSON
            content = serializer.writeValueAsString(mockUserInput("futureBirthDate"))
        }.andExpect { status { is4xxClientError() } }
    }

    @Test
    fun postShouldFailWithWrongStackFormat() {
        mockMvc.post("/users") {
            contentType = MediaType.APPLICATION_JSON
            content = serializer.writeValueAsString(mockUserInput("wrongStack"))
        }.andExpect { status { is4xxClientError() } }
    }

    @Test
    fun getShouldRetrieveUserPassingValidId() {
        val validIdtoOperate = saveValidUserRetrunId()
        val url = "/users/$validIdtoOperate"

        mockMvc.get(url)
            .andExpect { status { isOk() } }
            .andExpect { jsonPath("$.id"){ value(validIdtoOperate.toString()) } }
            .andExpect { jsonPath("$.name"){ value(validName) } }
            .andExpect { jsonPath("$.nick"){ value(validNick) } }
            .andExpect { jsonPath("$.birth_date"){ value(validbirthDate) } }
            .andExpect { jsonPath("$.stack"){ isArray() } }
//            .andExpect { jsonPath("$.stack[0]"){ value(validStack[0]) } }
//            .andExpect { jsonPath("$.stack[1]"){ value(validStack[1]) } }
    }

    @Test
    fun getStacksShouldRetrieveStackListPassingValidUserId() {
        val validIdtoOperate = saveValidUserRetrunId()
        val url = "/users/$validIdtoOperate/stacks"

        mockMvc.get(url)
            .andExpect { status { isOk() } }
            .andExpect { jsonPath("$"){ isArray() } }
//            .andExpect { jsonPath("$.stack[0]"){ value(validStack[0]) } }
//            .andExpect { jsonPath("$.stack[1]"){ value(validStack[1]) } }
    }

    @Test
    fun getShouldFailWithInvalidId() {
        val url = "/users/2"
        mockMvc.get(url)
            .andExpect { status { is4xxClientError() } }
    }

    @Test
    fun getShouldFailWithInexistentId() {
        val url = "/users/${UUID.randomUUID()}"
        mockMvc.get(url)
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun updateShouldSaveChangesWithValidFormat() {

        val validIdToOperate = saveValidUserRetrunId()

        val url = "/users/$validIdToOperate"
        mockMvc.put(url) {
            contentType = MediaType.APPLICATION_JSON
            content = serializer.writeValueAsString(mockUserInput("modified"))
        }
            .andExpect { status { isOk() } }
            .andExpect { jsonPath("$.id"){ value(validIdToOperate.toString()) } }
            .andExpect { jsonPath("$.name"){ value("$validName Bar") } }
            .andExpect { jsonPath("$.nick"){ value(validNick) } }
            .andExpect { jsonPath("$.birth_date"){ value(validbirthDate) } }
            .andExpect { jsonPath("$.stack"){ isArray() } }
    }

    private fun saveValidUserRetrunId(): UUID {
        return mockMvc.post("/users") {
            contentType = MediaType.APPLICATION_JSON
            content = serializer.writeValueAsString(mockUserInput())
        }
            .andReturn()
            .response.contentAsString
            .run {
                UUID.fromString(JsonPath.parse(this).read("$.id"))
            }
    }

    @Test
    fun updateShouldFailWithInvalidFormat() {
        val validIdToOperate = saveValidUserRetrunId()
        val url = "/users/$validIdToOperate"
        mockMvc.put(url) {
            contentType = MediaType.APPLICATION_JSON
            content = serializer.writeValueAsString(mockUserInput("wrongDate"))
        }
            .andExpect { status { is4xxClientError() } }
    }

    @Test
    fun deleteShouldBeSuccessfulWithSavedId() {
        val validIdToOperate = saveValidUserRetrunId()
        val url = "/users/$validIdToOperate"
        mockMvc.delete(url)
            .andExpect { status { isNoContent() } }
        mockMvc.get(url).andExpect { status { isNotFound() } }
    }

    @Test
     fun deleteShouldFailWithInvalidId() {
        val url = "/users/-1"
        mockMvc.delete(url)
            .andExpect {
                status { is4xxClientError() }
            }
    }

    @Test
     fun deleteShouldFailWithUnknownId() {
        val url = "/users/${UUID.randomUUID()}"
        mockMvc.delete(url)
            .andExpect {
                status { is4xxClientError() }
            }
    }

    @Test
    fun getAllShouldReturnListOfUsersPagedOnePage() {
        val defaultPageSize = 15
        val randomNumberOfUsers = (1..defaultPageSize).random()
        populateUsers(randomNumberOfUsers)
        mockMvc.get("/users")
            .andExpect {
                status { isOk() }
            }
            .andExpect {
                content {
                    jsonPath("$.records") { isArray() }
                    jsonPath( "$.records", hasSize<Array<Any>>(randomNumberOfUsers))
                }
            }
    }
    @Test
    fun getAllShouldReturnListOfUsersPagedVariousPages() {
        val defaultPageSize = 15
        val randomNumberOfUsers = (defaultPageSize..35).random()
        populateUsers(randomNumberOfUsers)
        mockMvc.get("/users")
            .andExpect {
                status { isPartialContent() }
            }
            .andExpect {
                content {
                    jsonPath( "$.records", hasSize<Array<Any>>(defaultPageSize))
                }
            }
    }
    @Test
    fun getAllShouldReturnListOfUsersPagedShouldFetchNextPage() {
        val defaultPageSize = 15
        val randomNumberOfUsers = (defaultPageSize +1..25).random()
        populateUsers(randomNumberOfUsers)
        mockMvc.get("/users?page=1")
            .andExpect {
                status { isOk() }
            }
            .andExpect {
                content {
                    jsonPath( "$.records", hasSize<Array<Any>>(randomNumberOfUsers - defaultPageSize))
                }
            }
    }

    private fun populateUsers(numberOfUsers: Int) {
        (1..numberOfUsers).iterator().forEach {
            mockMvc.post("/users"){
                contentType = MediaType.APPLICATION_JSON
                content = serializer.writeValueAsString(mockUserInput(it))
            }
        }
    }

}