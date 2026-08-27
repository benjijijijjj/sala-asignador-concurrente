package com.ucmaule

import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.thread
import kotlin.concurrent.write
import kotlin.concurrent.read

/**
 * Representa una sala con sus características y estado de ocupación.
 */
data class Sala(
    val id: Int,
    val capacidad: Int,
    val equipamiento: List<String>,
    var ocupada: Boolean = false
)

/**
 * Representa una solicitud de asignación de sala.
 */
data class Solicitud(
    val id: Int,
    val capacidadRequerida: Int,
    val equipamientoRequerido: List<String>
)

/**
 * Catálogo de salas - Estado compartido central.
 *
 * PUNTO CONCEPTUAL 1: Estado Compartido (Shared State)
 * Este catálogo representa el estado nombrado o celdas que es accesible
 * por múltiples hilos. El estado compartido es una característica fundamental
 * del paradigma, permitiendo que diferentes hilos observen y modifiquen
 * la misma información de ocupación de salas.
 */
class CatalogoSalas {
    // Estado compartido: lista mutable de salas
    private val salas = mutableListOf<Sala>()

    // Bloqueo para sincronización de lectura/escritura
    private val lock = ReentrantReadWriteLock()

    init {
        // Inicializar catálogo de salas
        salas.addAll(listOf(
            Sala(1, 30, listOf("proyector", "pizarra")),
            Sala(2, 50, listOf("proyector", "audio", "pizarra")),
            Sala(3, 20, listOf("pizarra")),
            Sala(4, 40, listOf("proyector", "audio")),
            Sala(5, 60, listOf("proyector", "pizarra", "audio"))
        ))
    }

    /**
     * Busca la primera sala que cumpla con los requisitos.
     *
     * PUNTO CONCEPTUAL 2: Mecanismos de Coordinación (Lock)
     * El uso de bloqueos (ReentrantReadWriteLock) es esencial para coordinar
     * el acceso al estado compartido. Sin estos mecanismos, múltiples hilos
     * podrían causar condiciones de carrera al verificar y modificar el mismo
     * estado simultáneamente.
     */
    fun buscarYReservarPrimeraSala(solicitud: Solicitud): Sala? {
        lock.write {
            // Buscar la primera sala que cumpla con los requisitos
            // y que esté disponible
            for (sala in salas) {
                if (!sala.ocupada &&
                    sala.capacidad >= solicitud.capacidadRequerida &&
                    solicitud.equipamientoRequerido.all { it in sala.equipamiento }) {
                    // Reservar la sala
                    sala.ocupada = true
                    return sala
                }
            }
            return null
        }
    }

    /**
     * Obtiene el estado actual del catálogo de forma segura.
     */
    fun obtenerEstado(): String {
        lock.read {
            return salas.joinToString("\n") { sala ->
                "Sala ${sala.id}: Capacidad ${sala.capacidad}, " +
                        "Equipamiento ${sala.equipamiento.joinToString()}, " +
                        "Ocupada: ${if (sala.ocupada) "SÍ" else "NO"}"
            }
        }
    }

    /**
     * Obtiene una copia del estado actual de las salas.
     */
    fun obtenerSalas(): List<Sala> {
        lock.read {
            return salas.map { it.copy() }
        }
    }
}

/**
 * Asignador de salas que gestiona el procesamiento concurrente.
 *
 * PUNTO CONCEPTUAL 3: Hilos Independientes (Threads)
 * Este asignador utiliza múltiples hilos para procesar solicitudes
 * concurrentemente. Cada hilo ejecuta una secuencia independiente que
 * interactúa con el estado compartido, representando la característica
 * central del paradigma de concurrencia con estado compartido.
 */
class AsignadorSalas(private val catalogo: CatalogoSalas) {

    // Contador para asignar IDs únicos a las solicitudes
    private var contadorSolicitudes = 0

    /**
     * Procesa una solicitud individual en un hilo separado.
     */
    private fun procesarSolicitud(solicitud: Solicitud): ResultadoAsignacion {
        val resultado = ResultadoAsignacion(solicitud.id)

        // Simular tiempo de procesamiento variable
        Thread.sleep((100..500).random().toLong())

        // Buscar y reservar sala
        val salaAsignada = catalogo.buscarYReservarPrimeraSala(solicitud)

        resultado.exito = salaAsignada != null
        resultado.salaAsignada = salaAsignada

        return resultado
    }

    /**
     * Procesa una lista de solicitudes de forma concurrente.
     * Cada solicitud se ejecuta en su propio hilo.
     */
    fun procesarSolicitudesConcurrentes(solicitudes: List<Solicitud>): List<ResultadoAsignacion> {
        // Crear solicitudes con IDs únicos
        val solicitudesConId = solicitudes.map { solicitud ->
            Solicitud(
                id = ++contadorSolicitudes,
                capacidadRequerida = solicitud.capacidadRequerida,
                equipamientoRequerido = solicitud.equipamientoRequerido
            )
        }

        // Lista para almacenar los resultados
        val resultados = mutableListOf<ResultadoAsignacion>()

        // Crear hilos para cada solicitud
        val hilos = solicitudesConId.map { solicitud ->
            thread(start = true) {
                val resultado = procesarSolicitud(solicitud)
                synchronized(resultados) {
                    resultados.add(resultado)
                }
            }
        }

        // Esperar a que todos los hilos terminen
        hilos.forEach { it.join() }

        return resultados.toList()
    }

    /**
     * Procesa solicitudes manteniendo el orden de llegada.
     * Útil para demostrar la necesidad de sincronización.
     */
    fun procesarSolicitudesOrdenadas(solicitudes: List<Solicitud>): List<ResultadoAsignacion> {
        // Este método demuestra cómo se puede mantener el orden
        // a costa de perder algunos beneficios de la concurrencia
        val resultados = mutableListOf<ResultadoAsignacion>()

        solicitudes.forEach { solicitud ->
            val solicitudConId = Solicitud(
                id = ++contadorSolicitudes,
                capacidadRequerida = solicitud.capacidadRequerida,
                equipamientoRequerido = solicitud.equipamientoRequerido
            )
            val resultado = procesarSolicitud(solicitudConId)
            resultados.add(resultado)
        }

        return resultados
    }
}

/**
 * Resultado de una asignación.
 */
data class ResultadoAsignacion(
    val solicitudId: Int,
    var exito: Boolean = false,
    var salaAsignada: Sala? = null
) {
    override fun toString(): String {
        return if (exito) {
            "Solicitud $solicitudId: ASIGNADA a Sala ${salaAsignada?.id} (Cap: ${salaAsignada?.capacidad})"
        } else {
            "Solicitud $solicitudId: RECHAZADA - No hay sala disponible"
        }
    }
}

/**
 * Función principal del programa.
 */
fun main() {
    println("=== SISTEMA DE ASIGNACIÓN DE SALAS - PROGRAMACIÓN CONCURRENTE CON ESTADO COMPARTIDO ===")
    println("Paradigma: Shared-state concurrent programming (Programación concurrente con estado compartido)")
    println()

    // Crear el catálogo de salas (estado compartido)
    val catalogo = CatalogoSalas()
    val asignador = AsignadorSalas(catalogo)

    // Mostrar estado inicial
    println("=== ESTADO INICIAL DEL CATÁLOGO DE SALAS ===")
    println(catalogo.obtenerEstado())
    println()

    // Crear solicitudes de ejemplo
    val solicitudes = listOf(
        Solicitud(id = 1, capacidadRequerida = 30, equipamientoRequerido = listOf("proyector", "pizarra")),
        Solicitud(id = 2, capacidadRequerida = 45, equipamientoRequerido = listOf("audio", "proyector")),
        Solicitud(id = 3, capacidadRequerida = 25, equipamientoRequerido = listOf("pizarra")),
        Solicitud(id = 4, capacidadRequerida = 55, equipamientoRequerido = listOf("proyector", "audio", "pizarra")),
        Solicitud(id = 5, capacidadRequerida = 35, equipamientoRequerido = listOf("audio"))
    )

    println("=== PROCESANDO SOLICITUDES DE FORMA CONCURRENTE ===")
    println("Cantidad de solicitudes: ${solicitudes.size}")
    println()

    // Procesar concurrentemente
    val inicioConcurrente = System.currentTimeMillis()
    val resultadosConcurrentes = asignador.procesarSolicitudesConcurrentes(solicitudes)
    val finConcurrente = System.currentTimeMillis()

    // Mostrar resultados concurrentes
    println("=== RESULTADOS DE PROCESAMIENTO CONCURRENTE ===")
    resultadosConcurrentes.forEach { println(it) }
    println("Tiempo total: ${finConcurrente - inicioConcurrente}ms")
    println()

    // Mostrar estado final
    println("=== ESTADO FINAL DEL CATÁLOGO DE SALAS ===")
    println(catalogo.obtenerEstado())
    println()

    // Demostrar procesamiento ordenado para comparación
    println("=== DEMOSTRACIÓN DE PROCESAMIENTO ORDENADO (PARA COMPARACIÓN) ===")
    val catalogo2 = CatalogoSalas() // Nuevo catálogo para demostración
    val asignador2 = AsignadorSalas(catalogo2)

    val inicioOrdenado = System.currentTimeMillis()
    val resultadosOrdenados = asignador2.procesarSolicitudesOrdenadas(solicitudes)
    val finOrdenado = System.currentTimeMillis()

    resultadosOrdenados.forEach { println(it) }
    println("Tiempo total: ${finOrdenado - inicioOrdenado}ms")
    println()

    println("=== EXPLICACIÓN DE CONCEPTOS ===")
    println("1. ESTADO COMPARTIDO: El catálogo de salas es accesible por múltiples hilos")
    println("2. MECANISMOS DE COORDINACIÓN: Bloqueos (ReentrantReadWriteLock) sincronizan el acceso")
    println("3. HILOS INDEPENDIENTES: Cada solicitud se procesa en su propio hilo")
    println()
    println("=== BENEFICIOS DEL PARADIGMA ===")
    println("- Procesamiento concurrente: ${resultadosConcurrentes.size} solicitudes procesadas en paralelo")
    println("- Estado centralizado: Todas las solicitudes ven el mismo estado actualizado")
    println("- Escalabilidad: El sistema puede manejar más solicitudes aumentando hilos")
    println()
    println("=== DESAFÍOS DEL PARADIGMA ===")
    println("- Condiciones de carrera: Riesgo si no se sincroniza correctamente")
    println("- Interbloqueos: Posibilidad si se adquieren múltiples bloqueos en diferente orden")
    println("- Nondeterminismo: El orden de los resultados puede variar en cada ejecución")
}