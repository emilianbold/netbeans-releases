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

package org.netbeans.core.windows.view.ui.slides;

import java.awt.Component;
import java.awt.Rectangle;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.core.windows.Constants;
import org.netbeans.swing.tabcontrol.SlideBarDataModel;


/**
 * Basic implementation of known types of SlideOperation.
 *
 * Isn't intended to be used directly, but through SlideOperationFactory.
 *
 * @author Dafe Simonek
 */
class SlideOperationImpl implements SlideOperation, ChangeListener {

    /** Type of slide operation */
    private final int type;
    /** Overall component that will be sliden, in winsys top component
     * surrounded by titlebar and border envelope */
    private final Component component;
    /** Slide effect */
    private final SlidingFx effect;
    /** true when component should be activated after slide */
    private final boolean requestsActivation;
    /** Desktop side where slide operation happens */
    private final String side;
    /** Bounds from where should effect start */    
    protected Rectangle startBounds;
    /** Bounds into which should effect finish */
    protected Rectangle finishBounds;
    /** Pane on which operation should take effect */
    private JLayeredPane pane;
    /** layer of layered pane to draw into */
    private Integer layer;
    
    /** Creates a new instance of SlideInOperation */
    SlideOperationImpl(int type, Component component, int orientation, 
         SlidingFx effect, boolean requestsActivation) {
        this(type, component, orientation2Side(orientation), effect, requestsActivation);
    }
    
    SlideOperationImpl(int type, Component component, String side, 
         SlidingFx effect, boolean requestsActivation) {
        this.type = type; 
        this.component = component;
        this.effect = effect;
        this.requestsActivation = requestsActivation;
        this.side = side;
    }

    public void run(JLayeredPane pane, Integer layer) {
        if (effect != null && effect.shouldOperationWait()) {
            // OK, effect is asynchronous and we should wait for effect finish,
            // so register and wait for stateChanged notification
            this.pane = pane;
            this.layer = layer;
            effect.setFinishListener(this);
            effect.showEffect(pane, layer, this);
        } else {
            if (effect != null) {
                effect.showEffect(pane, layer, this);
            }
            performOperation(pane, layer);
        }
    }

    /** Notification of effect finish is delivered here. Invokes operation */
    public void stateChanged(ChangeEvent e) {
        performOperation(pane, layer);
        pane = null;
        layer = null;
    }
    
    private void performOperation(JLayeredPane pane, Integer layer) {
        // XXX - TBD
        switch (type) {
            case SLIDE_IN:
                component.setBounds(finishBounds);
                pane.add(component, layer);
                break;
            case SLIDE_OUT:
                pane.remove(component);
                break;
            case SLIDE_RESIZE:
                component.setBounds(finishBounds);
                ((JComponent)component).revalidate();
                break;
        }
    }

    public void setFinishBounds(Rectangle bounds) {
        this.finishBounds = bounds;
    }

    public void setStartBounds(Rectangle bounds) {
        this.startBounds = bounds;
    }

    public String getSide() {
        return side;
    }

    public Component getComponent() {
        return component;
    }

    public Rectangle getFinishBounds() {
        return finishBounds;
    }

    public Rectangle getStartBounds() {
        return startBounds;
    }

    public boolean requestsActivation() {
        return requestsActivation;
    }

    protected static String orientation2Side (int orientation) {
        String side = Constants.LEFT; 
        if (orientation == SlideBarDataModel.WEST) {
            side = Constants.LEFT;
        } else if (orientation == SlideBarDataModel.EAST) {
            side = Constants.RIGHT;
        } else if (orientation == SlideBarDataModel.SOUTH) {
            side = Constants.BOTTOM;
        }
        return side;
    }

    public int getType () {
        return type;
    }

    public void prepareEffect() {
        if (effect != null) {
            effect.prepareEffect(this);
        }
    }        

    static int side2Orientation(String side) {
        int orientation = SlideBarDataModel.WEST; 
        if (Constants.LEFT.equals(side)) {
            orientation = SlideBarDataModel.WEST;
        } else if (Constants.RIGHT.equals(side)) {
            orientation = SlideBarDataModel.EAST;
        } else if (Constants.BOTTOM.equals(side)) {
            orientation = SlideBarDataModel.SOUTH;
        }
        return orientation;
    }
}
