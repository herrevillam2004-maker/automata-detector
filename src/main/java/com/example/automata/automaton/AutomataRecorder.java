package com.example.automata.automaton;

import java.util.ArrayList;
import java.util.List;

public class AutomataRecorder {

    private final List<Transition> transitions = new ArrayList<>();

    public void record(String fromState, String input, String toState) {
        transitions.add(new Transition(fromState, toState, input));
    }

    public List<Transition> getTransitions() {
        return transitions;
    }

    // Nombres EXACTOS que usa GraphvizGenerator
    public record Transition(String fromState, String toState, String input) {}
}
