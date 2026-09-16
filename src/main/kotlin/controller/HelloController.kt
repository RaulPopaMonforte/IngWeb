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
import java.time.format.DateTimeFormatter
import java.util.Locale

@Controller
class HelloController(
    @param:Value("\${app.message:Hello World}") 
    private val message: String,
    private val timeDependingController: TimeDependingController
) {
    
    /**
     * Se ha modificado la función con respecto a la proporcionada en el guión 
     * de tal manera que:
     *      1. Se de una bienvenida personalizada en función del momento del día
     *      2. Además de la bienvenida se indique el momento exacto de la última 
     *         petición al servidor
     */
    @GetMapping("/")
    fun welcome(
        model: Model,
        @RequestParam(defaultValue = "") name: String
    ): String {
        val formatoFecha = DateTimeFormatter.ofPattern(
            "d 'de' MMMM 'de' yyyy, HH:mm",
            Locale("es", "ES")
        )
        
        val respuesta = timeDependingController.timeDependingGreeting()
        val greeting = if (name.isNotBlank()) "${respuesta.first} $name!" else message

        model.addAttribute("message", greeting)
        model.addAttribute("name", name)
        model.addAttribute("timestamp", respuesta.second.format(formatoFecha).
                toString())
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

    /**
     * Crea un recibimiento para el usuario en función de la hora a la que 
     * este se encuente. Antes del medio día recibimiento = "Buenos días", 
     * después del medio día recibimiento = "Buenas tardes".
     * 
     * @return Devuelve la tupla con el recibimiento correspondiente y el 
     *         timestamp en el que se realizó la petición.
     */
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

    /**
     * Expone el endpoint dedicado a la obtención en formato JSON de un 
     * recibimiento personalizado basado en el momento del día.
     * 
     * @param nombre Parametro pasado en la petición HTTP que será interpretado
     *        cómo el nombre de la persona que espera el recibimiento.
     * 
     * @return Devuelve un mapa de tuplas (String,String) que representa la salida
     *         en formato JSON.
     */
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