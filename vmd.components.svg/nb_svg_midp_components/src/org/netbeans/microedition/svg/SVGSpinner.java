/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.microedition.svg;


import org.netbeans.microedition.svg.input.InputHandler;
import org.netbeans.microedition.svg.input.NumPadInputHandler;
import org.w3c.dom.svg.SVGAnimationElement;
import org.w3c.dom.svg.SVGElement;


/**
 * @author ads
 *
 */
public class SVGSpinner extends SVGComponent {
    
    private final String UP_SUFFIX = "_up";
    private final String DOWN_SUFFIX = "_down";
    
    private final String PRESSED_ELEM_SUFFIX = "_pressed";
    private final String RELEASED_ELEM_SUFFIX = "_released"; 
    
    private final String EDITOR_SUFFIX = "_editor";

    public SVGSpinner( SVGForm form, String elemId ) {
        super(form, elemId);
        myUpButton = (SVGElement) getElementById( wrapperElement, elemId + UP_SUFFIX);
        myDownButton = (SVGElement) getElementById( wrapperElement, elemId + DOWN_SUFFIX);
        
        myUpPressedAnimation = (SVGAnimationElement) getElementById(myUpButton, 
                myUpButton.getId() + PRESSED_ELEM_SUFFIX);
        myUpReleasedAnimation = (SVGAnimationElement) getElementById(myUpButton, 
                myUpButton.getId() + RELEASED_ELEM_SUFFIX);
        
        myDownPressedAnimation = (SVGAnimationElement) getElementById(myDownButton, 
                myDownButton.getId() + PRESSED_ELEM_SUFFIX);
        myDownReleasedAnimation = (SVGAnimationElement) getElementById(myDownButton, 
                myDownButton.getId() + RELEASED_ELEM_SUFFIX);
        
        myInputHandler = new SpinnerInputHandler();

        setModel( new DefaultModel() );
        setEditor( new DefaultSpinnerEditor( form , elemId + EDITOR_SUFFIX ) ); 
    }
    
    public void focusGained() {
        super.focusGained();
        getEditor().focusGained();
    }
    
    public void focusLost() {
        super.focusLost();
        getEditor().focusLost();
    }
    
    public void setModel( SVGSpinnerModel model ){
        myModel = model;
    }
    
    public SVGSpinnerModel getModel( ){
        return myModel;
    }
    
    public Object getValue(){
        return myModel.getValue();
    }
    
    public void setEditor( SVGComponent editor ){
        myEditor = editor;
    }
    
    public SVGComponent getEditor(){
        return myEditor;
    }
    
    public void pressUpButton() { 
        if (myUpPressedAnimation != null) {
            myUpPressedAnimation.beginElementAt(0);
        }
        form.activate(this);
    }
    
    public void releaseUpButton() {
        if (myUpReleasedAnimation != null) {
            myUpReleasedAnimation.beginElementAt(0);
        }
        getModel().setValue( getModel().getNextValue() );
    }
    
    public void pressDownButton() { 
        if (myDownPressedAnimation != null) {
            myDownPressedAnimation.beginElementAt(0);
        }
        form.activate(this);
    }
    
    public void releaseDownButton() {
        if (myDownReleasedAnimation != null) {
            myDownReleasedAnimation.beginElementAt(0);
        }
        getModel().setValue( getModel().getPreviousValue() );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.microedition.svg.SVGComponent#getInputHandler()
     */
    public InputHandler getInputHandler() {
        return myInputHandler;
    }
    
    public static interface SVGSpinnerModel {
        Object getValue();
        Object getNextValue();
        Object getPreviousValue();
        void setValue( Object value );
    }
    
    private class SpinnerInputHandler extends NumPadInputHandler {

        public SpinnerInputHandler( ) {
            super( form.getDisplay() );
        }

        public boolean handleKeyPress( SVGComponent comp, int keyCode ) {
            boolean ret = false;
            if ( comp instanceof SVGSpinner ){
                if ( keyCode == LEFT ){
                    pressDownButton();
                    ret = true;
                }
                else if ( keyCode == RIGHT ){
                    pressUpButton();
                    ret = true;
                }
                else {
                    return getEditor().getInputHandler().handleKeyPress(getEditor(), 
                            keyCode);
                }
            }
            return ret;
        }

        public boolean handleKeyRelease( SVGComponent comp, int keyCode ) {
            boolean ret = false;
            if ( comp instanceof SVGSpinner ){
                if ( keyCode == LEFT ){
                    releaseDownButton();
                    fireActionPerformed();
                    ret = true;
                }
                else if ( keyCode == RIGHT ){
                    releaseUpButton();
                    fireActionPerformed();
                    ret = true;
                }
                else {
                    return getEditor().getInputHandler().handleKeyRelease(getEditor(), 
                            keyCode);
                }
            }
            return ret;
        }
        
    }
    
    private class DefaultSpinnerEditor extends SVGTextField {

        public DefaultSpinnerEditor( SVGForm form , String id ) {
            super(form , id );
            SVGSpinner.this.addActionListener( new SVGActionListener (){

                public void actionPerformed( SVGComponent comp ) {
                    if( comp == SVGSpinner.this){
                        if ( getValue() != null ){
                            setText( getValue().toString() );
                        }
                    }
                }
            });
        }
        
    }
    
    private class DefaultModel implements SVGSpinnerModel {

        public Object getNextValue() {
            return new Integer( myValue.intValue()+1 );
        }

        public Object getPreviousValue() {
            return new Integer( myValue.intValue()-1 );
        }

        public Object getValue() {
            return myValue;
        }

        public void setValue( Object value ) {
            if ( ! (value instanceof Integer) ){
                throw new IllegalArgumentException( value +" is not " +
                		"allowed argument");
            }
            myValue = (Integer) value;
        }
        
        private Integer myValue = new Integer(0);
    }
        

    private InputHandler myInputHandler;
    
    private final SVGElement myUpButton;
    private final SVGElement myDownButton;
    
    private SVGComponent myEditor;
    
    private SVGSpinnerModel myModel;
    
    private final SVGAnimationElement myUpPressedAnimation;
    private final SVGAnimationElement myUpReleasedAnimation;
    private final SVGAnimationElement myDownPressedAnimation;
    private final SVGAnimationElement myDownReleasedAnimation;
}
