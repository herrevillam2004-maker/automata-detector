package com.example.automata.controllers;

import com.example.automata.automaton.AutomataRecorder;
import com.example.automata.automaton.GraphvizGenerator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.*;
import java.awt.image.BufferedImage;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class AnalizadorController {

    private final GraphvizGenerator graphvizGenerator = new GraphvizGenerator();

    @PostMapping("/analizar")
    public Map<String, Object> analizar(@RequestBody Map<String, String> body) {
        String texto = body.get("texto");

        // Regex para @menciones, #hashtags y URLs
        Pattern pattern = Pattern.compile(
                "(?:@\\w+)|(?:#\\w+)|(?:(?:https?://|www\\.)\\S+)",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = pattern.matcher(texto);
        List<String> menciones = new ArrayList<>();
        List<String> hashtags = new ArrayList<>();
        List<String> urls = new ArrayList<>();

        // Registrar transiciones para el autómata
        AutomataRecorder recorder = new AutomataRecorder();
        recorder.add("START", "READING", "texto");

        while (matcher.find()) {
            String token = matcher.group();
            if (token.startsWith("@")) {
                menciones.add(token);
                recorder.add("READING", "MENTION", token);
            } else if (token.startsWith("#")) {
                hashtags.add(token);
                recorder.add("READING", "HASHTAG", token);
            } else if (token.startsWith("http") || token.startsWith("www")) {
                urls.add(token);
                recorder.add("READING", "URL", token);
            }
        }

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("menciones", menciones);
        resultado.put("hashtags", hashtags);
        resultado.put("urls", urls);
        resultado.put("texto", texto);

        return resultado;
    }

    // 🔹 Nuevo endpoint para ver el autómata generado
    @GetMapping("/automata")
    public ResponseEntity<byte[]> automataGraph() throws IOException {
        AutomataRecorder recorder = new AutomataRecorder();
        recorder.add("START", "MENTION", "@");
        recorder.add("START", "HASHTAG", "#");
        recorder.add("START", "URL", "http");
        recorder.add("MENTION", "TEXT", "a-z");
        recorder.add("HASHTAG", "TEXT", "a-z");
        recorder.add("URL", "TEXT", "/");

        BufferedImage image = graphvizGenerator.generatePngFromTransitions(recorder.transitions);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);

        return ResponseEntity.ok()
                .header("Content-Type", "image/png")
                .body(baos.toByteArray());
    }
}
