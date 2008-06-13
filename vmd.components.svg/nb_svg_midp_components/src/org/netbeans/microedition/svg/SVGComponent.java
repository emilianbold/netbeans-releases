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
 * SVGComponent.java
 * 
 * Created on Oct 4, 2007, 1:35:53 PM
 */

package org.netbeans.microedition.svg;

import java.util.Vector;
import org.netbeans.microedition.svg.input.InputHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGLocatableElement;

/**
 *
 * @author Pavel Benes
 */
public abstract class SVGComponent implements SVGForm.FocusListener {
    public static final    String SVG_NS = "http://www.w3.org/2000/svg";
    
    protected static final String TRAIT_X          = "x";
    protected static final String TRAIT_Y          = "y";
    protected static final String TRAIT_VISIBILITY = "visibility";
    protected static final String TRAIT_FILL       = "fill";
    
    protected final String              elemId;
    protected final SVGForm             form;
    protected final SVGLocatableElement wrapperElement;
    protected       Vector              actionListeners;

    public SVGComponent( SVGForm form, String elemId) {
        this.elemId = elemId;
        this.form = form;
        Document doc = form.getDocument();
        wrapperElement = (SVGLocatableElement) doc.getElementById(elemId);
    }
    
    public SVGLocatableElement getElement() {
        return wrapperElement;
    }
    
    public SVGForm getForm(){
        return form;
    }
    
    public void requestFocus() {
        form.requestFocus(this);
    }

    public void focusGained() {
    }

    public void focusLost() {
    }   
    
    public InputHandler getInputHandler() {
        return null;
    }
    
    public synchronized void addActionListener(SVGActionListener listener) {
        if ( actionListeners == null) {
            actionListeners = new Vector(1);
        }
        actionListeners.addElement(listener);
    }
    
    public synchronized void removeActionListener(SVGActionListener listener) {
        if (actionListeners != null) {
            actionListeners.removeElement(listener);
            if ( actionListeners.isEmpty()) {
                actionListeners = null;
            }
        }
    }
    
    protected synchronized void fireActionPerformed() {
        if (actionListeners != null) {
            int listenersNum = actionListeners.size();
            for (int i = 0; i < listenersNum; i++) {
                ((SVGActionListener) actionListeners.elementAt(i)).actionPerformed(this);
            }
        }
    }

    protected static final SVGElement getElementById( SVGElement parent, String childId) {
        Element elem = parent.getFirstElementChild();
        while( elem != null && elem instanceof SVGElement) {
            SVGElement svgElem = (SVGElement) elem;
            if ( childId.equals( svgElem.getId())) {
                return svgElem;
            } else {
                SVGElement result = getElementById(svgElem, childId);
                if ( result != null) {
                    return result;
                }
            }
            elem = svgElem.getNextElementSibling();
        }
        
        return null;
    }   
}
