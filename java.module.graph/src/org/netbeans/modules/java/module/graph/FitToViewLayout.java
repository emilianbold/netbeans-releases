/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.java.module.graph;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.visual.layout.SceneLayout;
import org.netbeans.api.visual.widget.Widget;

/**
 * Fits widget into current pane.
 * Todo: Taken from maven.graph, API?
 * @author Milos Kleint
 */
class FitToViewLayout extends SceneLayout {

    private final DependencyGraphScene depScene;
    private List<? extends Widget> widgets;

    FitToViewLayout(DependencyGraphScene scene) {
        super(scene);
        this.depScene = scene;
    }


    void fitToView(@NullAllowed List<? extends Widget> widgets) {
        this.widgets = widgets;
        this.invokeLayout();
    }

    @Override
    protected void performLayout() {
        Rectangle rectangle = null;
        List<? extends Widget> toFit = widgets != null ? widgets : depScene.getChildren();
        if (toFit == null) {
            return;
        }

        for (Widget widget : toFit) {
            Rectangle bounds = widget.getBounds();
            if (bounds == null) {
                continue;
            }
            if (rectangle == null) {
                rectangle = widget.convertLocalToScene(bounds);
            } else {
                rectangle = rectangle.union(widget.convertLocalToScene(bounds));
            }
        }
        if (rectangle == null) {
            return;
        }
        // margin around
        if (widgets == null) {
            rectangle.grow(5, 5);
        } else {
            rectangle.grow(25, 25);
        }
        Dimension dim = rectangle.getSize();
        Dimension viewDim = depScene.getOwner().getScrollPane().
                getViewportBorderBounds ().getSize ();
        double zf = Math.min ((double) viewDim.width / dim.width, (double) viewDim.height / dim.height);
        if (depScene.isAnimated()) {
            if (widgets == null) {
                depScene.getSceneAnimator().animateZoomFactor(zf);
            } else {
                CenteredZoomAnimator cza = new CenteredZoomAnimator(depScene.getSceneAnimator());
                cza.setZoomFactor(zf,
                        new Point((int)rectangle.getCenterX(), (int)rectangle.getCenterY()));
            }
        } else {
            depScene.setMyZoomFactor (zf);
        }
    }
}