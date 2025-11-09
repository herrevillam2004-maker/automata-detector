package com.example.automata.automaton;

import java.util.*;

public class AutomataRecorder {
    public static class Transition {
        public String from;
        public String to;
        public String label;

        public Transition(String from, String to, String label) {
            this.from = from;
            this.to = to;
            this.label = label;
        }
    }

    public List<Transition> transitions = new ArrayList<>();

    public void add(String from, String to, String label) {
        transitions.add(new Transition(from, to, label));
    }
}
