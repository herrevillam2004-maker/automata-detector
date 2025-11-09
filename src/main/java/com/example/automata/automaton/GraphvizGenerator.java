package com.example.automata.automaton;

import guru.nidi.graphviz.attribute.*;
import guru.nidi.graphviz.attribute.Rank.RankDir;
import guru.nidi.graphviz.engine.*;
import guru.nidi.graphviz.model.*;
import java.io.IOException;
import java.util.*;
import static guru.nidi.graphviz.model.Factory.*;
import java.awt.image.BufferedImage;

public class GraphvizGenerator {

    public BufferedImage generatePngFromTransitions(List<AutomataRecorder.Transition> transitions) throws IOException {
        Map<String, Node> nodes = new HashMap<>();

        // Crear nodos únicos
        for (AutomataRecorder.Transition t : transitions) {
            nodes.putIfAbsent(t.from, node(t.from).with(Shape.CIRCLE));
            nodes.putIfAbsent(t.to, node(t.to).with(Shape.CIRCLE));
        }

        // Crear aristas
        Graph g = graph("Automata")
                .directed()
                .graphAttr().with(Rank.dir(RankDir.LEFT_TO_RIGHT));

        for (AutomataRecorder.Transition t : transitions) {
            g = g.with(nodes.get(t.from).link(to(nodes.get(t.to)).with(Label.of(t.label))));
        }

        // Exportar como PNG
        return Graphviz.fromGraph(g)
                .width(800)
                .render(Format.PNG)
                .toImage();
    }
}
