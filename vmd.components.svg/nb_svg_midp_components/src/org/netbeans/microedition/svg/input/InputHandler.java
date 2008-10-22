
/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt. 
  * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */ 

/*
 * InputHandler.java
 * 
 * Created on Oct 2, 2007, 10:03:22 PM
 */

package org.netbeans.microedition.svg.input;

import java.util.Vector;
import org.netbeans.microedition.svg.SVGAbstractButton;
import org.netbeans.microedition.svg.SVGComponent;

/**
 *
 * @author Pavel Benes
 */
public abstract class InputHandler {
    public static final int UP        = -1;
    public static final int DOWN      = -2;
    public static final int LEFT      = -3;
    public static final int RIGHT     = -4;
    public static final int FIRE      = -5;
    public static final int BACKSPACE = -8;

    protected final Vector  caretListeners = new Vector(1);
    protected       Boolean prevVisibility = null;
    
    public static final InputHandler BUTTON_INPUT_HANDLER = new InputHandler() { 
        public boolean handleKeyPress(SVGComponent comp, int nKeyCode) {
            if ( comp instanceof SVGAbstractButton) {
                if ( nKeyCode == FIRE) {
                    ((SVGAbstractButton)comp).pressButton();
                    return true;
                }
            }
            return false;
        }
        public boolean handleKeyRelease(SVGComponent comp, int nKeyCode) {
            if ( comp instanceof SVGAbstractButton) {
                if ( nKeyCode == FIRE) {
                    ((SVGAbstractButton)comp).releaseButton();
                    return true;
                }
            }
            return false;
        }
        

        public void handlePointerPress( SVGComponent comp, int arg1, int arg2 )
        {
            ((SVGAbstractButton)comp).pressButton();
            super.handlePointerPress(comp, arg1, arg2);
        }
        
        public void handlePointerRelease( SVGComponent comp, int arg1, int arg2 )
        {
            ((SVGAbstractButton)comp).releaseButton();
            super.handlePointerPress(comp, arg1, arg2);
        }
    };
    
    public interface CaretVisibilityListener {
        void setCaretVisible(boolean isVisible);
    }
    
    public abstract boolean handleKeyPress(SVGComponent comp, int nKeyCode);
    public abstract boolean handleKeyRelease(SVGComponent comp, int nKeyCode);
    
    /**
     * Invoked when the pointer device (if any), is pressed over the SVG 
     * component <code>comp</code>.
     * Coordinates are determined via bounding rectangle for <code>comp</code>
     * ( method is called when bounding rectangle contains point).
     * These coordinates <code>x</code> and <code>y</code> are absolute screen
     * coordinates. They are not translated into "component" coordinates.
     *  Method implementation could decide how this 
     * event should be handled based on underlying SVG element 
     * ( f.e. ellipse should not react on corner coordinates of bounding rectangle).   
     * Default implementation send event to all listeners. 
     * @param comp SVG component 
     * @param x x-coordinate relative to bounding rectangle
     * @param y y-coordinate relative to bounding rectangle
     */
    public void handlePointerPress(SVGComponent comp, int x, int y){
        PointerListener[] listeners = comp.getPointerListeners();
        for (int i = 0; i < listeners.length; i++) {
            listeners[i].pointerPressed(x, y);
        }
    }
    
    /**
     * Invoked when the pointer device (if any), is released over the SVG 
     * component <code>comp</code>.
     * Coordinates are determined via bounding rectangle for <code>comp</code>
     * ( method is called when bounding rectangle contains point).
     * These coordinates <code>x</code> and <code>y</code> are absolute screen
     * coordinates. They are not translated into "component" coordinates.
     *  Method implementation could decide how this 
     * event should be handled based on underlying SVG element 
     * ( f.e. ellipse should not react on corner coordinates of bounding rectangle).    
     * Default implementation send event to all listeners. 
     * @param comp SVG component 
     * @param x x-coordinate relative to bounding rectangle
     * @param y y-coordinate relative to bounding rectangle
     */
    public void handlePointerRelease(SVGComponent comp, int x, int y){
        PointerListener[] listeners = comp.getPointerListeners();
        for (int i = 0; i < listeners.length; i++) {
            listeners[i].pointerReleased(x, y);
        }
    }
    
    public void addVisibilityListener( CaretVisibilityListener listener) {
        caretListeners.addElement( listener);
    }

    public void removeVisibilityListener( CaretVisibilityListener listener) {
        caretListeners.removeElement( listener);
    }
    
    protected void fireCaretVisibilityChanged(boolean isVisible) {
        if (prevVisibility == null || prevVisibility.booleanValue() != isVisible) {
            int listenerNum = caretListeners.size();

            for (int i = 0; i < listenerNum; i++) {
                ((CaretVisibilityListener) caretListeners.elementAt(i)).setCaretVisible(isVisible);
            }
            prevVisibility = new Boolean(isVisible);
        }
    }
}
