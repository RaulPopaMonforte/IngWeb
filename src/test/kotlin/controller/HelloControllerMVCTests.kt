package es.unizar.webeng.hello.controller

import com.jayway.jsonpath.JsonPath
import java.time.ZoneId
import java.time.ZonedDateTime
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(
        HelloController::class,
        HelloApiController::class,
        TimeDependingController::class )
class HelloControllerMVCTests {
    @Value("\${app.message:Welcome to the Modern Web App!}")
    private lateinit var message: String

    @Autowired
    private lateinit var mockMvc: MockMvc
    
    @Test
    fun `should return home page with default message`() {
        mockMvc.perform(get("/"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(view().name("welcome"))
            .andExpect(model().attribute("message", equalTo(message)))
            .andExpect(model().attribute("name", equalTo("")))
    }
    
    /**
     * Se ha modificado el test para que tenga el cuenta el nuevo "greeting".
     */
    @Test
    fun `should return home page with personalized message`() {
        val timeDependingController = TimeDependingController()
        val resultado = timeDependingController.timeDependingGreeting()
        val greeting = resultado.first

        mockMvc.perform(get("/").param("name", "Developer"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(view().name("welcome"))
            .andExpect(model().attribute("message", equalTo("$greeting, Developer!")))
            .andExpect(model().attribute("name", equalTo("Developer")))
    }
    
    @Test
    fun `should return API response as JSON`() {
        mockMvc.perform(get("/api/hello").param("name", "Test"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message", equalTo("Hello, Test!")))
            .andExpect(jsonPath("$.timestamp").exists())
    }

    @Test
    fun `should return time-dependent greeting and Madrid timestamp as JSON`() {
        val result = mockMvc.perform(get("/api/helloTime").param("nombre", "Raul"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message", anyOf(
                equalTo("Buenos días, Raul"),
                equalTo("Buenas tardes, Raul")
            )))
            .andExpect(jsonPath("$.timestamp").isString)
            .andReturn()

        val timestamp = JsonPath.read<String>(result.response.contentAsString, "$.timestamp")
        assertThat(ZonedDateTime.parse(timestamp).zone).isEqualTo(ZoneId.of("Europe/Madrid"))
    }
}
