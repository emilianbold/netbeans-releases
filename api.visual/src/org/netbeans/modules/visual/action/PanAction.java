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
package org.netbeans.modules.visual.action;

import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.action.WidgetAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * @author David Kaspar
 */
public final class PanAction extends WidgetAction.LockedAdapter {

    private Scene scene;
    private JScrollPane scrollPane;
    private Point lastLocation;

    protected boolean isLocked () {
        return scrollPane != null;
    }

    public State mousePressed (Widget widget, WidgetMouseEvent event) {
        if (event.getButton () == MouseEvent.BUTTON2) {
            scene = widget.getScene ();
            scrollPane = findScrollPane (scene.getView ());
            if (scrollPane != null) {
                lastLocation = scene.convertSceneToView (widget.convertLocalToScene (event.getPoint ()));
                SwingUtilities.convertPointToScreen (lastLocation, scene.getView ());
                return State.createLocked (widget, this);
            }
        }
        return State.REJECTED;
    }

    private JScrollPane findScrollPane (JComponent component) {
        for (;;) {
            if (component == null)
                return null;
            if (component instanceof JScrollPane)
                return ((JScrollPane) component);
            Container parent = component.getParent ();
            if (! (parent instanceof JComponent))
                return null;
            component = (JComponent) parent;
        }
    }

    public State mouseReleased (Widget widget, WidgetMouseEvent event) {
        boolean state = pan (widget, event.getPoint ());
        if (state)
            scrollPane = null;
        return state ? State.createLocked (widget, this) : State.REJECTED;
    }

    public State mouseDragged (Widget widget, WidgetMouseEvent event) {
        return pan (widget, event.getPoint ()) ? State.createLocked (widget, this) : State.REJECTED;
    }

    private boolean pan (Widget widget, Point newLocation) {
        if (scrollPane == null  ||  scene != widget.getScene ())
            return false;
        newLocation = scene.convertSceneToView (widget.convertLocalToScene (newLocation));
        SwingUtilities.convertPointToScreen (newLocation, scene.getView ());
        JComponent view = scene.getView ();
        Rectangle rectangle = view.getVisibleRect ();
        rectangle.x += lastLocation.x - newLocation.x;
        rectangle.y += lastLocation.y - newLocation.y;
        view.scrollRectToVisible (rectangle);
        lastLocation = newLocation;
        return true;
    }

}
