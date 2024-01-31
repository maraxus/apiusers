package aniq.dev.apiusers.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class UsersControllerIntegrationTest (){

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var serializer: ObjectMapper

    @Test
    fun postShouldSaveAValidUserAndReturnIt() {

        mockMvc.post("/users") {
            contentType = MediaType.APPLICATION_JSON
            content = serializer.writeValueAsString(mockUserInput())
        }
            .andExpect { status { isCreated() } }
            .andExpect { content { contentType(MediaType.APPLICATION_JSON) } }
            .andExpect { jsonPath("$.id") { isNumber() } }
    }

    private fun mockUserInput(type: String? = null): Any {
        when(type) {
            "wrongStack" -> return object {
                val name = "Jonh Doe"
                val nick = "Blue pen"
                val birth_date = "2087-07-02T15:00:45"
                val stack = 324
            }

            "wrongDate" -> return object {
                val name = "Jonh Doe"
                val nick = "Blue pen"
                val birth_date = "asdfa"
                val stack = listOf("javascript", "Go")
            }

            "noName" -> return object {
                val nick = "Blue pen"
                val birth_date = "2087-07-02T15:00:45"
                val stack = listOf("javascript", "Go")
            }

            //valid
            else -> return object {
                val name = "Jonh Doe"
                val nick = "Blue pen"
                val birth_date = "2087-07-02T15:00:45"
                val stack = listOf("javascript", "Go")
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
    fun postShouldFailWithWrongStackFormat() {
        mockMvc.post("/users") {
            contentType = MediaType.APPLICATION_JSON
            content = serializer.writeValueAsString(mockUserInput("wrongStack"))
        }.andExpect { status { is4xxClientError() } }
    }
}