
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

        myText = (SVGLocatableElement) getElementById( getElement(),
                getElement().getId() + SVGTextField.TEXT_SUFFIX);
        if ( myText== null ){
            myText = (SVGLocatableElement) getNestedElementByMeta( getElement(),
                    TYPE, SVGTextField.TEXT );
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

    public String getText(){
        if ( myText != null ){
            return myText.getTrait( SVGTextField.TRAIT_TEXT );
        }
        return null;
    }

    public void setText(String text){
       if ( text == null ){
           text ="";
       }
       if ( myText != null ){
            setTraitSafely(myText, SVGTextField.TRAIT_TEXT, text.trim());
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
    private SVGLocatableElement myText;
    
    protected SVGAnimationElement myPressedAnimation;
    protected SVGAnimationElement myReleasedAnimation;
}
