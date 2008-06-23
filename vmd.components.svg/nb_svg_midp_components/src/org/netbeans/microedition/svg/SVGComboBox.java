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

import org.netbeans.microedition.svg.SVGList.DefaultListMoldel;
import org.netbeans.microedition.svg.SVGList.ListModel;
import org.netbeans.microedition.svg.input.InputHandler;
import org.netbeans.microedition.svg.input.NumPadInputHandler;
import org.netbeans.microedition.svg.meta.ChildrenAcceptor;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGAnimationElement;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGLocatableElement;


/**
 * Suggested svg snippet :
 * <pre>
 * &lt;g id="country_combobox" transform="..." ...>
 *       &lt;rect x="0" y="-5" rx="5" ry="5" width="90" height="30" fill="none" stroke="rgb(255,165,0)" stroke-width="2" visibility="hidden">
 *           &lt;set attributeName="visibility" attributeType="XML" begin="country_combobox.focusin" fill="freeze" to="visible"/>
 *           &lt;set attributeName="visibility" attributeType="XML" begin="country_combobox.focusout" fill="freeze" to="hidden"/>
 *       &lt;/rect>
 *   &lt;rect  x="5.0" y="0.0" width="80" height="20" fill="none" stroke="black" stroke-width="2"/>
 *   &lt;rect id="country_combobox_button" x="66.0" y="1.0" 
 *      width="18" height="18" fill="rgb(220,220,220)" stroke="black" stroke-width="1.5">
 *       &lt;animate id="country_combobox_button_pressed" attributeName="fill" attributeType="XML" begin="indefinite" dur="0.25s" fill="freeze"
 *               to="rgb(170,170,170)"/>
 *       &lt;animate id="country_combobox_button_released" attributeName="fill" attributeType="XML" begin="indefinite" dur="0.25s" fill="freeze"
 *               to="rgb(220,220,220)"/>
 *   &lt;/rect>
 *   &lt;polygon transform="translate(73,8)"  points="0,0 4,0 2,4" fill="blue" stroke="black" stroke-width="2"/>
 *   &lt;g id="country_combobox_editor">
 *       &lt;!-- this editor is SVGTextField component -->
 *
 *       &lt;!-- metadata definition-->
 *       &lt;text display="none">readOnly="false" enabled="true"</text>
 *       &lt;text id="country_combobox_editor_text" x="10" y="15" stroke="black" font-size="15" font-family="SunSansSemiBold">Item 1</text>
 *       &lt;rect id="country_combobox_editor_caret" visibility="visible" x="17" y="3" width="2" height="15" fill="black" stroke="black"/>
 *       &lt;!-- The rectangle below is difference between rectangle that bound combobox and combobox button ( the latter 
 *       has id = country_combobox_button ). It needed for counting bounds of input text area .
 *       It should be created programatically or SVGTextField should have API for dealing with "width"
 *       of editor not based only on width of text field component.-->
 *       &lt;rect visibility="hidden" x="5.0" y="0" width="60" height="20" />
 *   &lt;/g>
 * &lt;/g>
 * </pre>
 * 
 * Also the following snippet should be placed at the end of XML document
 * ( this is because it should be on very top of any figure ).
 * 
 * <pre>
 * &lt;g id="country_combobox_list"  visibility="hidden" transform="...." ...>
 *       &lt;!-- This is not standalone component ! It reelates to combobox component. This is list that is shown
 *       for user when he press to button. It should be outside of ComboBox component figure ( and should
 *       be at the very end of XML file between other such figures ) because in this case it will be 
 *       on top of any other figure. Otherwise it will be hidden by following sibling component. -->
 *       &lt;text id="country_combobox_list_hidden_text" visibility="hidden" x="10" y="13" stroke="black" font-size="15" font-family="SunSansSemiBold">
 *           HIDDEN TEXT
 *       &lt;/text>
 *       &lt;rect id="country_combobox_list_bound" x="5.0" y="0.0" width="80" height="60" fill="white" stroke="black" stroke-width="2"
 *           visibility="inherit"/>
 *       &lt;rect id="country_combobox_list_selection" x="5" y="0" stroke="black" stroke-width="1" fill="rgb(200,200,255)" visibility="inherit"
 *           width="80" height="0"/>
 *       &lt;g id="country_combobox_list_content" visibility="inherit"/>
 *   &lt;/g>
 * </pre>  
 * 
 * @author ads
 *
 */
public class SVGComboBox extends SVGComponent {
    
    private static final String EDITOR          = "editor";         // NOI18N
    private static final String BUTTON          = "button";         // NOI18N
    private static final String LIST            = "list";           // NOI18N
    
    private static final String PRESSED         = "pressed";        // NOI18N
    private static final String RELEASED        = "released";       // NOI18N

    public SVGComboBox( SVGForm form, String elemId ) {
        super(form, elemId);
        
        myButton =  getElementByMeta( wrapperElement, TYPE , BUTTON);
        myPressedAnimation = (SVGAnimationElement) getElementByMeta(myButton, 
                TYPE , PRESSED );
        myReleasedAnimation = (SVGAnimationElement) getElementByMeta(myButton, 
                TYPE , RELEASED );
        
        Element root = form.getDocument().getDocumentElement();
        SVGElement listElement = getElementByMeta( (SVGElement)root , 
                REF , getElement().getId());
        myList = new SVGList( form , (SVGLocatableElement)listElement );
        
        myInputHandler = new ComboBoxInputHandler();
        myEditor = new DefaultComboBoxEditor( form , 
                (SVGLocatableElement)getElementByMeta( getElement(),TYPE, EDITOR));
        myRenderer = new SVGDefaultListCellRenderer();
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
        myList.setModel( model );
    }
    
    public SVGComponent getEditor(){
        return myEditor;
    }
    
    public void setEditor( SVGComponent editor ){
        myEditor = editor;
    }
    
    public SVGListCellRenderer getRenderer(){
        return myRenderer;
    }
    
    public void setRenderer( SVGListCellRenderer renderer ){
        myRenderer = renderer;
    }
    
    public interface ComboBoxModel extends ListModel{
        int getSelectedIndex();
        void setSelectedItem( int index );
    }
    
    private void showList(){
        isListShown = true;
        myList.focusGained();
        
        myList.getElement().setTrait("visibility", "visible");
        myIndex = getModel().getSelectedIndex();
    }
    
    private void hideList(){
        isListShown = false;
        myList.focusLost();
        
        myList.getElement().setTrait("visibility", "hidden");
    }
    
    public static class DefaultModel extends DefaultListMoldel 
        implements ComboBoxModel 
    {
        
        public DefaultModel( Vector data ){
            super( data );
        }

        public int getSelectedIndex() {
            return myCurrentSelectionIndx;
        }

        public void setSelectedItem( int index ) {
            
            myCurrentSelectionIndx = index;
        }
        
        private int myCurrentSelectionIndx;
    }
    
    private class ComboBoxInputHandler extends NumPadInputHandler {

        public ComboBoxInputHandler( ) {
            super( form.getDisplay() );
        }

        public boolean handleKeyPress( SVGComponent comp, int keyCode ) {
            boolean ret = false;
            if ( comp instanceof SVGComboBox ){
                if ( keyCode == LEFT ){
                    if ( isListShown ) {
                        ret = myList.getInputHandler().handleKeyPress( comp, keyCode);
                    }
                    else {
                        ret= true;
                    }
                }
                else if ( keyCode == RIGHT ){
                    if ( !isListShown ){
                        myPressedAnimation.beginElementAt(0);
                        ret = true;
                    }
                    else {
                        ret = myList.getInputHandler().handleKeyPress( myList, keyCode);
                    }
                }
                else if( keyCode == FIRE ){
                    if ( isListShown ){
                        ret = myList.getInputHandler().handleKeyPress( myList, keyCode);
                    }
                    else {
                        ret = true;
                    }
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
                        myIndex = Math.max(0, myIndex - 1);
                        myList.getInputHandler().handleKeyRelease( myList, keyCode);
                    }
                    ret = true;
                }
                else if ( keyCode == RIGHT ){
                    if (isListShown) {
                        myIndex = Math.min(getModel().getSize() - 1, myIndex + 1);
                        myList.getInputHandler().handleKeyRelease( myList, keyCode);
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
                    myList.getSelectionModel().addSelectionInterval(
                            myIndex, myIndex);
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

        public DefaultComboBoxEditor( SVGForm form , SVGLocatableElement element ) {
            super(form , element );
            SVGComboBox.this.addActionListener( new SVGActionListener (){
                public void actionPerformed( SVGComponent comp ) {
                    if( comp == SVGComboBox.this){
                        int index = getModel().getSelectedIndex();
                        Object selected = index <getModel().getSize() ? 
                                getModel().getElementAt(index) : null ;
                                System.out.println("$$$$$$$$$$ " +selected);
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
    private SVGListCellRenderer myRenderer;
    
    private InputHandler myInputHandler;
    private final SVGElement myButton;
    private final SVGAnimationElement myPressedAnimation;
    private final SVGAnimationElement myReleasedAnimation;
    
    private final SVGList myList;
    
    private boolean isListShown;
    
    private int myIndex;

}
