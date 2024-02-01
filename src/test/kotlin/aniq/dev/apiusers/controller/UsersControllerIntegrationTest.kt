package aniq.dev.apiusers.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class UsersControllerIntegrationTest (){

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var serializer: ObjectMapper

    companion object {
        var validIdtoOperate = 0
        val validName = "Jonh Doe"
        val validNick = "Blue pen"
        val validbirthDate = "2087-07-02T15:00:45"
        val validStack = listOf("javascript", "Go")
    }

    @Test
    @Order(1)
    fun postShouldSaveAValidUserAndReturnIt() {

        val result  = mockMvc.post("/users") {
            contentType = MediaType.APPLICATION_JSON
            content = serializer.writeValueAsString(mockUserInput())
        }
            .andExpect { status { isCreated() } }
            .andExpect { content { contentType(MediaType.APPLICATION_JSON) } }
            .andExpect { jsonPath("$.id") { isNumber() } }.andReturn().response.contentAsString

        validIdtoOperate = serializer.readTree(result)["id"].intValue()
    }

    private fun mockUserInput(type: String? = null): Any {
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

            //valid
            else -> return object {
                val name = validName
                val nick = validNick
                val birth_date = validbirthDate
                val stack = validStack
            }

        }
    }

//    @Test
//    fun postShouldFailWithSameInputNamesCantBeRepeated() {
//        mockMvc.post("/users") {
//            contentType = MediaType.APPLICATION_JSON
//            content = serializer.writeValueAsString(mockValidUserInput())
//        }
//    }

    @Test
    @Order(2)
    fun postShouldFailWithInsufficientInput() {
        mockMvc.post("/users") {
            contentType = MediaType.APPLICATION_JSON
            content = serializer.writeValueAsString(mockUserInput("noName"))
        }.andExpect { status { is4xxClientError() } }
    }

    @Test
    @Order(3)
    fun postShouldFailWithWrongDateType() {
        mockMvc.post("/users") {
            contentType = MediaType.APPLICATION_JSON
            content = serializer.writeValueAsString(mockUserInput("wrongDate"))
        }.andExpect { status { is4xxClientError() } }
    }

    @Test
    @Order(4)
    fun postShouldFailWithWrongStackFormat() {
        mockMvc.post("/users") {
            contentType = MediaType.APPLICATION_JSON
            content = serializer.writeValueAsString(mockUserInput("wrongStack"))
        }.andExpect { status { is4xxClientError() } }
    }

    @Test
    @Order(5)
    fun getShouldRetrieveUserPassingValidId() {
        val url = "/users/$validIdtoOperate"

        val result = mockMvc.get(url)
            .andExpect { status { isOk() } }
            .andExpect { jsonPath("$.id"){ value(validIdtoOperate) } }
            .andExpect { jsonPath("$.name"){ value(validName) } }
            .andExpect { jsonPath("$.nick"){ value(validNick) } }
            .andExpect { jsonPath("$.birth_date"){ value(validbirthDate) } }
            .andExpect { jsonPath("$.stack"){ isArray() } }
//            .andExpect { jsonPath("$.stack[0]"){ value(validStack[0]) } }
//            .andExpect { jsonPath("$.stack[1]"){ value(validStack[1]) } }
    }

    @Test
    @Order(6)
    fun getShouldFailWithUnknownId() {
        val url = "/users/2"
        mockMvc.get(url)
            .andExpect { status { isNotFound() } }
    }

    @Test
    @Order(7)
    fun updateShouldSaveChangesWithValidFormat() {
        val url = "/users/$validIdtoOperate"
        mockMvc.put(url) {
            contentType = MediaType.APPLICATION_JSON
            content = serializer.writeValueAsString(mockUserInput("modified"))
        }
            .andExpect { status { isOk() } }
            .andExpect { jsonPath("$.id"){ value(validIdtoOperate) } }
            .andExpect { jsonPath("$.name"){ value("$validName Bar") } }
            .andExpect { jsonPath("$.nick"){ value(validNick) } }
            .andExpect { jsonPath("$.birth_date"){ value(validbirthDate) } }
            .andExpect { jsonPath("$.stack"){ isArray() } }
    }


}