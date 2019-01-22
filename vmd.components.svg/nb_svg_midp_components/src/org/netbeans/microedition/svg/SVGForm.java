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
 * SVGForm.java
 *
 * Created on Oct 2, 2007, 3:09:21 PM
 */
package org.netbeans.microedition.svg;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.Display;
import javax.microedition.m2g.SVGEventListener;
import javax.microedition.m2g.SVGImage;

import org.netbeans.microedition.svg.input.InputHandler;
import org.netbeans.microedition.svg.input.NumPadInputHandler;
import org.netbeans.microedition.svg.input.PointerEvent;

/**
 *
 * 
 */
public class SVGForm extends SVGPlayer implements InputHandler.CaretVisibilityListener {
    private final Vector             components;
    private       SVGComponent       focusedComponent = null;
    private       NumPadInputHandler inputHandler;

    public interface FocusListener {
        void focusGained();
        void focusLost();
    }
    
    public SVGForm(SVGImage svgImage, Display display) throws IllegalArgumentException {
        super( svgImage, display);
        components = new Vector();
        //this.display = display;
        setResetAnimationWhenStopped(false); // animation cannot be reset automatically        
        setSVGEventListener(new SvgFormEventListener()); // set menu listener        
        setFullScreenMode(true); // menu is usually full screen
    }
    
    public synchronized void add(SVGComponent component ){
        components.addElement( component );
        if ( getFocusedField()==null && component.isFocusable() ){
            component.requestFocus();
        }
    }
         
    public synchronized SVGComponent getFocusedField() {
        return focusedComponent;
    }    
    
    synchronized void requestFocus(SVGComponent comp) {
        if ( focusedComponent != comp) {
            if ( focusedComponent != null) {
                focusedComponent.focusLost();
            }
            focusedComponent = comp;
            if ( focusedComponent != null) {
                getSVGImage().focusOn(focusedComponent.getElement());
                focusedComponent.focusGained();
            } else {
                getSVGImage().focusOn(null);
            }
        }
    }

    public void activate(SVGComponent comp) {
        requestFocus(comp);
        getSVGImage().activate();
    }
    
    public void setCaretVisible(boolean isVisible) {
        SVGComponent component = focusedComponent;
        if (component instanceof SVGTextField  ) {
            ((SVGTextField)component).setCaretVisible(isVisible);
        }
        if ( component instanceof SVGTextArea ){
            ((SVGTextArea)component).setCaretVisible(isVisible);
        }
    }
    
    public synchronized NumPadInputHandler getNumPadInputHandler() {
        if ( inputHandler == null) {
            inputHandler = new NumPadInputHandler(getDisplay());
            inputHandler.addVisibilityListener(this);            
        }
        return inputHandler;
    }
    
    synchronized SVGLabel getLabelFor( SVGComponent component ){
        Enumeration en = components.elements();
        while ( en.hasMoreElements() ){
            SVGComponent comp = (SVGComponent)en.nextElement();
            if ( comp instanceof SVGLabel ){
                SVGLabel label = (SVGLabel) comp;
                SVGComponent labelFor = label.getLabelFor();
                if ( labelFor == component ){
                    return label;
                }
            }
        }
        return null;
    }
    
    private class SvgFormEventListener implements SVGEventListener {
        
        /**
         * Value in milliseconds that is used for determination
         * event with multiple click count.
         * If two clicks on the same point was less then 
         * <code>MILLIS_ON_CLICK</code> seconds then 
         * there will be generated PointerEvent with incremented 
         * click count.
         * Otherwise there will be just separate PointerEvents with
         * click count equals to 1. 
         */
        private int MILLIS_ON_CLICK = 300;
        
        public void keyPressed( int keyCode ) {
            synchronized (SVGForm.this) {
                if (focusedComponent != null) {
                    int index;
                    switch (keyCode) {
                        case InputHandler.UP:
                            SVGComponent next = null;
                            index = components.indexOf(focusedComponent);
                            while (next != focusedComponent) {
                                if (--index < 0) {
                                    index = components.size() - 1;
                                }
                                next = (SVGComponent) components
                                        .elementAt(index);
                                if (next.isFocusable()) {
                                    requestFocus(next);
                                    break;
                                }
                            }
                            break;
                        case InputHandler.DOWN:
                            next = null;
                            index = components.indexOf(focusedComponent);
                            while (next != focusedComponent) {
                                if (++index >= components.size()) {
                                    index = 0;
                                }
                                next = (SVGComponent) components
                                        .elementAt(index);
                                if (next.isFocusable()) {
                                    requestFocus(next);
                                }
                            }
                            break;
                        default:
                            InputHandler handler = focusedComponent
                                    .getInputHandler();
                            if (handler != null) {
                                handler.handleKeyPress(focusedComponent,
                                        keyCode);
                            }
                            break;
                    }
                }
            }
        }
        
        public void keyReleased( int keyCode ) {
            synchronized (SVGForm.this) {
                if (focusedComponent != null) {
                    InputHandler handler = focusedComponent.getInputHandler();
                    if (handler != null) {
                        handler.handleKeyRelease(focusedComponent, keyCode);
                    }
                }
            }
        }
        
        public void pointerPressed(int x, int y) {
            long currentTime = System.currentTimeMillis();
            if ( myLastEvent != null && x == myLastEvent.getX() 
                    && y == myLastEvent.getY() && 
                    ( currentTime - myLastEvent.getWhen()) <= MILLIS_ON_CLICK)
            {
                SVGComponent component = myLastEvent.getComponent();
                if ( component.getInputHandler() != null ){
                    component.getInputHandler().handlePointerPress( 
                        new PointerEvent(component, x, y, 
                                myLastEvent.getClickCount() +1));
                }
                return;
            }
            else {
                myLastEvent = null;
            }
            synchronized(SVGForm.this){
                Enumeration en = components.elements();
                while ( en.hasMoreElements() ){
                    SVGComponent next = (SVGComponent)en.nextElement();
                    SVGRectangle rectangle = next.getBounds();
                    if ( rectangle != null && rectangle.contains(x, y)
                            && next.getInputHandler()!= null )
                    {
                        next.getInputHandler().handlePointerPress( 
                                new PointerEvent(next, x, y));
                        return;
                    }
                }
            }
        }
        
        public void pointerReleased(int x, int y) {
            long currentTime = System.currentTimeMillis();
            if ( myLastEvent != null && x == myLastEvent.getX() 
                    && y == myLastEvent.getY() && 
                    ( currentTime - myLastEvent.getWhen()) <= MILLIS_ON_CLICK)
            {
                SVGComponent component = myLastEvent.getComponent();
                myLastEvent = new PointerEvent(component, x, y, 
                        myLastEvent.getClickCount() +1);
                if ( component.getInputHandler() != null ){
                    component.getInputHandler().handlePointerRelease( myLastEvent );
                }
                return;
            }
            else {
                myLastEvent = null;
            }
            synchronized(SVGForm.this){
                Enumeration en = components.elements();
                while ( en.hasMoreElements() ){
                    SVGComponent next = (SVGComponent)en.nextElement();
                    SVGRectangle rectangle = next.getBounds();
                    if ( rectangle != null && rectangle.contains(x, y)
                            && next.getInputHandler()!= null)
                    {
                        myLastEvent = new PointerEvent(next, x, y);
                        next.getInputHandler().handlePointerRelease( 
                                myLastEvent);
                        return;
                    }
                }
            }
        }
        
        public void hideNotify() {
        }
        
        public void showNotify() {
        }
        
        public void sizeChanged(int width, int height) {
        }
        
        private PointerEvent myLastEvent;
    }

}
