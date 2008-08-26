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

/**
 *
 * @author Pavel Benes
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
    
    public void add(SVGComponent component ){
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
        if (focusedComponent instanceof SVGTextField  ) {
            ((SVGTextField)focusedComponent).setCaretVisible(isVisible);
        }
        if ( focusedComponent instanceof SVGTextArea ){
            ((SVGTextArea)focusedComponent).setCaretVisible(isVisible);
        }
    }
    
    public synchronized NumPadInputHandler getNumPadInputHandler() {
        if ( inputHandler == null) {
            inputHandler = new NumPadInputHandler(getDisplay());
            inputHandler.addVisibilityListener(this);            
        }
        return inputHandler;
    }
    
    SVGLabel getLabelFor( SVGComponent component ){
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
        public void keyPressed(int keyCode) {
            if ( focusedComponent != null) {
                int index;
                switch( keyCode) {
                    case InputHandler.UP:
                        SVGComponent next = null;
                        index = components.indexOf(focusedComponent);
                        while (next != focusedComponent) {
                            if (--index < 0) {
                                index = components.size() - 1;
                            }
                            next = (SVGComponent) components.elementAt(index);
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
                            next = (SVGComponent) components.elementAt(index);
                            if (next.isFocusable()) {
                                requestFocus(next);
                            }
                        }
                        break;
                    default:
                        InputHandler handler = focusedComponent.getInputHandler();
                        if ( handler != null) {
                            handler.handleKeyPress( focusedComponent, keyCode);
                        }
                        break;
                }
            }
        }
        
        public void keyReleased(int keyCode) {
            if ( focusedComponent != null) {
                InputHandler handler = focusedComponent.getInputHandler();
                if ( handler != null) {
                    handler.handleKeyRelease( focusedComponent, keyCode);
                }
            }
        }
        
        public void pointerPressed(int x, int y) {
        }
        
        public void pointerReleased(int x, int y) {
        }
        
        public void hideNotify() {
        }
        
        public void showNotify() {
        }
        
        public void sizeChanged(int width, int height) {
        }
    }

}
