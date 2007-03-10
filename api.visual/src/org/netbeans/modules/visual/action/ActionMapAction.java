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

import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author David Kaspar
 */
public class ActionMapAction extends WidgetAction.Adapter {

    private InputMap inputMap;
    private ActionMap actionMap;

    public ActionMapAction (InputMap inputMap, ActionMap actionMap) {
        this.inputMap = inputMap;
        this.actionMap = actionMap;
    }

    public State keyPressed (Widget widget, WidgetKeyEvent event) {
        return handleKeyEvent (widget, event, KeyStroke.getKeyStroke (event.getKeyCode (), event.getModifiers ()));
    }

    public State keyReleased (Widget widget, WidgetKeyEvent event) {
        return handleKeyEvent (widget, event, KeyStroke.getKeyStroke (event.getKeyCode (), event.getModifiers (), true));
    }

    public State keyTyped (Widget widget, WidgetKeyEvent event) {
        return handleKeyEvent (widget, event, KeyStroke.getKeyStroke (event.getKeyCode (), event.getModifiers ()));
    }

    private State handleKeyEvent (Widget widget, WidgetKeyEvent event, KeyStroke keyStroke) {
        ActionListener action;
        if (actionMap != null && inputMap != null) {
            action = actionMap.get (inputMap.get (keyStroke));
        } else {
            JComponent view = widget.getScene ().getView ();
            action = view != null ? view.getActionForKeyStroke (keyStroke) : null;
        }
        if (action != null) {
            action.actionPerformed (new ActionEvent (widget, (int) event.getEventID (), null, event.getWhen (), event.getModifiers ())); // TODO - action-event command
            return State.CONSUMED;
        }
        return State.REJECTED;
    }

    public State mousePressed (Widget widget, WidgetMouseEvent event) {
        return handleMouseEvent (widget, event);
    }

    public State mouseReleased (Widget widget, WidgetMouseEvent event) {
        return handleMouseEvent (widget, event);
    }

    private State handleMouseEvent (Widget widget, WidgetMouseEvent event) {
        if (event.isPopupTrigger ()) {
            JComponent view = widget.getScene ().getView ();
            if (view != null) {
                ActionMap map = actionMap != null ? actionMap : view.getActionMap ();
                Object[] objects = map.allKeys (); // HINT - the popup menu items order is defined by result of ActionMap.allKeys method
                if (objects != null) {
                    Action[] actions = new Action[objects.length];
                    for (int i = 0; i < objects.length; i ++)
                        actions[i] = map.get (objects[i]);
                    JPopupMenu popupMenu = Utilities.actionsToPopup (actions, view);
                    if (popupMenu != null) {
                        Scene scene = widget.getScene ();
                        Point point = scene.convertSceneToView (widget.convertLocalToScene (event.getPoint ()));
                        popupMenu.show (scene.getView (), point.x, point.y);
                    }
                }
                return State.CONSUMED;
            }
        }
        return State.REJECTED;
    }

}
