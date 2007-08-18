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
package org.netbeans.modules.web.jsf.navigation.graph.actions;

import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.Widget;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author David Kaspar
 */
public class MyActionMapAction extends WidgetAction.Adapter {

    private final InputMap inputMap;
    private final ActionMap actionMap;

    public MyActionMapAction (InputMap inputMap, ActionMap actionMap) {
        super();
        this.inputMap = inputMap;
        this.actionMap = actionMap;
        //System.out.println("Creating My Action Map");
        //printKeys();
    }
    
    private void printKeys() {
        System.out.println("Keys");
        for( KeyStroke keyStroke : inputMap.keys()){
            System.out.println(keyStroke.toString());
        }
        Thread.dumpStack();
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
            Object o = inputMap.get (keyStroke);
            action = o != null ? actionMap.get (o) : null;
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

    /*
    @Override
    protected void finalize() throws Throwable {
        System.out.println("Destroy MyActionMapAction");
        printKeys();
        super.finalize();
    }*/
    
    


}
