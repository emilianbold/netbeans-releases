/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.java.module.graph;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Scene;

/**
 *
 * @author Tomas Zezula
 */
final class EdgeWidget extends ConnectionWidget implements Zoomable {
    private final DependencyEdge edge;

    EdgeWidget(
            @NonNull final Scene scene,
            @NonNull final DependencyEdge edge) {
        super(scene);
        this.edge = edge;
        setTargetAnchorShape(AnchorShape.TRIANGLE_FILLED);
    }

    @Override
    public void updateReadableZoom() {
    }
}
