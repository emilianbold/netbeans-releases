/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.api.visual.widget;

import java.awt.*;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;

/**
 * @author David Kaspar
 */
// TODO - addComponentResizeListener
// TODO - fix calculateClientArea method - use convertViewToScene instead
public final class ComponentWidget extends Widget {

    private Component component;
    private boolean componentAdded;
    private double zoomFactor = Double.MIN_VALUE;
    private ComponentSceneListener validateListener;
    private ComponentComponentListener componentListener;

    public ComponentWidget (Scene scene, Component component) {
        super (scene);
        this.component = component;
        validateListener = new ComponentSceneListener ();
        componentListener = new ComponentComponentListener ();
        attach ();
    }

    public final void attach () {
        getScene ().addSceneListener (validateListener);
    }

    public final void detach () {
        getScene ().removeSceneListener (validateListener);
    }

    public final Component getComponent () {
        return component;
    }

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
            scene.getComponent ().add (component);
            component.addComponentListener (componentListener);
            componentAdded = true;
        }
        component.removeComponentListener (componentListener);
        component.setBounds (scene.convertSceneToView (convertLocalToScene (getBounds ())));
        component.addComponentListener (componentListener);
    }

    private void removeComponent () {
        Scene scene = getScene ();
        if (componentAdded) {
            component.removeComponentListener (componentListener);
            scene.getComponent ().remove (component);
            componentAdded = false;
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
