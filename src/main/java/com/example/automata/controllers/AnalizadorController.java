package com.example.automata.controllers;

import com.example.automata.automaton.AutomataRecorder;
import com.example.automata.automaton.GraphvizGenerator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class AnalizadorController {

 
    private final GraphvizGenerator graphvizGenerator = new GraphvizGenerator();

    //  ENDPOINT 1: Devuelve imagen PNG del autómata
    @PutMapping(value = "/analizar", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> analizar(@RequestBody TextoRequest request) throws IOException {

        String texto = request.getTexto();

        if (texto == null || texto.isBlank()) {
            return ResponseEntity.badRequest().body("El campo 'texto' no puede estar vacío");
        }

        // Simulación del autómata (DUMMY)
        AutomataRecorder recorder = new AutomataRecorder();

        String estadoActual = "S0";
        for (char c : texto.toCharArray()) {
            String siguiente = "S" + (c % 5);
            recorder.record(estadoActual, String.valueOf(c), siguiente);
            estadoActual = siguiente;
        }

        byte[] pngBytes = graphvizGenerator.generatePngBytes(recorder.getTransitions());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pngBytes);
    }

    //  ENDPOINT 1: Devuelve imagen PNG del autómata (CON LIMPIEZA DE ESPACIOS)
    @PutMapping(value = "/analizar/clean", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> analizarclean(@RequestBody TextoRequest request) throws IOException {

        String texto = Optional.ofNullable(request.getTexto()).orElse("").replaceAll("\\s+", "");
        if (texto.isEmpty()) {
            return ResponseEntity.badRequest().body("El campo 'texto' no puede estar vacío");
        }

        // Simulación del autómata (DUMMY)
        AutomataRecorder recorder = new AutomataRecorder();

        String estadoActual = "S0";
        for (char c : texto.toCharArray()) {
            String siguiente = "S" + (c % 5);
            recorder.record(estadoActual, String.valueOf(c), siguiente);
            estadoActual = siguiente;
        }

        byte[] pngBytes = graphvizGenerator.generatePngBytes(recorder.getTransitions());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pngBytes);
    }

    //  ENDPOINT 3: Extraer menciones, hashtags, urls y texto
    @PutMapping(value = "/extraer", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> extraer(@RequestBody TextoRequest request) {

        String texto = request.getTexto();

        if (texto == null || texto.isBlank()) {
            return ResponseEntity.badRequest().body("El campo 'texto' no puede estar vacío");
        }

        // ------------------------------
        // Detectar menciones
        // ------------------------------
        Pattern pMencion = Pattern.compile("@[a-zA-Z0-9_]+");
        Matcher mMencion = pMencion.matcher(texto);
        List<String> menciones = new ArrayList<>();
        while (mMencion.find()) menciones.add(mMencion.group());

        // ------------------------------
        // Detectar hashtags
        // ------------------------------
        Pattern pHash = Pattern.compile("#[a-zA-Z0-9_]+");
        Matcher mHash = pHash.matcher(texto);
        List<String> hashtags = new ArrayList<>();
        while (mHash.find()) hashtags.add(mHash.group());

        // ------------------------------
        // Detectar URLs
        // ------------------------------
        Pattern pUrl = Pattern.compile("(http://|https://)[^\\s]+");
        Matcher mUrl = pUrl.matcher(texto);
        List<String> urls = new ArrayList<>();
        while (mUrl.find()) urls.add(mUrl.group());

        // ------------------------------
        // Armar respuesta JSON
        // ------------------------------
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("menciones", menciones);
        resultado.put("hashtags", hashtags);
        resultado.put("urls", urls);
        resultado.put("texto", texto);

        return ResponseEntity.ok(resultado);
    }

    // ---------------------------------------------------------
    // Clase Request
    // ---------------------------------------------------------
    public static class TextoRequest {
        private String texto;
        public String getTexto() { return texto; }
        public void setTexto(String texto) { this.texto = texto; }
    }
}
