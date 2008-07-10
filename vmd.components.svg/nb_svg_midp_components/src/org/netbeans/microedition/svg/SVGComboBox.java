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
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGAnimationElement;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGLocatableElement;


/**
 * Suggested svg snippet :
 * <pre>
 * &lt;g id="country_combobox" transform="translate(20,180)">
 *   &lt;!-- Metadata information. Please don't edit. -->
 *   &lt;text display="none">type=combobox&lt;/text>
 *
 *       &lt;rect x="0" y="-5" rx="5" ry="5" width="90" height="30" fill="none" stroke="rgb(255,165,0)" 
 *              stroke-width="2" visibility="hidden">
 *           &lt;set attributeName="visibility" attributeType="XML" begin="country_combobox.focusin" 
 *               fill="freeze" to="visible"/>
 *           &lt;set attributeName="visibility" attributeType="XML" begin="country_combobox.focusout" 
 *               fill="freeze" to="hidden"/>
 *       &lt;/rect>
 *       &lt;rect  x="5.0" y="0.0" width="80" height="20" fill="none" stroke="black" stroke-width="2"/>
 *   &lt;g>
 *       &lt;!-- Metadata information. Please don't edit. -->
 *       &lt;text display="none">type=button&lt;/text>
 *
 *       &lt;rect  x="66.0" y="1.0" width="18" height="18" fill="rgb(220,220,220)" 
 *               stroke="black" stroke-width="1.5">
 *           &lt;animate  attributeName="fill" attributeType="XML" begin="indefinite" dur="0.25s" 
 *               fill="freeze" to="rgb(170,170,170)"/>
 *           &lt;animate  attributeName="fill" attributeType="XML" begin="indefinite" dur="0.25s" 
 *               fill="freeze" to="rgb(220,220,220)"/>
 *   &lt;/rect>
 *   &lt;/g>
 *   &lt;polygon transform="translate(73,8)"  points="0,0 4,0 2,4" fill="blue" 
 *                 stroke="black" stroke-width="2"/>
 *   &lt;g>
 *       &lt;!-- this editor is SVGTextField component -->
 *
 *       &lt;!-- Metadata information. Please don't edit. -->
 *       &lt;text display="none">type=editor&lt;/text>
 *       &lt;text display="none">readOnly="false" enabled="true"&lt;/text>
 *
 *       &lt;text  x="10" y="15" stroke="black" font-size="15" font-family="SunSansSemiBold">Item 1
 *           &lt;!-- Metadata information. Please don't edit. -->
 *           &lt;text display="none">type=text&lt;/text>
 *       &lt;/text>
 *       &lt;g>
 *           &lt;!-- Metadata information. Please don't edit. -->
 *           &lt;text display="none">type=caret&lt;/text>
 *           &lt;rect  visibility="visible" x="17" y="3" width="2" height="15" fill="black" stroke="black"/>
 *       &lt;/g>
 *       &lt;!-- The rectangle below is difference between rectangle that bound 
 *
 *               combobox and combobox button ( the latter 
 *       has id = country_combobox_button ). It needed for counting bounds of input text area .
 *       It should be created via source code or SVGTextField should have API for dealing with "width"
 *       of editor not based only on width of text field component.-->
 *       &lt;rect visibility="hidden" x="5.0" y="0" width="60" height="20" />
 *   &lt;/g>
 *   &lt;/g>
 *
 * </pre>
 * 
 * Also the following snippet should be placed at the end of XML document
 * ( this is because it should be on very top of any figure ).
 * 
 * <pre>
 * &lt;g visibility="hidden" transform="translate(20,200)">
 *       &lt;!-- Metadata information. Please don't edit. -->
 *       &lt;text display="none">ref=country_combobox&lt;/text>
 *       &lt;text display="none">type=list&lt;/text>
 *
 *       &lt;!-- This is not standalone component ! It reelates to combobox component. This is list that is shown
 *       for user when he press to button. It should be outside of ComboBox component figure ( and should
 *       be at the very end of XML file between other such figures ) because in this case it will be 
 *       on top of any other figure. Otherwise it will be hidden by following sibling component. -->
 *       &lt;text id="_53" visibility="hidden" x="10" y="13" stroke="black" font-size="15" font-family="SunSansSemiBold">
 *           &lt;!-- Metadata information. Please don't edit. -->
 *           &lt;text display="none">type=hidden_text&lt;/text>
 *           HIDDEN TEXT
 *       &lt;/text>
 *       &lt;g>
 *           &lt;!-- Metadata information. Please don't edit. -->
 *           &lt;text display="none">type=bound&lt;/text>
 *           &lt;rect  x="5.0" y="0.0" width="80" height="60" fill="white" stroke="black" stroke-width="2" visibility="inherit"/>
 *       &lt;/g>
 *       &lt;g>
 *           &lt;!-- Metadata information. Please don't edit. -->
 *           &lt;text display="none">type=selection&lt;/text>
 *           &lt;rect  x="5" y="0" stroke="black" stroke-width="1" fill="rgb(200,200,255)" visibility="inherit" width="80" height="0"/>
 *       &lt;/g>
 *       &lt;g  visibility="inherit">
 *           &lt;!-- Metadata information. Please don't edit. -->
 *           &lt;text display="none">type=content&lt;/text>
 *           &lt;/g>
 *   &lt;/g
 * </pre>  
 * 
 * @author ads
 *
 */
public class SVGComboBox extends SVGComponent implements 
    DataListener, SVGActionListener
{
    
    private static final String EDITOR          = "editor";         // NOI18N
    private static final String BUTTON          = "button";         // NOI18N
    private static final String LIST            = "list";           // NOI18N
    
    public SVGComboBox( SVGForm form, String elemId ) {
        super(form, elemId);
        
        initButton();
        
        Element root = form.getDocument().getDocumentElement();
        SVGElement listElement = getElementByMeta( (SVGElement)root , 
                REF , getElement().getId());
        myList = new SVGList( form , (SVGLocatableElement)listElement );
        
        myInputHandler = new ComboBoxInputHandler();
        setEditor( new DefaultComboBoxEditor( form , 
                (SVGLocatableElement)getElementByMeta( getElement(),
                        TYPE, EDITOR)));
    }
    
    public void focusGained() {
        super.focusGained();
        getEditor().getEditorComponent().focusGained();
    }
    
    public void focusLost() {
        super.focusLost();
        hideList();
        getEditor().getEditorComponent().focusLost();
    }
    
    public InputHandler getInputHandler() {
        return myInputHandler;
    }
    
    public ComboBoxModel getModel(){
        return myModel;
    }
    
    public void setModel( ComboBoxModel model ){
        if ( myModel != null ){
            myModel.removeDataListener( this );
        }
        myModel = model;
        model.addDataListener( this );
        myList.setModel( model );
    }
    
    public ComboBoxEditor getEditor(){
        return myEditor;
    }
    
    public void setEditor(  ComboBoxEditor editor ){
        if ( myEditor != null) {
            myEditor.removeActionListener( this );
        }
        myEditor = editor;
        myEditor.addActionListener( this );
    }
    
    public SVGListCellRenderer getRenderer(){
        return getList().getRenderer();
    }
    
    public void setRenderer( SVGListCellRenderer renderer ){
        getList().setRenderer(renderer);
    }
    
    public Object getSelectedItem(){
        return mySelectedValue;
    }
    
    public void setSelectedItem( Object value ){
        setSelected(value);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.microedition.svg.DataListener#contentsChanged(java.lang.Object)
     */
    public void contentsChanged( Object source ) {
        if (source != getModel()) {
            return;
        }
        synchronized (myUILock) {
            if (isUIAction) {
                isUIAction = false;
            }
            else {
                hideList();
                
                myIndex = getModel().getSelectedIndex();
                if ( myIndex != -1 ){
                    myList.getSelectionModel().addSelectionInterval(myIndex,
                            myIndex);
                    mySelectedValue = getModel().getElementAt(myIndex);
                    setItem();
                }
                else {
                    myList.getSelectionModel().clearSelection();
                    getEditor().setItem( mySelectedValue );
                }
                fireActionPerformed();
            }
        }
    }
   
    /* (non-Javadoc)
     * @see org.netbeans.microedition.svg.SVGActionListener#actionPerformed(org.netbeans.microedition.svg.SVGComponent)
     */
    public void actionPerformed( SVGComponent comp ) {
        setSelected( getEditor().getItem());
        fireActionPerformed();
    }
    

    private void initButton() {
        myButton =  getNestedElementByMeta( wrapperElement, TYPE , BUTTON);
        myPressedAnimation = (SVGAnimationElement) myButton.getFirstElementChild();
        myReleasedAnimation = (SVGAnimationElement) 
            myPressedAnimation.getNextElementSibling();
    }
    
    private void setSelected( Object value ){
        mySelectedValue = value;
        int size = getModel().getSize();
        boolean found = false;
        for ( int i=0; i<size ; i++ ){
            Object obj = getModel().getElementAt( i );
            if ( value == null ){
                if ( obj == null ){
                    getModel().setSelectedIndex( i );
                    myIndex = i;
                    found = true;
                }
            }
            else {
                if ( value.equals(obj)){
                    getModel().setSelectedIndex( i );
                    myIndex = i;
                    found = true;
                }
            }
        }
        if ( !found ){
            myIndex = -1;
            myList.getSelectionModel().clearSelection();
            getModel().setSelectedIndex( -1 );
        }
    }
    
    private void showList(){
        isListShown = true;
        myList.focusGained();
        
        myList.setTraitSafely( myList.getElement(), 
                TRAIT_VISIBILITY, TR_VALUE_VISIBLE );
        myIndex = getModel().getSelectedIndex();
    }
    
    private void hideList(){
        if ( isListShown ) {
            isListShown = false;
            myList.focusLost();
            myList.setTraitSafely(  myList.getElement(), 
                    TRAIT_VISIBILITY , TR_VALUE_HIDDEN);
        }
    }
    
    private void setItem(){
        int index = getModel().getSelectedIndex();
        Object selected = index <getModel().getSize() ? 
                getModel().getElementAt(index) : null ;
        getEditor().setItem( selected );
    }
    
    private SVGList getList(){
        return myList;
    }
    
    public interface ComboBoxModel extends ListModel{
        int getSelectedIndex();
        void setSelectedIndex( int index );
    }
    
    public interface ComboBoxEditor  {
        SVGComponent getEditorComponent();
        Object getItem();
        void setItem( Object value );
        void addActionListener(SVGActionListener listener);
        void removeActionListener(SVGActionListener listener);
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

        public void setSelectedIndex( int index ) {
            myCurrentSelectionIndx = index;
            fireDataChanged();
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
                        ret = myList.getInputHandler().handleKeyPress( comp, 
                                keyCode);
                    }
                    else {
                        ret= true;
                    }
                }
                else if ( keyCode == RIGHT ){
                    if ( !isListShown ){
                        getForm().invokeLaterSafely( new Runnable() {
                            public void run() {
                                myPressedAnimation.beginElementAt(0);
                            }
                        });
                        ret = true;
                    }
                    else {
                        ret = myList.getInputHandler().handleKeyPress( myList, 
                                keyCode);
                    }
                }
                else if( keyCode == FIRE ){
                    if ( isListShown ){
                        ret = myList.getInputHandler().handleKeyPress( myList, 
                                keyCode);
                    }
                    else {
                        SVGComponent component = getEditor().getEditorComponent();
                        return component.getInputHandler().handleKeyPress( 
                                component, keyCode);
                    }
                }
                else {
                    SVGComponent component = getEditor().getEditorComponent();
                    return component.getInputHandler().
                            handleKeyPress( component , keyCode);
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
                        myList.getInputHandler().handleKeyRelease( myList, 
                                keyCode);
                    }
                    ret = true;
                }
                else if ( keyCode == RIGHT ){
                    if (isListShown) {
                        myIndex = Math.min(getModel().getSize() - 1, myIndex + 1);
                        myList.getInputHandler().handleKeyRelease( myList, 
                                keyCode);
                    }
                    else {
                        getForm().invokeLaterSafely( new Runnable(){
                            public void run() {
                                myReleasedAnimation.beginElementAt( 0 );
                            }
                        });
                        showList();
                    }
                    ret = true;
                }
                else if( keyCode == FIRE ){
                    if (isListShown) {
                        hideList();
                        synchronized (myUILock) {
                            isUIAction = true;
                            getModel().setSelectedIndex(myIndex);
                        }
                        setItem();
                        fireActionPerformed();
                    }
                    else {
                        SVGComponent component = getEditor().getEditorComponent();
                        return component.getInputHandler().handleKeyRelease( 
                                component, keyCode);
                    }
                }
                else {
                    SVGComponent component = getEditor().getEditorComponent();
                    return component.getInputHandler().handleKeyRelease( 
                            component, keyCode);
                }
            }
            return ret;
        }
        
    }
    
    private class DefaultComboBoxEditor extends SVGTextField implements ComboBoxEditor{

        public DefaultComboBoxEditor( SVGForm form , SVGLocatableElement element ) {
            super(form , element );
            /*SVGComboBox.this.addActionListener( new SVGActionListener (){
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
            });*/
        }
        
        /* (non-Javadoc)
         * @see org.netbeans.microedition.svg.SVGComboBox.ComboBoxEditor#getEditorCompoenent()
         */
        public SVGComponent getEditorComponent() {
            return this;
        }

        /* (non-Javadoc)
         * @see org.netbeans.microedition.svg.SVGComboBox.ComboBoxEditor#getItem()
         */
        public Object getItem() {
            return getText();
        }

        /* (non-Javadoc)
         * @see org.netbeans.microedition.svg.SVGComboBox.ComboBoxEditor#setItem(java.lang.Object)
         */
        public void setItem( Object value ) {
            if ( value != null ){
                setText( value.toString() );
            }
            else {
                setText( "null" );                           // NOI18N
            }
        }
        
    }
    
    private ComboBoxModel myModel;
    private ComboBoxEditor myEditor;
    
    private InputHandler myInputHandler;
    private SVGElement myButton;
    private SVGAnimationElement myPressedAnimation;
    private SVGAnimationElement myReleasedAnimation;
    
    private final SVGList myList;
    
    private boolean isListShown;
    
    private int myIndex;
    private Object mySelectedValue;
    
    private boolean isUIAction;
    private Object myUILock = new Object();

}
