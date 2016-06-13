
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2008, 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
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
        

        public void handlePointerPress( PointerEvent event )
        {
            ((SVGAbstractButton)event.getComponent()).pressButton();
            super.handlePointerPress(event);
        }
        
        public void handlePointerRelease( PointerEvent event )
        {
            ((SVGAbstractButton)event.getComponent()).releaseButton();
            super.handlePointerRelease(event);
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
     * Coordinates are determined via bounding rectangle for component
     * ( method is called when bounding rectangle contains point).
     * These coordinates <code>x</code> and <code>y</code> are absolute screen
     * coordinates. They are not translated into "component" coordinates.
     *  Method implementation could decide how this 
     * event should be handled based on underlying SVG element 
     * ( f.e. ellipse should not react on corner coordinates of bounding rectangle).    
     * Default implementation send event to all listeners. 
     * @param event pointer event 
     */
    public void handlePointerPress(PointerEvent event){
        PointerListener[] listeners = event.getComponent().getPointerListeners();
        for (int i = 0; i < listeners.length; i++) {
            listeners[i].pointerPressed(event);
        }
    }
    
    /**
     * Invoked when the pointer device (if any), is released over the SVG 
     * component <code>comp</code>.
     * Coordinates are determined via bounding rectangle for component
     * ( method is called when bounding rectangle contains point).
     * These coordinates <code>x</code> and <code>y</code> are absolute screen
     * coordinates. They are not translated into "component" coordinates.
     *  Method implementation could decide how this 
     * event should be handled based on underlying SVG element 
     * ( f.e. ellipse should not react on corner coordinates of bounding rectangle).    
     * Default implementation send event to all listeners. 
     * @param event pointer event 
     */
    public void handlePointerRelease(PointerEvent event){
        PointerListener[] listeners = event.getComponent().getPointerListeners();
        for (int i = 0; i < listeners.length; i++) {
            listeners[i].pointerReleased(event);
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
