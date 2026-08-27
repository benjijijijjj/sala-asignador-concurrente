



# Sistema de Asignacion de Salas - Programacion Concurrente

Implementacion de un sistema de asignacion de salas utilizando programacion concurrente con estado compartido en Kotlin. Este proyecto demuestra los conceptos fundamentales de hilos, estado compartido y sincronizacion con bloqueos, desarrollado para el curso INF-223 Programacion Avanzada.

---

## Descripcion del Proyecto

El sistema permite gestionar la asignacion de salas de manera concurrente. Cuando llegan multiples solicitudes simultaneamente, el programa las procesa en paralelo usando hilos independientes, compartiendo un catalogo central de salas que se actualiza en tiempo real.

El proyecto implementa el paradigma de programacion concurrente con estado compartido, donde multiples secuencias de ejecucion independientes interactuan mutando una memoria comun.

---

## Caracteristicas Principales

- Procesamiento concurrente de solicitudes usando hilos independientes
- Estado compartido centralizado con el catalogo de salas
- Sincronizacion con bloqueos ReentrantReadWriteLock para evitar condiciones de carrera
- Busqueda automatica de la primera sala disponible que cumpla los requisitos
- Pruebas unitarias para verificar el correcto funcionamiento
- Comparativa de rendimiento entre procesamiento concurrente y secuencial

---

## Requisitos del Sistema
```
- Java Development Kit (JDK) version 11 o superior
- Kotlin 1.9.20 o superior
- Gradle 7.0 o superior (incluido el wrapper en el proyecto)
```
---

## Tutorial de Instalacion y Ejecucion

### Paso 1: Clonar el repositorio

Abre una terminal o PowerShell y ejecuta:
```
git clone https://github.com/benjijii/sala-asignador-concurrente.git
```
Luego ingresa a la carpeta del proyecto:
```
cd sala-asignador-concurrente
```
---

### Paso 2: Verificar la instalacion de Java

Para confirmar que Java esta instalado correctamente, ejecuta:
```
java -version
```
Deberias ver informacion similar a:
```
openjdk version "11.0.20" 2023-07-18
OpenJDK Runtime Environment (build 11.0.20+8)
OpenJDK 64-Bit Server VM (build 11.0.20+8, mixed mode)
```
Si no aparece, descarga e instala JDK desde oracle.com o adoptium.net.

---

### Paso 3: Compilar el proyecto

Ejecuta el siguiente comando para compilar el proyecto:
```
./gradlew clean build
```
En Windows, si el comando anterior no funciona, prueba:
```
gradlew clean build
```
Este proceso descargara las dependencias necesarias y compilara el codigo fuente. La salida esperada es:

BUILD SUCCESSFUL in Xs

---

### Paso 4: Ejecutar el programa

Para ejecutar la aplicacion principal, usa:
```
./gradlew run
```
El programa realizara las siguientes acciones automaticamente:

1. Creara un catalogo con 5 salas predefinidas, cada una con capacidad y equipamiento especifico
2. Mostrara el estado inicial del catalogo con todas las salas disponibles
3. Generara 5 solicitudes de ejemplo con diferentes requisitos
4. Procesara las solicitudes de forma concurrente usando hilos
5. Mostrara los resultados indicando que solicitudes fueron asignadas y cuales rechazadas
6. Mostrara el estado final del catalogo con las salas ocupadas
7. Realizara una comparativa de rendimiento con procesamiento secuencial

---

### Paso 5: Ejecutar las pruebas unitarias

Para verificar que todo funciona correctamente, ejecuta:
```
./gradlew test
```
Deberias ver:
```
BUILD SUCCESSFUL in Xs
3 tests completed, 0 failed
```
Esto confirma que las tres pruebas unitarias han pasado exitosamente.

---

### Paso 6: Interpretar los resultados

Al ejecutar el programa, veras una salida similar a:
```
=== ESTADO INICIAL DEL CATALOGO DE SALAS ===
Sala 1: Capacidad 30, Equipamiento proyector, pizarra, Ocupada: NO
Sala 2: Capacidad 50, Equipamiento proyector, audio, pizarra, Ocupada: NO
Sala 3: Capacidad 20, Equipamiento pizarra, Ocupada: NO
Sala 4: Capacidad 40, Equipamiento proyector, audio, Ocupada: NO
Sala 5: Capacidad 60, Equipamiento proyector, pizarra, audio, Ocupada: NO

=== RESULTADOS DE PROCESAMIENTO CONCURRENTE ===
Solicitud 1: ASIGNADA a Sala 1
Solicitud 2: ASIGNADA a Sala 4
Solicitud 3: ASIGNADA a Sala 3
Solicitud 4: ASIGNADA a Sala 5
Solicitud 5: RECHAZADA - No hay sala disponible
```
Esto demuestra que el sistema proceso todas las solicitudes en paralelo y asigno las salas disponibles.

---

## Explicacion de los Conceptos del Paradigma

### Concepto 1: Estado Compartido

El catalogo de salas es un estado mutable compartido entre todos los hilos. Esto permite que cualquier hilo pueda consultar y modificar la informacion de ocupacion de las salas en tiempo real.

En el codigo, esto se implementa en la clase CatalogoSalas con la variable "salas" que es una lista mutable accesible por todos los hilos.

### Concepto 2: Mecanismos de Coordinacion

Para evitar condiciones de carrera, se utiliza ReentrantReadWriteLock. Este mecanismo garantiza que solo un hilo pueda modificar el estado compartido a la vez, mientras que multiples hilos pueden leer simultaneamente.

En el codigo, esto se aplica en la funcion buscarYReservarPrimeraSala donde se usa lock.write para hacer atomica la verificacion y asignacion de una sala.

### Concepto 3: Hilos Independientes

Cada solicitud se procesa en su propio hilo, permitiendo que multiples evaluaciones ocurran en paralelo. Esto mejora el tiempo de respuesta del sistema.

En el codigo, esto se implementa en la funcion procesarSolicitudesConcurrentes donde se crea un hilo por cada solicitud usando thread(start = true).

---

## Estructura del Proyecto
```
sala-asignador-concurrente/
   ├── build.gradle.kts          Configuracion de Gradle
   ├── settings.gradle.kts       Configuracion del proyecto
   ├── README.md                 Esta documentacion
   ├── LICENSE                   Licencia MIT
   ├── .gitignore                Archivos ignorados por Git
   └── src/
        ├── main/
        │   └── kotlin/
        │       └── com/
        │           └── ucmaule/
        │               └── SalaAsignador.kt    Codigo principal
        └── test/
            └── kotlin/
                └── com/
                    └── ucmaule/
                        └── SalaAsignadorTest.kt    Pruebas unitarias
```
---

## Clases Principales

Sala: Representa una sala con identificador, capacidad, equipamiento y estado de ocupacion.

Solicitud: Representa una peticion de asignacion con requisitos de capacidad y equipamiento.

CatalogoSalas: Contiene el estado compartido del sistema. Gestiona el catalogo de salas y las operaciones de busqueda y reserva con sincronizacion.

AsignadorSalas: Gestiona el procesamiento concurrente. Crea y coordina los hilos para procesar solicitudes en paralelo.

ResultadoAsignacion: Almacena el resultado de cada asignacion indicando exito o rechazo.

---

## Comandos Utiles
```
./gradlew clean build   Limpia y compila el proyecto
```
```
./gradlew run           Ejecuta la aplicacion principal
```
```
./gradlew test          Ejecuta las pruebas unitarias
```
```
./gradlew clean         Limpia los archivos compilados
```
---

## Contacto

Integrantes: Benjamín Romo, Benjamín Vásquez, Benjamín Ruz, Marco Rifo y Rubén Sánchez.

Docente: Ruber Hernandez

Curso: INF-223 Programacion Avanzada

Fecha: 26 de agosto de 2026

---

## Licencia

Este proyecto esta bajo la licencia MIT. Consulta el archivo LICENSE para mas detalles.

---

## Video Demostrativo

El video demostrativo de la implementacion y ejecucion del sistema esta disponible en el siguiente enlace: https://github.com/user-attachments/assets/c6735d48-c502-4a45-8cc3-f83563ce9fc0
