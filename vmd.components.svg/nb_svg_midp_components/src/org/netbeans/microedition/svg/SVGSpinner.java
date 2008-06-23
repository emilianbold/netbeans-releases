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
import org.w3c.dom.svg.SVGLocatableElement;


/**
 * Suggested svg snippet:
 * <pre>
 * &lt;g id="age_spinner"  transform="..." ...>
 *       &lt;rect x="0" y="-5" rx="5" ry="5" width="44" height="30" fill="none" 
 *       stroke="rgb(255,165,0)" stroke-width="2" visibility="hidden">
 *           &lt;set attributeName="visibility" attributeType="XML" begin="age_spinner.focusin" fill="freeze" to="visible"/>
 *           &lt;set attributeName="visibility" attributeType="XML" begin="age_spinner.focusout" fill="freeze" to="hidden"/>
 *       &lt;/rect>
 *       &lt;rect  x="5.0" y="0.0" width="33" height="20" fill="none" stroke="black" stroke-width="2"/>
 *   &lt;g id="age_spinner_editor">
 *       &lt;!-- this editor is SVGTextField component -->
 *
 *       &lt;!-- metadata definition-->
 *       &lt;text display="none">readOnly="false" enabled="true"</text>
 *       &lt;text id="age_spinner_editor_text" x="10" y="15" stroke="black" font-size="15" font-family="SunSansSemiBold">0</text>
 *       &lt;rect id="age_spinner_editor_caret" visibility="visible" x="17" y="3" width="2" height="15" fill="black" stroke="black"/>
 *       &lt;!-- The rectangle below is difference between rectangle that bounds spinner and spinner buttons ( the latter 
 *       has id = age_spinner_up and age_spinner_down ). It needed for counting bounds of input text area .
 *       It should be created programatically or SVGTextField should have API for dealing with "width"
 *       of editor not based only on width of text field component.-->
 *       &lt;rect visibility="hidden" x="5.0" y="0" width="33" height="20" />
 *   &lt;/g>
 *   &lt;rect id="age_spinner_up" x="21.0" y="0.0" width="16" height="10" fill="rgb(220,220,220)" stroke="black" stroke-width="1.5">
 *           &lt;animate id="age_spinner_up_pressed" attributeName="fill" attributeType="XML" begin="indefinite" dur="0.25s" fill="freeze" to="rgb(170,170,170)"/>
 *           &lt;animate id="age_spinner_up_released" attributeName="fill" attributeType="XML" begin="indefinite" dur="0.25s" fill="freeze" to="rgb(220,220,220)"/>
 *   &lt;/rect>
 *       &lt;rect id="age_spinner_down" x="21.0" y="10.0" width="16" height="10" fill="rgb(220,220,220)" stroke="black" stroke-width="1.5">
 *           &lt;animate id="age_spinner_down_pressed" attributeName="fill" attributeType="XML" begin="indefinite" dur="0.25s" fill="freeze" to="rgb(170,170,170)"/>
 *           &lt;animate id="age_spinner_down_released" attributeName="fill" attributeType="XML" begin="indefinite" dur="0.25s" fill="freeze" to="rgb(220,220,220)"/>
 *   <&lt;rect>
 *   &lt;polygon transform="translate(28,6)"  points="0,0 2,0 1,-2" fill="blue" stroke="black" stroke-width="2"/>
 *   &lt;polygon transform="translate(28,14)"  points="0,0 2,0 1,2" fill="blue" stroke="black" stroke-width="2"/>
 *   </g>
 * </pre>
 * @author ads
 *
 */
public class SVGSpinner extends SVGComponent {
    
    private static final String UP              = "up_button";      // NOI18N
    private static final String DOWN            = "down_button";    // NOI18N
    private static final String PRESSED         = "pressed";        // NOI18N
    private static final String RELEASED        = "released";       // NOI18N
    private static final String EDITOR          = "editor";         // NOI18N
    
    public SVGSpinner( SVGForm form, String elemId ) {
        super(form, elemId);
        myUpButton = getElementByMeta( getElement(), TYPE, UP);
        myDownButton = getElementByMeta( getElement(), TYPE , DOWN);
        
        myUpPressedAnimation = (SVGAnimationElement) getElementByMeta(myUpButton, 
                TYPE , PRESSED );
        myUpReleasedAnimation = (SVGAnimationElement) getElementByMeta(myUpButton, 
                TYPE, RELEASED);
        
        myDownPressedAnimation = (SVGAnimationElement) getElementByMeta(myDownButton, 
                TYPE , PRESSED);
        myDownReleasedAnimation = (SVGAnimationElement) getElementByMeta(myDownButton, 
                TYPE, RELEASED );
        
        myInputHandler = new SpinnerInputHandler();

        setModel( new DefaultModel() );
        setEditor( new DefaultSpinnerEditor( form , 
                (SVGLocatableElement)getElementByMeta( getElement(), TYPE, 
                        EDITOR) ) ); 
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

        public DefaultSpinnerEditor( SVGForm form , SVGLocatableElement element  ) {
            super(form , element );
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
