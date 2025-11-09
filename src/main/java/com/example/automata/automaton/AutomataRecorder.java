/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.automata.automaton;

/**
 *
 * @author Valentina Gray
 */
import java.util.*;
import java.util.stream.Collectors;

/**
 * Procesa el texto carácter por carácter y guarda
 * la secuencia de estados y transiciones para graficar.
 */
public class AutomataRecorder {

    public enum State {
        START,
        IN_MENTION,
        IN_HASHTAG,
        IN_URL,
        IN_TEXT
    }

    public static class Transition {
        public final String from;
        public final String to;
        public final String label; // what character(s) triggered it
        public Transition(String from, String to, String label) {
            this.from = from; this.to = to; this.label = label;
        }
    }

    private final List<Transition> transitions = new ArrayList<>();
    private final List<String> visitedStates = new ArrayList<>();

    // Nombres de estados dinámicos para la ruta de procesamiento (q0,q1,...)
    private int stateCounter = 0;

    private String newStateName(State s) {
        return s.name() + "_" + (stateCounter++);
    }

    /**
     * Procesa el texto y produce las transiciones entre estados.
     */
    public void process(String texto) {
        transitions.clear();
        visitedStates.clear();
        stateCounter = 0;

        State current = State.START;
        String currentStateName = newStateName(current);
        visitedStates.add(currentStateName);

        // buffers y helpers
        StringBuilder tokenBuf = new StringBuilder();
        StringBuilder urlBuf = new StringBuilder();

        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            String prevStateName = currentStateName;

            switch (current) {
                case START:
                case IN_TEXT:
                    if (c == '@') {
                        current = State.IN_MENTION;
                        tokenBuf.setLength(0);
                        currentStateName = newStateName(current);
                        transitions.add(new Transition(prevStateName, currentStateName, "@"));
                    } else if (c == '#') {
                        current = State.IN_HASHTAG;
                        tokenBuf.setLength(0);
                        currentStateName = newStateName(current);
                        transitions.add(new Transition(prevStateName, currentStateName, "#"));
                    } else if (startsUrlAt(texto, i)) {
                        // jump into URL state, capture prefix (http/https/www)
                        current = State.IN_URL;
                        urlBuf.setLength(0);
                        String prefix = getUrlPrefix(texto, i);
                        currentStateName = newStateName(current);
                        transitions.add(new Transition(prevStateName, currentStateName, prefix));
                        // advance pointer for prefix chars already consumed in logic below (we will append them to urlBuf and move i)
                        int advance = prefix.length() - 1; // loop will still increment i
                        for (int k = 0; k < advance; k++) {
                            i++;
                            if (i < texto.length()) urlBuf.append(texto.charAt(i));
                        }
                    } else {
                        // regular text char -> stay or go to IN_TEXT
                        if (current != State.IN_TEXT) {
                            current = State.IN_TEXT;
                            currentStateName = newStateName(current);
                            transitions.add(new Transition(prevStateName, currentStateName, String.valueOf(c)));
                        } else {
                            // staying in IN_TEXT - optionally record self-loop
                            transitions.add(new Transition(prevStateName, prevStateName, String.valueOf(c)));
                        }
                    }
                    break;

                case IN_MENTION:
                    if (Character.isLetterOrDigit(c) || c == '_' ) {
                        tokenBuf.append(c);
                        // stay in mention: add self loop label
                        transitions.add(new Transition(prevStateName, prevStateName, String.valueOf(c)));
                    } else {
                        // end mention -> go to IN_TEXT
                        current = State.IN_TEXT;
                        currentStateName = newStateName(current);
                        transitions.add(new Transition(prevStateName, currentStateName, "ε")); // epsilon-like transition
                        // we might want to reprocess this char in IN_TEXT, so decrement i
                        i--;
                    }
                    break;

                case IN_HASHTAG:
                    if (Character.isLetterOrDigit(c) || c == '_') {
                        tokenBuf.append(c);
                        transitions.add(new Transition(prevStateName, prevStateName, String.valueOf(c)));
                    } else {
                        current = State.IN_TEXT;
                        currentStateName = newStateName(current);
                        transitions.add(new Transition(prevStateName, currentStateName, "ε"));
                        i--;
                    }
                    break;

                case IN_URL:
                    // accept a set of url-safe chars until whitespace
                    if (!Character.isWhitespace(c)) {
                        urlBuf.append(c);
                        transitions.add(new Transition(prevStateName, prevStateName, String.valueOf(c)));
                    } else {
                        current = State.IN_TEXT;
                        currentStateName = newStateName(current);
                        transitions.add(new Transition(prevStateName, currentStateName, "ε"));
                    }
                    break;
            }
        }

        // end of string — optionally close token states to IN_TEXT
        // (not strictly necessary for the graph but can be helpful)
    }

    private boolean startsUrlAt(String t, int i) {
        String rest = t.substring(i).toLowerCase();
        return rest.startsWith("http://") || rest.startsWith("https://") || rest.startsWith("www.");
    }

    private String getUrlPrefix(String t, int i) {
        String rest = t.substring(i).toLowerCase();
        if (rest.startsWith("https://")) return "https://";
        if (rest.startsWith("http://")) return "http://";
        if (rest.startsWith("www.")) return "www.";
        return "";
    }

    public List<Transition> getTransitions() {
        return Collections.unmodifiableList(transitions);
    }
}
