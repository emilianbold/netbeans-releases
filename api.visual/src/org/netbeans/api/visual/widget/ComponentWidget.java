/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.api.visual.widget;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

/**
 * This widget allows to use an AWT/Swing component in the scene. The widget itself just represents and reserve the place
 * occupied by the component. When a component is resized, then reserved place is recalculated. The component placement
 * is automatically update based on the placement of the widget.
 * The widget also paints the component in the satelite views.
 * <p>
 * When a widget is added into the scene, it has to attach a scene listener for automatic recalculation. The attaching
 * is done automatically for the first time.
 *
 * @author David Kaspar
 */
// TODO - addComponentResizeListener
// TODO - fix calculateClientArea method - use convertViewToScene instead
public class ComponentWidget extends Widget {

    private Component component;
    private boolean componentAdded;
    private double zoomFactor = Double.MIN_VALUE;
    private ComponentSceneListener validateListener;
    private ComponentComponentListener componentListener;

    /**
     * Creates a component widget.
     * @param scene the scene
     * @param component the AWT/Swing component
     */
    public ComponentWidget (Scene scene, Component component) {
        super (scene);
        this.component = component;
        validateListener = null;
        componentListener = new ComponentComponentListener ();
        attach ();
    }

    /**
     * Attaches the scene listener for automatic recalculation. The attaching is done automatically for the first time.
     * You have to call the attach method when you are reusing the component widget after calling the detach method.
     */
    public final void attach () {
        if (validateListener != null)
            return;
        validateListener = new ComponentSceneListener ();
        getScene ().addSceneListener (validateListener);
    }

    /**
     * Detaches the scene listener for automatic recaulculation. The detaching is required for preventing memory leaks
     * after the component widget is remove from the scene. When you want to reuse the widget again, you have call
     * the attach method.
     */
    public final void detach () {
        if (validateListener == null)
            return;
        getScene ().removeSceneListener (validateListener);
        validateListener = null;
    }

    /**
     * Returns a AWT/Swing component.
     * @return the AWT/Swing component
     */
    public final Component getComponent () {
        return component;
    }

    /**
     * Calculates a client area from the preferred size of the component.
     * @return the calculated client area
     */
    protected final Rectangle calculateClientArea () {
        Dimension preferredSize = component.getPreferredSize ();
        zoomFactor = getScene ().getZoomFactor ();
        preferredSize.width = (int) Math.floor (preferredSize.width / zoomFactor);
        preferredSize.height = (int) Math.floor (preferredSize.height / zoomFactor);
        return new Rectangle (preferredSize);
    }

    private boolean isWidgetInScene () {
        Scene scene = getScene ();
        Widget widget = this;
        while (widget != null) {
            if (widget == scene)
                return true;
            widget = widget.getParentWidget ();
        }
        return false;
    }

    private void addComponent () {
        Scene scene = getScene ();
        if (! componentAdded) {
            scene.getView ().add (component);
            component.addComponentListener (componentListener);
            componentAdded = true;
        }
        component.removeComponentListener (componentListener);
        component.setBounds (scene.convertSceneToView (convertLocalToScene (getClientArea ())));
        component.addComponentListener (componentListener);
    }

    private void removeComponent () {
        Scene scene = getScene ();
        if (componentAdded) {
            component.removeComponentListener (componentListener);
            scene.getView ().remove (component);
            componentAdded = false;
        }
    }

    /**
     * Paints the component widget.
     */
    protected void paintWidget () {
        if (getScene ().isPaintEverything ()) {
            Graphics2D graphics = getGraphics ();
            Rectangle bounds = getClientArea ();
            AffineTransform previousTransform = graphics.getTransform ();
            graphics.translate (bounds.x, bounds.y);
            double zoomFactor = getScene ().getZoomFactor ();
            graphics.scale (1 / zoomFactor, 1 / zoomFactor);
            component.paint (graphics);
            graphics.setTransform (previousTransform);
        }
    }

    private final class ComponentSceneListener implements Scene.SceneListener {

        public void sceneRepaint () {
        }

        public void sceneValidating () {
            if (getScene ().getZoomFactor () - zoomFactor != 0.0)
                revalidate ();
        }

        public void sceneValidated () {
            if (isWidgetInScene ())
                addComponent ();
            else
                removeComponent ();
        }
    }

    private final class ComponentComponentListener implements ComponentListener {

        public void componentResized (ComponentEvent e) {
            revalidate ();
        }

        public void componentMoved (ComponentEvent e) {
            revalidate ();
        }

        public void componentShown (ComponentEvent e) {
        }

        public void componentHidden (ComponentEvent e) {
        }

    }

}
