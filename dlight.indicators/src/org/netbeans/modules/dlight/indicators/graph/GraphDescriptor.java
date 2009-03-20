package org.netbeans.modules.dlight.indicators.graph;

import java.awt.Color;

public class GraphDescriptor {

    private final Color color;
    private final String description;

    public GraphDescriptor(Color color, String description) {
        this.color = color;
        this.description = description;
    }

    public Color getColor() {
        return color;
    }

    public String getDescription() {
        return description;
    }
}
