package com.example.automata.controllers;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Valentina Gray
 */

import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.regex.*;

@RestController
@RequestMapping("/api")

public class AnalizadorController {
    
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

        while (matcher.find()) {
            String token = matcher.group();
            if (token.startsWith("@")) {
                menciones.add(token);
            } else if (token.startsWith("#")) {
                hashtags.add(token);
            } else if (token.startsWith("http") || token.startsWith("www")) {
                urls.add(token);
            }
        }

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("menciones", menciones);
        resultado.put("hashtags", hashtags);
        resultado.put("urls", urls);
        resultado.put("texto", texto);

        return resultado;
    }
}
