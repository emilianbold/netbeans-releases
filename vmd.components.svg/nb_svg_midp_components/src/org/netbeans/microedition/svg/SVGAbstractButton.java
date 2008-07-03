
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
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGLocatableElement;

/**
 * 
 * @author Pavel Benes
 * @author ads 
 */
public abstract class SVGAbstractButton extends SVGComponent {
    
    protected static final String PRESSED  = "pressed";         // NOI18N
    protected static final String RELEASED = "released";        // NOI18N
    private static final String   BODY     = "body";            // NOI18N
    
    protected SVGAnimationElement pressedAnimation;
    protected SVGAnimationElement releasedAnimation;
    
    public SVGAbstractButton( SVGForm form, String elemId) {
        super(form, elemId);
        myBodyElement = (SVGLocatableElement) getNestedElementByMeta( getElement(), 
                TYPE, BODY );
        initAnimation();
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
    
    protected SVGLocatableElement getBodyElement(){
        return myBodyElement;
    }
    
    private void initAnimation() {
        if ( myBodyElement != null ){
            int count = 0;
            SVGElement animation = (SVGElement)
                myBodyElement.getFirstElementChild();
            while ( animation != null ){
                animation = (SVGElement)animation.getNextElementSibling();
                if ( count == 1 ){
                    pressedAnimation = (SVGAnimationElement)animation;
                }
                else if ( count ==2 ){
                    releasedAnimation = (SVGAnimationElement)animation;
                }
                count++;
            }
        }
        else {
            pressedAnimation = null;
            releasedAnimation = null;
        }
        
    }
    
    private SVGLocatableElement myBodyElement;
}
