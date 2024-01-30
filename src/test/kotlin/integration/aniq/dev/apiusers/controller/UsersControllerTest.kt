package integration.aniq.dev.apiusers.controller

import aniq.dev.apiusers.controller.UsersController
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class UsersControllerTest (@Autowired mockMvc: MockMvc){

    lateinit var mockMvc: MockMvc

    @Test
    fun testThis() {
        this.mockMvc.get("/users").andExpect {
            status { isOk() }
        }
    }
}