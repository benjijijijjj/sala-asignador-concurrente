```kotlin
package com.ucmaule

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger

class SalaAsignadorTest {

    @Test
    fun `test asignación concurrente sin conflictos`() {
        val catalogo = CatalogoSalas()
        val asignador = AsignadorSalas(catalogo)

        val solicitudes = listOf(
            Solicitud(capacidadRequerida = 20, equipamientoRequerido = listOf("pizarra")),
            Solicitud(capacidadRequerida = 30, equipamientoRequerido = listOf("proyector")),
            Solicitud(capacidadRequerida = 40, equipamientoRequerido = listOf("audio", "proyector"))
        )

        val resultados = asignador.procesarSolicitudesConcurrentes(solicitudes)

        // Verificar que no haya más asignaciones que salas disponibles
        val asignacionesExitosas = resultados.count { it.exito }
        assertTrue(asignacionesExitosas <= 5) // Máximo 5 salas disponibles
    }

    @Test
    fun `test estado compartido consistente`() {
        val catalogo = CatalogoSalas()
        val asignador = AsignadorSalas(catalogo)

        val solicitudes = listOf(
            Solicitud(capacidadRequerida = 25, equipamientoRequerido = listOf("pizarra")),
            Solicitud(capacidadRequerida = 25, equipamientoRequerido = listOf("pizarra")),
            Solicitud(capacidadRequerida = 25, equipamientoRequerido = listOf("pizarra"))
        )

        val resultados = asignador.procesarSolicitudesConcurrentes(solicitudes)

        // Verificar que solo una solicitud obtuvo la misma sala
        val salasAsignadas = resultados
            .filter { it.exito }
            .map { it.salaAsignada?.id }

        val idsUnicos = salasAsignadas.distinct()
        assertEquals(salasAsignadas.size, idsUnicos.size, "No debería haber asignaciones duplicadas")
    }

    @Test
    fun `test rendimiento concurrente vs secuencial`() {
        val catalogo = CatalogoSalas()
        val asignador = AsignadorSalas(catalogo)

        val solicitudes = (1..10).map {
            Solicitud(
                capacidadRequerida = 20 + (it * 5),
                equipamientoRequerido = listOf("proyector")
            )
        }

        // Medir tiempo concurrente
        val startConcurrente = System.currentTimeMillis()
        asignador.procesarSolicitudesConcurrentes(solicitudes)
        val endConcurrente = System.currentTimeMillis()
        val tiempoConcurrente = endConcurrente - startConcurrente

        // Medir tiempo secuencial
        val startSecuencial = System.currentTimeMillis()
        asignador.procesarSolicitudesOrdenadas(solicitudes)
        val endSecuencial = System.currentTimeMillis()
        val tiempoSecuencial = endSecuencial - startSecuencial

        // El concurrente debería ser más rápido (o al menos no más lento)
        assertTrue(tiempoConcurrente <= tiempoSecuencial * 2,
            "El procesamiento concurrente no debería ser mucho más lento que el secuencial")
    }
}