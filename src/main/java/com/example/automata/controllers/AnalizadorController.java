package com.example.automata.controllers;

// import com.example.automata.automaton.AutomataRecorder;
// import com.example.automata.automaton.GraphvizGenerator;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import javax.imageio.ImageIO;
// import java.io.ByteArrayOutputStream;
// import java.io.IOException;
// import java.util.*;
// import java.util.regex.*;
// import java.awt.image.BufferedImage;



// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;
// import org.springframework.web.multipart.MultipartFile;

// import java.util.HashMap;
// import java.util.Map;




import com.example.automata.automaton.AutomataRecorder;
import com.example.automata.automaton.GraphvizGenerator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class AnalizadorController {





    // private final GraphvizGenerator graphvizGenerator = new GraphvizGenerator();

    // @PostMapping("/analizar")
    // public Map<String, Object> analizar(@RequestBody Map<String, String> body) {
    //     String texto = body.get("texto");

    //     // Regex para @menciones, #hashtags y URLs
    //     Pattern pattern = Pattern.compile(
    //             "(?:@\\w+)|(?:#\\w+)|(?:(?:https?://|www\\.)\\S+)",
    //             Pattern.CASE_INSENSITIVE
    //     );

    //     Matcher matcher = pattern.matcher(texto);
    //     List<String> menciones = new ArrayList<>();
    //     List<String> hashtags = new ArrayList<>();
    //     List<String> urls = new ArrayList<>();

    //     // Registrar transiciones para el autómata
    //     AutomataRecorder recorder = new AutomataRecorder();
    //     recorder.add("START", "READING", "texto");

    //     while (matcher.find()) {
    //         String token = matcher.group();
    //         if (token.startsWith("@")) {
    //             menciones.add(token);
    //             recorder.add("READING", "MENTION", token);
    //         } else if (token.startsWith("#")) {
    //             hashtags.add(token);
    //             recorder.add("READING", "HASHTAG", token);
    //         } else if (token.startsWith("http") || token.startsWith("www")) {
    //             urls.add(token);
    //             recorder.add("READING", "URL", token);
    //         }
    //     }

    //     Map<String, Object> resultado = new HashMap<>();
    //     resultado.put("menciones", menciones);
    //     resultado.put("hashtags", hashtags);
    //     resultado.put("urls", urls);
    //     resultado.put("texto", texto);

    //     return resultado;
    // }

    // // 🔹 Nuevo endpoint para ver el autómata generado
    // @GetMapping("/automata")
    // public ResponseEntity<byte[]> automataGraph() throws IOException {
    //     AutomataRecorder recorder = new AutomataRecorder();
    //     recorder.add("START", "MENTION", "@");
    //     recorder.add("START", "HASHTAG", "#");
    //     recorder.add("START", "URL", "http");
    //     recorder.add("MENTION", "TEXT", "a-z");
    //     recorder.add("HASHTAG", "TEXT", "a-z");
    //     recorder.add("URL", "TEXT", "/");

    //     BufferedImage image = graphvizGenerator.generatePngFromTransitions(recorder.transitions);
    //     ByteArrayOutputStream baos = new ByteArrayOutputStream();
    //     ImageIO.write(image, "png", baos);

    //     return ResponseEntity.ok()
    //             .header("Content-Type", "image/png")
    //             .body(baos.toByteArray());
    // }

    //  @PostMapping("/analizar-con-grafico")
    // public ResponseEntity<Map<String, Object>> analizarConGrafico(@RequestBody Map<String, String> body) throws IOException {
    //     String texto = body.getOrDefault("texto", "");

    //     // mismo análisis (regex)
    //     Pattern pattern = Pattern.compile(
    //             "(?:@\\w+)|(?:#\\w+)|(?:(?:https?://|www\\.)\\S+)",
    //             Pattern.CASE_INSENSITIVE
    //     );

    //     Matcher matcher = pattern.matcher(texto);
    //     List<String> menciones = new ArrayList<>();
    //     List<String> hashtags = new ArrayList<>();
    //     List<String> urls = new ArrayList<>();

    //     while (matcher.find()) {
    //         String token = matcher.group();
    //         if (token.startsWith("@")) {
    //             menciones.add(token);
    //         } else if (token.startsWith("#")) {
    //             hashtags.add(token);
    //         } else {
    //             urls.add(token);
    //         }
    //     }

    //     // Registrar transiciones reales en el recorder
    //     AutomataRecorder recorder = new AutomataRecorder();
    //     recorder.recordFromText(texto, menciones, hashtags, urls);

    //     // Generar PNG bytes
    //     byte[] png = graphvizGenerator.generatePngBytes(recorder.getTransitions());

    //     // Codificar a base64 y construir respuesta
    //     String b64 = "data:image/png;base64," + Base64.getEncoder().encodeToString(png);

    //     Map<String, Object> resultado = new HashMap<>();
    //     resultado.put("menciones", menciones);
    //     resultado.put("hashtags", hashtags);
    //     resultado.put("urls", urls);
    //     resultado.put("texto", texto);
    //     resultado.put("automataImageBase64", b64);

    //     return ResponseEntity.ok(resultado);
    // }
    
    private final GraphvizGenerator graphvizGenerator = new GraphvizGenerator();

    //  ENDPOINT 1: Devuelve la imagen del autómata (PNG)
    @PutMapping(value = "/automata", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> analizar(@RequestBody TextoRequest request) throws IOException {

        String texto = request.getTexto();

        if (texto == null || texto.isBlank()) {
            return ResponseEntity.badRequest().body("El campo 'texto' no puede estar vacío");
        }

        // 1. Simulación del autómata + grabar transiciones
        AutomataRecorder recorder = new AutomataRecorder();

        String estadoActual = "S0";
        for (char c : texto.toCharArray()) {
            String siguiente = "S" + (c % 5);
            recorder.record(estadoActual, String.valueOf(c), siguiente);
            estadoActual = siguiente;
        }

        // 2. Generar PNG usando transiciones capturadas
        byte[] pngBytes = graphvizGenerator.generatePngBytes(recorder.getTransitions());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pngBytes);
    }


    // Clase para recibir JSON
    public static class TextoRequest {
        private String texto;
        public String getTexto() { return texto; }
        public void setTexto(String texto) { this.texto = texto; }
    }
}
