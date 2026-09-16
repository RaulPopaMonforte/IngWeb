package es.unizar.webeng.hello.controller

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

//Imports usados para el cálculo identificar si es por la mañana o
//por la tarde
import java.time.Instant
import java.time.ZoneId
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

@Controller
class HelloController(
    @param:Value("\${app.message:Hello World}") 
    private val message: String
) {
    
    @GetMapping("/")
    fun welcome(
        model: Model,
        @RequestParam(defaultValue = "") name: String
    ): String {
        val greeting = if (name.isNotBlank()) "Hello, $name!" else message
        model.addAttribute("message", greeting)
        model.addAttribute("name", name)
        return "welcome"
    }
}

@RestController
class HelloApiController {
    
    @GetMapping("/api/hello", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun helloApi(@RequestParam(defaultValue = "World") name: String): Map<String, String> {
        return mapOf(
            "message" to "Hello, $name!",
            "timestamp" to java.time.Instant.now().toString()
        )
    }
}

@RestController
class TimeDependingController {

    
    fun timeDependingGreeting(): Pair<String, ZonedDateTime> 
    {
        var greeting : String
        val tiempoActual : Instant = java.time.Instant.now()
        val IdZona = ZoneId.of("Europe/Madrid")
        val medioDia = LocalDate.now(IdZona).atTime(LocalTime.NOON).atZone(IdZona)
                                .toInstant()

        val tiempoDevolver = tiempoActual.atZone(IdZona)
        if (tiempoActual.isAfter(medioDia)) {
            return Pair("Buenas tardes", tiempoDevolver)
        } else {
            return Pair("Buenos días", tiempoDevolver)
        }
    }

    @GetMapping("/api/helloTime", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun greetingTime(@RequestParam(defaultValue = "Desconocido") nombre : String)
        : Map<String,String>  
    {
            val resultado = timeDependingGreeting()
            val saludo = resultado.first
            val tiempo = resultado.second.toString()

            return mapOf(
                "message" to "$saludo, $nombre",
                "timestamp" to "$tiempo"
            )    
    }
}