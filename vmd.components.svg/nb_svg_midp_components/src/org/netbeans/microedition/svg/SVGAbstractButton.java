
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
    
    protected static final String PRESSED       = "pressed";         // NOI18N
    protected static final String RELEASED      = "released";        // NOI18N
    
    protected static final String PRESSED_SUFFIX = DASH+PRESSED;     // NOI18N
    protected static final String RELEASED_SUFFIX= DASH +RELEASED;   // NOI18N
    
    private static final String   BODY           = "body";           // NOI18N
    private static final String   BODY_SUFFIX    = DASH+BODY;        // NOI18N
    
    public SVGAbstractButton( SVGForm form, String elemId) {
        super(form, elemId);
        
        myBodyElement = (SVGLocatableElement) getElementById( getElement(),
                getElement().getId() + BODY_SUFFIX);
        if ( myBodyElement == null ){
            myBodyElement = (SVGLocatableElement) getNestedElementByMeta( getElement(), 
                TYPE, BODY );
        }
        initAnimation();
    }

    public InputHandler getInputHandler() {
        return InputHandler.BUTTON_INPUT_HANDLER;
    }
    
    public void pressButton() { 
        if (getPressedAnimation() != null) {
            getForm().invokeLaterSafely( new Runnable(){
                public void run() {
                    getPressedAnimation().beginElementAt(0);
                }
            });
        }
        getForm().activate(this);
    }
    
    public void releaseButton() {
        if (getReleasedAnimation() != null) {
            getForm().invokeLaterSafely( new Runnable(){
                public void run() {
                    getReleasedAnimation().beginElementAt(0);
                }
            });
        }
    }

    public abstract boolean isSelected();
    
    public abstract void setSelected( boolean isSelected);
    
    protected SVGLocatableElement getBodyElement(){
        return myBodyElement;
    }
    
    private void initAnimation() {
        if ( getBodyElement() != null ){
            
            myPressedAnimation = (SVGAnimationElement) getElementById( 
                    getBodyElement(), getBodyElement().getId() +PRESSED_SUFFIX);
            myReleasedAnimation = (SVGAnimationElement) getElementById( 
                    getBodyElement(), getBodyElement().getId() +RELEASED_SUFFIX);
            
            if ( myPressedAnimation != null ){
                return;
            }
            
            int count = 0;
            SVGElement animation = (SVGElement)
                getBodyElement().getFirstElementChild();
            while ( animation != null ){
                animation = (SVGElement)animation.getNextElementSibling();
                if ( count == 1 ){
                    myPressedAnimation = (SVGAnimationElement)animation;
                }
                else if ( count ==2 ){
                    myReleasedAnimation = (SVGAnimationElement)animation;
                }
                count++;
            }
        }
        else {
            myPressedAnimation = null;
            myReleasedAnimation = null;
        }
        
    }
    
    private SVGAnimationElement getPressedAnimation(){
        return myPressedAnimation;
    }
    
    private SVGAnimationElement getReleasedAnimation(){
        return myReleasedAnimation;
    }
    
    private SVGLocatableElement myBodyElement;
    
    protected SVGAnimationElement myPressedAnimation;
    protected SVGAnimationElement myReleasedAnimation;
}
