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


import java.util.Enumeration;
import java.util.Vector;

import org.netbeans.microedition.svg.input.InputHandler;
import org.netbeans.microedition.svg.input.NumPadInputHandler;
import org.netbeans.microedition.svg.input.PointerEvent;
import org.w3c.dom.svg.SVGAnimationElement;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGLocatableElement;
import org.w3c.dom.svg.SVGRect;


/**
 * Suggested svg snippet:
 * <pre>
 * &lt;g id="age_spinner"  transform="translate(20,140)">
 *   &lt;!-- Metadata information. Please don't edit. -->
 *   &lt;text display="none">type=spinner&lt;/text>
 *
 *       &lt;rect x="0" y="-5" rx="5" ry="5" width="44" height="30" fill="none" stroke="rgb(255,165,0)" stroke-width="2" visibility="hidden">
 *           &lt;set attributeName="visibility" attributeType="XML" begin="age_spinner.focusin" fill="freeze" to="visible"/>
 *           &lt;set attributeName="visibility" attributeType="XML" begin="age_spinner.focusout" fill="freeze" to="hidden"/>
 *       &lt;/rect>
 *       &lt;rect  x="5.0" y="0.0" width="33" height="20" fill="none" stroke="black" stroke-width="2"/>
 *   &lt;g id="age_spinner_editor">
 *       &lt;!-- this editor is SVGTextField component -->
 *
 *       &lt;!-- metadata definition-->
 *       &lt;text display="none">type=editor&lt;/text>
 *       &lt;text display="none">editable=false&lt;/text>
 *       &lt;text display="none">enabled=true&lt;/text>
 *
 *       &lt;g>
 *           &lt;text id="age_spinner_editor_text" x="10" y="15" stroke="black" font-size="15" font-family="SunSansSemiBold">
 *           0&lt;/text>
 *           &lt;!-- Metadata information. Please don't edit. -->
 *           &lt;text display="none">type=text&lt;/text>
 *       &lt;/g>
 *       
 *       &lt;!-- The rectangle below is difference between rectangle that bounds spinner and spinner buttons ( the latter 
 *       has id = age_spinner_up and age_spinner_down ). It needed for counting bounds of input text area .
 *       It should be created programatically or SVGTextField should have API for dealing with "width"
 *       of editor not based only on width of text field component.-->
 *       &lt;rect visibility="hidden" x="5.0" y="0" width="33" height="20"/>
 *   &lt;/g>
 *   &lt;g>
 *       &lt;!-- Metadata information. Please don't edit. -->
 *       &lt;text display="none">type=up_button&lt;/text>
 *
 *       &lt;rect id="age_spinner_up_button" x="21.0" y="0.0" width="16" height="10" fill="rgb(220,220,220)" stroke="black" stroke-width="1.5">
 *           &lt;animate id="age_spinner_up_button_pressed" attributeName="fill" attributeType="XML" begin="indefinite" dur="0.25s" fill="freeze" to="rgb(170,170,170)"/>
 *           &lt;animate id="age_spinner_up_button_released" attributeName="fill" attributeType="XML" begin="indefinite" dur="0.25s" fill="freeze" to="rgb(220,220,220)"/>
 *       &lt;/rect>
 *   &lt;/g>
 *   &lt;g>
 *       &lt;!-- Metadata information. Please don't edit. -->
 *       &lt;text display="none">type=down_button&lt;/text>
 *
 *       &lt;rect id="age_spinner_down_button" x="21.0" y="10.0" width="16" height="10" fill="rgb(220,220,220)" stroke="black" stroke-width="1.5">
 *           &lt;animate id="age_spinner_down_button_pressed" attributeName="fill" attributeType="XML" begin="indefinite" dur="0.25s" fill="freeze" to="rgb(170,170,170)"/>
 *           &lt;animate id="age_spinner_down_button_released" attributeName="fill" attributeType="XML" begin="indefinite" dur="0.25s" fill="freeze" to="rgb(220,220,220)"/>
 *       &lt;/rect>
 *   &lt;/g>
 *   &lt;polygon transform="translate(28,6)"  points="0,0 2,0 1,-2" fill="blue" stroke="black" stroke-width="2"/>
 *   &lt;polygon transform="translate(28,14)"  points="0,0 2,0 1,2" fill="blue" stroke="black" stroke-width="2"/>
 *   &lt;/g>
 * </pre>
 * @author ads
 *
 */
public class SVGSpinner extends SVGComponent implements DataListener {
    
    private static final String UP              = "up_button";          // NOI18N
    private static final String DOWN            = "down_button";        // NOI18N
    private static final String EDITOR          = "editor";             // NOI18N
    
    private static final String UP_SUFFIX       = DASH+UP;
    private static final String DOWN_SUFFIX     = DASH+DOWN;
    private static final String EDITOR_SUFFIX   = DASH+EDITOR;
    
    private static final String PRESSED_SUFFIX =  DASH + "pressed";     // NOI18N
    private static final String RELEASED_SUFFIX  = DASH + "released";   // NOI18N
    
    public SVGSpinner( SVGForm form, String elemId ) {
        super(form, elemId);
        
        myUILock = new Object();
        
        initButtons();
        
        myInputHandler = new SpinnerInputHandler();

        setModel( new DefaultModel() );
        
        initEditor(); 
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
        if ( myModel != null ){
            myModel.removeDataListener( this );
        }
        myModel = model;
        myModel.addDataListener( this );
    }
    
    public SVGSpinnerModel getModel( ){
        return myModel;
    }
    
    public Object getValue(){
        return myModel.getValue();
    }
    
    public void setEditor( SVGComponent editor ){
        myEditor = editor;;
    }
    
    public SVGComponent getEditor(){
        return myEditor;
    }
    
    
    /* (non-Javadoc)
     * @see org.netbeans.microedition.svg.SVGComponent#getInputHandler()
     */
    public InputHandler getInputHandler() {
        return myInputHandler;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.microedition.svg.DataListener#contentsChanged(java.lang.Object)
     */
    public void contentsChanged( Object source ) {
        if ( source == getModel() ){
            synchronized (myUILock) {
                if ( !isUIAction ){
                    isInternallyEdited = true;
                    fireActionPerformed();
                    isInternallyEdited = false;
                }
            }
        }
    }
    

    private void initEditor() {
        SVGLocatableElement editor =null;
        if ( getElement().getId() != null ){
            editor = (SVGLocatableElement)getElementById( getElement(), 
                    getElement().getId()+EDITOR_SUFFIX);
        }
        if (editor == null) {
            editor = (SVGLocatableElement) getElementByMeta(getElement(), TYPE,
                    EDITOR);
        }
        if (editor != null) {
            setEditor(new DefaultSpinnerEditor(getForm(), editor, myUILock));
        }
    }
    
    private void initButtons() {
        if ( getElement().getId() != null ){
            myUpButton = getElementById( getElement(), getElement().getId() +
                    UP_SUFFIX);
            myDownButton = getElementById( getElement(), getElement().getId() +
                    DOWN_SUFFIX);
            
            myUpPressedAnimation = (SVGAnimationElement)getElementById( myUpButton,
                    myUpButton.getId() + PRESSED_SUFFIX );
            myUpReleasedAnimation = (SVGAnimationElement) getElementById( 
                    myUpButton, myUpButton.getId() + RELEASED_SUFFIX );
            
            myDownPressedAnimation = (SVGAnimationElement)getElementById( 
                    myDownButton, myDownButton.getId() + PRESSED_SUFFIX );
            myDownReleasedAnimation = (SVGAnimationElement)getElementById(
                    myDownButton , myDownButton.getId() + RELEASED_SUFFIX);
        }
        
        if ( myUpButton == null ) {
            myUpButton = getNestedElementByMeta( getElement(), TYPE, UP);
        }
        if ( myDownButton == null ){
            myDownButton = getNestedElementByMeta( getElement(), TYPE , DOWN);
        }
        
        if ( myUpPressedAnimation == null ){
            myUpPressedAnimation = 
                (SVGAnimationElement)myUpButton.getFirstElementChild();
        }
        if ( myDownPressedAnimation == null ){
            myDownPressedAnimation = (SVGAnimationElement) myDownButton.
                getFirstElementChild();
        }
        
        if ( myUpReleasedAnimation ==null && myUpPressedAnimation != null ){
            myUpReleasedAnimation = (SVGAnimationElement)myUpPressedAnimation.
                getNextElementSibling();
        }
        if ( myDownReleasedAnimation == null && myDownPressedAnimation != null ){
            myDownReleasedAnimation = (SVGAnimationElement)myDownPressedAnimation.
                getNextElementSibling();
        }
    }
    
    private void releaseUpButton() {
        getForm().invokeLaterSafely(new Runnable() {
            public void run() {
                if (myUpReleasedAnimation != null) {
                    myUpReleasedAnimation.beginElementAt(0);
                }
            }
        });
        getModel().setValue( getModel().getNextValue() );
    }
    
    private void pressDownButton() {
        getForm().invokeLaterSafely(new Runnable() {
            public void run() {
                if (myDownPressedAnimation != null) {
                    myDownPressedAnimation.beginElementAt(0);
                }
            }
        });
        form.activate(this);
    }
    
    private void pressUpButton() {
        getForm().invokeLaterSafely(new Runnable() {
            public void run() {
                if (myUpPressedAnimation != null) {
                    myUpPressedAnimation.beginElementAt(0);
                }
            }
        });
        form.activate(this);
    }
    
    private void releaseDownButton() {
        getForm().invokeLaterSafely(new Runnable() {
            public void run() {
                if (myDownReleasedAnimation != null) {
                    myDownReleasedAnimation.beginElementAt(0);
                }
            }
        });
        getModel().setValue( getModel().getPreviousValue() );
    }
    
    public static interface SVGSpinnerModel {
        Object getValue();
        Object getNextValue();
        Object getPreviousValue();
        void setValue( Object value );
        void addDataListener( DataListener listener );
        void removeDataListener( DataListener listener );
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
                    synchronized ( myUILock) {
                        isUIAction = true;
                        isInternallyEdited = true;
                        fireActionPerformed();
                        isUIAction = false;
                        isInternallyEdited = false;
                    }
                    ret = true;
                }
                else if ( keyCode == RIGHT ){
                    releaseUpButton();
                    synchronized ( myUILock) {
                        isUIAction = true;
                        isInternallyEdited = true;
                        fireActionPerformed();
                        isUIAction = false;
                        isInternallyEdited = false;
                    }
                    ret = true;
                }
                else {
                    return getEditor().getInputHandler().handleKeyRelease(getEditor(), 
                            keyCode);
                }
            }
            return ret;
        }
        
        public void handlePointerPress( PointerEvent event ) {
            SVGRect rect = ((SVGLocatableElement)myUpButton).getScreenBBox();
            if( rect != null ) {
                SVGRectangle rectangle = new SVGRectangle( rect );
                if ( rectangle.contains(event.getX(), event.getY())){
                    pressUpButton();
                }
            }
            rect = ((SVGLocatableElement)myDownButton).getScreenBBox();
            if( rect != null ){
                SVGRectangle rectangle = new SVGRectangle( rect );
                if ( rectangle.contains(event.getX(), event.getY())){
                    pressDownButton();
                }
            }
            super.handlePointerPress(event);
        }
        
        public void handlePointerRelease( PointerEvent event ) {
            SVGRect rect = ((SVGLocatableElement)myUpButton).getScreenBBox();
            if( rect != null ) {
                SVGRectangle rectangle = new SVGRectangle( rect );
                if ( rectangle.contains(event.getX(), event.getY())){
                    releaseUpButton();
                }
            }
            rect = ((SVGLocatableElement)myDownButton).getScreenBBox();
            if( rect != null ){
                SVGRectangle rectangle = new SVGRectangle( rect );
                if ( rectangle.contains(event.getX(), event.getY())){
                    releaseDownButton();
                }
            }
            super.handlePointerRelease(event);
        }
        
    }
    
    private class DefaultSpinnerEditor extends SVGTextField {

        DefaultSpinnerEditor( SVGForm form, SVGLocatableElement element ,
                Object lock )
        {
            super(form, element);
            myLock = lock;
            SVGSpinner.this.addActionListener(new SVGActionListener() {

                public void actionPerformed( SVGComponent comp ) {
                    if (comp == SVGSpinner.this && isInternallyEdited) {
                        if (getValue() != null) {
                            setText(getValue().toString());
                        }
                    }
                }
            });

        }
        
        /* (non-Javadoc)
         * @see org.netbeans.microedition.svg.SVGTextField#setText(java.lang.String)
         */
        public void setText( String text ) {
            super.setText(text);
            if ( myLock == null ){
                return;
            }
            synchronized (myLock) {
                if (!isInternallyEdited) {
                    getModel().setValue(text);
                }
            }
        }
        
        private Object myLock;
    }
    
    private class DefaultModel implements SVGSpinnerModel {
        
        DefaultModel(){
            myListeners = new Vector( 1 );
        }

        public Object getNextValue() {
            return new Integer( myValue.intValue()+1 );
        }

        public Object getPreviousValue() {
            return new Integer( myValue.intValue()-1 );
        }

        public Object getValue() {
            return myValue;
        }
        
        public void addDataListener( DataListener listener ) {
            synchronized( myListeners ){
                myListeners.addElement( listener );
            }
        }

        public void removeDataListener( DataListener listener ) {
            synchronized( myListeners ){
                myListeners.addElement( listener );
            }            
        }

        public void setValue( Object value ) {
            String strValue = value.toString();
            Integer result = Integer.valueOf( strValue );
            myValue = result;
            fireDataChange();
        }
        
        protected void fireDataChange(){
            synchronized( myListeners ){
                Enumeration en = myListeners.elements();
                while ( en.hasMoreElements() ){
                    DataListener listener = (DataListener)en.nextElement();
                    listener.contentsChanged( this );
                }
            }
        }
        
        private Integer myValue = new Integer(0);
        
        private Vector myListeners;

    }
        

    private InputHandler myInputHandler;
    
    private SVGElement myUpButton;
    private SVGElement myDownButton;
    
    private SVGComponent myEditor;
    
    private SVGSpinnerModel myModel;
    
    private SVGAnimationElement myUpPressedAnimation;
    private SVGAnimationElement myUpReleasedAnimation;
    private SVGAnimationElement myDownPressedAnimation;
    private SVGAnimationElement myDownReleasedAnimation;
    
    private boolean isUIAction;
    private boolean isInternallyEdited;
    private Object myUILock;

}
