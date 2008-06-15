
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
 * SVGButton.java
 * 
 * Created on Oct 4, 2007, 1:56:11 PM
 */
package org.netbeans.microedition.svg;

import org.netbeans.microedition.svg.input.InputHandler;
import org.w3c.dom.svg.SVGAnimationElement;

/**
 *
 * @author Pavel Benes
 */
public abstract class SVGAbstractButton extends SVGComponent {
    protected static final String BUTTONPRESSED_ELEM_SUFFIX  = "_pressed";
    protected static final String BUTTONRELEASED_ELEM_SUFFIX = "_released";
    
    protected final SVGAnimationElement pressedAnimation;
    protected final SVGAnimationElement releasedAnimation;
    
    public SVGAbstractButton( SVGForm form, String elemId) {
        super(form, elemId);
        pressedAnimation = (SVGAnimationElement) getElementById(wrapperElement, elemId + BUTTONPRESSED_ELEM_SUFFIX);
        releasedAnimation = (SVGAnimationElement) getElementById(wrapperElement, elemId + BUTTONRELEASED_ELEM_SUFFIX);
    }
    
    public InputHandler getInputHandler() {
        return InputHandler.BUTTON_INPUT_HANDLER;
    }
    
    public void pressButton() { 
        if (pressedAnimation != null) {
            pressedAnimation.beginElementAt(0);
        }
        form.activate(this);
    }
    
    public void releaseButton() {
        if (releasedAnimation != null) {
            releasedAnimation.beginElementAt(0);
        }
    }

    public abstract boolean isSelected();
    
    public abstract void setSelected( boolean isSelected);
}
