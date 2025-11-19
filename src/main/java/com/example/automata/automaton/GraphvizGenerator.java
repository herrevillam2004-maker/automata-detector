package com.example.automata.automaton;

import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.attribute.Rank;
import guru.nidi.graphviz.attribute.Shape;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.Node;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static guru.nidi.graphviz.model.Factory.graph;
import static guru.nidi.graphviz.model.Factory.node;
import static guru.nidi.graphviz.model.Factory.to;

public class GraphvizGenerator {

    public byte[] generatePngBytes(List<AutomataRecorder.Transition> transitions) throws IOException {

        Map<String, Node> nodes = new HashMap<>();

        for (AutomataRecorder.Transition t : transitions) {
            nodes.putIfAbsent(t.fromState(), node(t.fromState()).with(Shape.CIRCLE));
            nodes.putIfAbsent(t.toState(), node(t.toState()).with(Shape.CIRCLE));
        }

        Graph g = graph("Automata")
                .directed()
                .graphAttr()
                .with(Rank.dir(Rank.RankDir.LEFT_TO_RIGHT));

        for (AutomataRecorder.Transition t : transitions) {
            g = g.with(
                    nodes.get(t.fromState())
                            .link(to(nodes.get(t.toState())).with(Label.of(t.input())))
            );
        }

        // Render PNG as BufferedImage (exists in all versions!)
        var img = Graphviz.fromGraph(g)
                .render(Format.PNG)
                .toImage();

        // Convert to byte[]
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        return baos.toByteArray();
    }
}
