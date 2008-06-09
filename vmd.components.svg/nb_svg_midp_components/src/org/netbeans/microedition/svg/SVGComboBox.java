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



import java.util.Vector;

import org.netbeans.microedition.svg.input.InputHandler;
import org.netbeans.microedition.svg.input.NumPadInputHandler;
import org.w3c.dom.svg.SVGAnimationElement;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGLocatableElement;
import org.w3c.dom.svg.SVGRect;


/**
 * @author ads
 *
 */
public class SVGComboBox extends SVGComponent {
    
    private final String EDITOR_SUFFIX = "_editor";
    private final String BUTTON_SUFFIX = "_button";
    private final String LIST_SUFFIX = "_list";
    private final String CURRENT_SUFFIX = "_current";
    
    private final String PRESSED_ELEM_SUFFIX = "_pressed";
    private final String RELEASED_ELEM_SUFFIX = "_released";

    public SVGComboBox( SVGForm form, String elemId ) {
        super(form, elemId);
        
        myButton = (SVGElement) getElementById( wrapperElement, elemId + BUTTON_SUFFIX);
        myPressedAnimation = (SVGAnimationElement) getElementById(myButton, 
                myButton.getId() + PRESSED_ELEM_SUFFIX);
        myReleasedAnimation = (SVGAnimationElement) getElementById(myButton, 
                myButton.getId() + RELEASED_ELEM_SUFFIX);
        myList = (SVGElement ) getElementById( wrapperElement, elemId + LIST_SUFFIX);
        myCurrentRect = (SVGLocatableElement) getElementById( myList, 
                myList.getId() + CURRENT_SUFFIX);
        
        myInputHandler = new ComboBoxInputHandler();
        myEditor = new DefaultComboBoxEditor( form , elemId + EDITOR_SUFFIX);
    }
    
    public void focusGained() {
        super.focusGained();
        getEditor().focusGained();
    }
    
    public void focusLost() {
        super.focusLost();
        hideList();
        getEditor().focusLost();
    }
    
    public InputHandler getInputHandler() {
        return myInputHandler;
    }
    
    public ComboBoxModel getModel(){
        return myModel;
    }
    
    public void setModel( ComboBoxModel model ){
        myModel = model;
    }
    
    public SVGComponent getEditor(){
        return myEditor;
    }
    
    public interface ComboBoxModel {
        int getSelectedIndex();
        void setSelectedItem( int index );
        Object getElementAt( int indx );
        int getSize();
    }
    
    private void updateList( byte direction ){
        SVGRect rect = myCurrentRect.getBBox();
        float y = rect.getY();
        if ( myOriginalY == null ){
            myOriginalY = new Float(y);
        }
        float height = rect.getHeight();
        y += direction * height;
        myCurrentRect.setTrait("y", ""+y);
    }
    
    private void showList(){
        if ( myOriginalY != null ){
            myCurrentRect.setTrait("y", ""+myOriginalY);
        }
        isListShown = true;
        myList.setTrait("visibility", "visible");
    }
    
    private void hideList(){
        isListShown = false;
        myList.setTrait("visibility", "hidden");
    }
    
    public static class DefaultModel implements ComboBoxModel {
        
        public DefaultModel( Vector data ){
            myData = new Vector();
            for ( int i=0; i<data.size() ; i++ ){
                myData.addElement( data.elementAt( i ));
            }
        }

        public Object getElementAt( int indx ) {
            return myData.elementAt(indx);
        }

        public int getSelectedIndex() {
            return myCurrentSelectionIndx;
        }

        public int getSize() {
            return myData.size();
        }

        public void setSelectedItem( int index ) {
            myCurrentSelectionIndx = index;
        }
        
        private int myCurrentSelectionIndx;
        private Vector myData;
    }
    
    private class ComboBoxInputHandler extends NumPadInputHandler {

        public ComboBoxInputHandler( ) {
            super( form.getDisplay() );
        }

        public boolean handleKeyPress( SVGComponent comp, int keyCode ) {
            boolean ret = false;
            if ( comp instanceof SVGComboBox ){
                if ( keyCode == LEFT ){
                    ret = true;
                }
                else if ( keyCode == RIGHT ){
                    if ( !isListShown ){
                        myPressedAnimation.beginElementAt(0);
                    }
                    ret = true;
                }
                else if( keyCode == FIRE ){
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
            if ( comp instanceof SVGComboBox ){
                if ( keyCode == LEFT ){
                    if (isListShown) {
                        int index = getModel().getSelectedIndex();
                        myIndex = Math.max(0, index - 1);
                        updateList( (byte)-1 );
                    }
                    ret = true;
                }
                else if ( keyCode == RIGHT ){
                    if (isListShown) {
                        int index = getModel().getSelectedIndex();
                        myIndex = Math.min(getModel().getSize() - 1, index + 1);
                        updateList( (byte)1 );
                    }
                    else {
                        myReleasedAnimation.beginElementAt( 0 );
                        showList();
                    }
                    ret = true;
                }
                else if( keyCode == FIRE ){
                    hideList();
                    getModel().setSelectedItem( myIndex );
                    fireActionPerformed();
                }
                else {
                    return getEditor().getInputHandler().handleKeyRelease(getEditor(), 
                            keyCode);
                }
            }
            return ret;
        }
        
    }
    
    private class DefaultComboBoxEditor extends SVGTextField {

        public DefaultComboBoxEditor( SVGForm form , String id ) {
            super(form , id );
            SVGComboBox.this.addActionListener( new SVGActionListener (){
                public void actionPerformed( SVGComponent comp ) {
                    if( comp == SVGComboBox.this){
                        int index = getModel().getSelectedIndex();
                        Object selected = index <getModel().getSize() ? 
                                getModel().getElementAt(index) : null ;
                        if ( selected != null ){
                            setText( selected.toString() );
                        }
                    }
                }
            });
        }
        
    }
    
    private ComboBoxModel myModel;
    private SVGComponent myEditor;
    private InputHandler myInputHandler;
    private final SVGElement myButton;
    private final SVGElement myList;
    private final SVGLocatableElement myCurrentRect;
    private final SVGAnimationElement myPressedAnimation;
    private final SVGAnimationElement myReleasedAnimation;
    
    private boolean isListShown;
    private int myIndex;
    
    private Float myOriginalY ;

}
