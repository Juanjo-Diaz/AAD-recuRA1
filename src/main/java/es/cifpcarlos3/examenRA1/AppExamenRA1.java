package es.cifpcarlos3.examenRA1;

import es.cifpcarlos3.examenRA1.vo.Jugador;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
//import tools.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class AppExamenRA1 {

    // Rutas (raíz del proyecto)
    private static final Path BASE = Path.of("src","main","java","es","cifpcarlos3","examenRA1");
    private static final Path RUTA_JUGADORES_TXT = BASE.resolve("jugadores.txt");
    private static final Path RUTA_SALIDA = BASE.resolve("salida");
    private static final Path RUTA_BINARIO = RUTA_SALIDA.resolve("jugadores.dat");
    private static final Path RUTA_JSON = RUTA_SALIDA.resolve("jugadores.json");

    // Lista en memoria
    private static final List<Jugador> jugadores = new ArrayList<>();

    // Jackson 2 (JSON)
    private static final JsonMapper JSON_MAPPER = JsonMapper.builder()
            .enable(SerializationFeature.INDENT_OUTPUT)    // JSON "bonito"
            .build();

    public static void main(String[] args) {

        try (Scanner sc = new Scanner(System.in)) {
            boolean salir = false;

            while (!salir) {
                mostrarMenu();
                System.out.print("Opción: ");

                String opcion = sc.nextLine().trim();

                switch (opcion) {
                    case "1" -> cargarJugadoresDesdeTxt();
                    case "2" -> guardarJugadoresEnBinario();
                    case "3" -> exportarJugadoresAJson();
                    case "4" -> leerBinarioYMostrar();
                    case "0" -> {
                        System.out.println("Saliendo del programa...");
                        salir = true;
                    }
                    default -> System.out.println("Opción no válida. Intente de nuevo.");
                }
            }
        } catch (InputMismatchException e) {
            System.err.println("Error de entrada. Terminando programa: " + e.getMessage());
        }
    }

    // ================================
    // Menú
    // ================================
    private static void mostrarMenu() {
        System.out.println();
        System.out.println("EXAMEN RA1 - Gestión jugadores");
        System.out.println("------------------------------");
        System.out.println("1. Cargar jugadores desde jugadores.txt");
        System.out.println("2. Guardar jugadores en binario");
        System.out.println("3. Exportar jugadores a JSON");
        System.out.println("4. Leer binario y mostrar jugadores por pantalla");
        System.out.println("0. Salir");
        System.out.println("------------------------------");
    }

    // ================================
    // Opción 1: Cargar desde jugadores.txt
    // ================================
    private static void cargarJugadoresDesdeTxt() {

        if (!Files.exists(RUTA_JUGADORES_TXT)) {
            System.err.println("ERROR: No se encuentra el fichero " + RUTA_JUGADORES_TXT.toAbsolutePath());
            return;
        }

        jugadores.clear(); // para no duplicar si se llama varias veces

        int lineas = 0;
        int cargados = 0;

        try (var br = Files.newBufferedReader(RUTA_JUGADORES_TXT, StandardCharsets.UTF_8)) {

            String linea;
            while ((linea = br.readLine()) != null) {
                lineas++;

                if (linea.isBlank()) {
                    continue;
                }

                String[] partes = linea.split(";");
                if (partes.length < 3) {
                    System.out.println("AVISO: línea " + lineas + " ignorada (formato incorrecto)");
                    continue;
                }

                String nombreUsuario = partes[0].trim();
                String email = partes[1].trim();
                String puntuacionTexto = partes[2].trim();

                try {
                    int puntuacion = Integer.parseInt(puntuacionTexto);
                    jugadores.add(new Jugador(nombreUsuario, email, puntuacion));
                    cargados++;
                } catch (NumberFormatException e) {
                    System.out.println("AVISO: línea " + lineas + " ignorada (puntuación no válida: " + puntuacionTexto + ")");
                }
            }

            System.out.printf("Lectura completada. Jugadores cargados: %d (líneas leídas: %d)%n",
                    cargados, lineas);

        } catch (IOException e) {
            System.err.println("ERROR al leer " + RUTA_JUGADORES_TXT + ": " + e.getMessage());
        }
    }

    // ================================
    // Opción 2: Guardar binario jugadores.dat
    // ================================
    private static void guardarJugadoresEnBinario() {
        if (jugadores.isEmpty()) {
            System.out.println("No hay jugadores cargados. Use primero la opción 1.");
            return;
        }

        try {
            if (Files.notExists(RUTA_SALIDA)) {
                Files.createDirectories(RUTA_SALIDA);
            }

            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(RUTA_BINARIO))) {
                oos.writeObject(jugadores);
                System.out.println("Jugadores serializados correctamente en: " + RUTA_BINARIO.toAbsolutePath());
            }

        } catch (IOException e) {
            System.err.println("ERROR al guardar binario: " + e.getMessage());
        }
    }

    // ================================
    // Opción 3: Exportar a JSON
    // ================================
    private static void exportarJugadoresAJson() {
        if (jugadores.isEmpty()) {
            System.out.println("No hay jugadores cargados. Use primero la opción 1.");
            return;
        }

        try {
            if (Files.notExists(RUTA_SALIDA)) {
                Files.createDirectories(RUTA_SALIDA);
            }

            try (var writer = Files.newBufferedWriter(RUTA_JSON, StandardCharsets.UTF_8)) {
                JSON_MAPPER.writeValue(writer, jugadores);
                System.out.println("Fichero JSON generado correctamente en: " + RUTA_JSON.toAbsolutePath());
            }

        } catch (IOException e) {
            System.err.println("ERROR al exportar a JSON: " + e.getMessage());
        }
    }

    // ================================
    // Opción 4: Leer binario y mostrar
    // ================================
    private static void leerBinarioYMostrar() {
        if (!Files.exists(RUTA_BINARIO)) {
            System.out.println("No se encuentra el fichero binario. Use antes la opción 2.");
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(RUTA_BINARIO))) {

            List<Jugador> jugadoresLeidos = (List<Jugador>) ois.readObject();

            if (jugadoresLeidos == null || jugadoresLeidos.isEmpty()) {
                System.out.println("El fichero binario no contiene jugadores.");
                return;
            }

            System.out.println("Lista de jugadores leídos de binario:");
            System.out.println("--------------------------------------");
            jugadoresLeidos.forEach(j -> System.out.println(j.toString()));

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("ERROR al leer binario: " + e.getMessage());
        }
    }
}
