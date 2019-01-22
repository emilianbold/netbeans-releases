/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
import org.netbeans.microedition.svg.input.PointerEvent;
import org.netbeans.microedition.svg.meta.MetaData;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGLocatableElement;
import org.w3c.dom.svg.SVGRect;


/**
 * Suggested svg snippet:
 * <pre>
 *  &lt;g id="list" transform="translate(20,220)" >
 *       &lt;g>
 *       &lt;!-- Metadata information. Please don't edit. -->
 *       &lt;text display="none">type=selection&lt;/text>
 *       
 *       &lt;rect id="list_selection" x="5" y="0" stroke="black" stroke-width="1" fill="rgb(200,200,255)" visibility="inherit" width="80" height="0"/>
 *       &lt;/g>
 *       &lt;g>
 *         &lt;text id="list_hidden_text" visibility="hidden" x="10" y="13" stroke="black" font-size="15" font-family="SunSansSemiBold">
 *          HIDDEN TEXT&lt;/text>
 *         &lt;text display="none">type=hidden_text&lt;/text>
 *       &lt;/g>
 *   &lt;g id="list_current_selection" >
 *       &lt;!-- Metadata information. Please don't edit. -->
 *       &lt;text display="none">type=current_selection&lt;/text> 
 *   &lt;/g>
 *   &lt;g id="list_content" > 
 *       &lt;!-- Metadata information. Please don't edit. -->
 *       &lt;text display="none">type=content&lt;/text> 
 *   &lt;/g>
 *       &lt;rect x="0" y="-5" rx="5" ry="5" width="90" height="70" fill="none" stroke="rgb(255,165,0)" stroke-width="2" visibility="hidden">
 *           &lt;set attributeName="visibility" attributeType="XML" begin="list.focusin" fill="freeze" to="visible"/>
 *           &lt;set attributeName="visibility" attributeType="XML" begin="list.focusout" fill="freeze" to="hidden"/>
 *       &lt;/rect>
 *   &lt;g>
 *           &lt;!-- Metadata information. Please don't edit. -->
 *       &lt;text display="none">type=bound&lt;/text>
 *
 *       &lt;rect id="list_bound" x="5.0" y="0.0" width="80" height="60" fill="none" stroke="black" stroke-width="2"/>
 *   &lt;/g>
 *   &lt;/g>
 * </pre>
 * 
 * Nested elements 'content' and 'bound' are necessary.
 * All other is used by default renderer. 
 * See {@link SVGDefaultListCellRenderer.}  
 * 
 *
 */
public class SVGList extends SVGComponent implements DataListener {
    
    private static final String CONTENT             = "content";          // NOI18N
    private static final String SELECTION           = "selection";        // NOI18N
    private static final String CUR_SELECTION       = "current_selection";// NOI18N
    private static final String HIDDEN_TEXT         = "hidden_text";      // NOI18N
    private static final String BOUNDS              = "bound";            // NOI18N
    
    private static final String CONTENT_SUFFIX      = DASH + CONTENT;
    private static final String SELECTION_SUFFIX    = DASH + SELECTION;
    private static final String CUR_SELECTION_SUFFIX= DASH + CUR_SELECTION;
    private static final String BOUNDS_SUFIX        = DASH + BOUNDS;
    private static final String HIDDEN_TEXT_SUFFIX  = DASH +HIDDEN_TEXT;


    public SVGList( SVGForm form, SVGLocatableElement element) {
        super(form, element);
        
        initNestedElements();
        
        verify( );
        
        if ( myHiddenText != null ){
            float height = myHiddenText.getFloatTrait( SVGTextField.TRAIT_FONT_SIZE );
            myCount = (int)(myBounds.getBBox().getHeight()/height);
            setRenderer( new SVGDefaultListCellRenderer( height , form, 
                    myHiddenText ));
        }
        
        //setTraitSafely( hiddenText, TRAIT_VISIBILITY, TR_VALUE_HIDDEN);
        
        isSlave = TR_VALUE_HIDDEN.equals(getElement().getTrait( TRAIT_VISIBILITY ));
        
        myRenderedComponents = new Vector/*<SVGComponent>*/();
        setSelectionModel(  new DefaultSelectionModel() );
        myHandler = new ListHandler();    
        
        setModel( new DefaultListModel( new Vector()));
    }

    public SVGList( SVGForm form, String elemId ){
        this( form , (SVGLocatableElement)
                form.getDocument().getElementById( elemId ));
    }


    public SVGListCellRenderer getRenderer(){
        return myRenderer;
    }
    
    public ListModel getModel(){
        return myModel;
    }
    
    public void setModel( ListModel model ){
        if ( myModel != null ){
            myModel.removeDataListener( this );
        }
        myModel = model;
        myModel.addDataListener( this );
        
        if ( model.getSize() >0 ){
            getSelectionModel().addSelectionInterval(0, 0);
        }
        
        renderList();
        
    }
    
    public void setRenderer( SVGListCellRenderer renderer ){
        myRenderer = renderer;
    }
    
    public SelectionModel getSelectionModel(){
        return mySelectionModel;
    }
    
    public void setSelectionModel( SelectionModel model ){
        if ( mySelectionModel != null ){
            mySelectionModel.removeDataListener( this );
        }
        mySelectionModel = model;
        mySelectionModel.addDataListener( this );
    }
    
    public InputHandler getInputHandler(){
        return myHandler;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.microedition.svg.DataListener#contentsChanged(java.lang.Object)
     */
    public void contentsChanged( Object source ) {
        if ( source == getSelectionModel() ){
            synchronized (myUILock) {
                if ( !isUIAction ){
                    if ( getSelectionModel().getSelectedIndex() != -1 ){
                        myCurrentIndex = getSelectionModel().getSelectedIndex();
                    }
                    renderList();
                }
            }
        }
        else if ( source == getModel() ){
            renderList();
        }
    }
    
    SVGLocatableElement getBoundsElement(){
        return myBounds;
    }
    
    SVGLocatableElement getHiddenText(){
        return myHiddenText;
    }
    
    SVGLocatableElement getContent(){
        return myContent;
    }
    
    SVGLocatableElement getSelection(){
        return mySelection;
    }
    
    SVGLocatableElement getCurrentSelection(){
        return myCurrentSelection;
    }
    
    boolean isSlave(){
        return isSlave;
    }
    
    private void  initNestedElements() {
        if (getElement().getId() != null) {
            myHiddenText = (SVGLocatableElement) getElementById(getElement(),
                    getElement().getId() + HIDDEN_TEXT_SUFFIX);
            myBounds = (SVGLocatableElement) getElementById(getElement(),
                    getElement().getId() + BOUNDS_SUFIX);
            myContent = (SVGLocatableElement)getElementById( getElement(), 
                    getElement().getId() +CONTENT_SUFFIX );
            mySelection = (SVGLocatableElement)getElementById( getElement(), 
                    getElement().getId() +SELECTION_SUFFIX );
            myCurrentSelection = (SVGLocatableElement)getElementById( getElement(), 
                    getElement().getId() +CUR_SELECTION_SUFFIX );
        }
        if (myHiddenText == null) {
            myHiddenText = (SVGLocatableElement) getNestedElementByMeta(getElement(),
                    TYPE, HIDDEN_TEXT);
        }

        if (myBounds == null) {
            myBounds = (SVGLocatableElement) getNestedElementByMeta(getElement(), TYPE,
                    BOUNDS);
        }
        
        if ( myContent == null ){
            myContent = (SVGLocatableElement)getElementByMeta(
                getElement(), SVGList.TYPE,  SVGList.CONTENT );
        }
        
        if ( mySelection == null ){
            mySelection = (SVGLocatableElement)getElementByMeta(getElement(), 
                    SVGList.TYPE,SELECTION);
        }
        
        if ( myCurrentSelection == null ){
            myCurrentSelection = (SVGLocatableElement)getElementByMeta(getElement(), 
                    SVGList.TYPE,CUR_SELECTION);
        }
    }
    
    private void verify(  ) {
        if ( myBounds ==null ){
            throw new IllegalArgumentException("Element with id="+
                    getElement().getId()+" couldn't be List element" +
                    		" becuase it doesn't contain nested 'bound' " +
                    		"element. See javadoc for SVG snippet.");
        }
        if ( myContent == null ){
            throw new IllegalArgumentException("Element with id="+
                    getElement().getId()+" couldn't be List element" +
                            " becuase it doesn't contain nested 'content' " +
                            " element. See javadoc for SVG snippet.");
        }
    }
    
    private void renderList() {
        synchronized (this) {
            if (myCount!=-1 && myCurrentIndex >= myTopIndex + myCount) {
                myTopIndex = myCurrentIndex - myCount +1;
            }
            else if (myCurrentIndex < myTopIndex) {
                myTopIndex = myCurrentIndex ;
            }
        }
        
        doRenderList();
    }
    
    private void renderList( int deltaTopIndex ) {
        synchronized (this) {
            int topIndex = myTopIndex;
            topIndex += deltaTopIndex;
            if ( topIndex < 0 ){
                topIndex = 0;
            }
            if ( topIndex >= getModel().getSize() - myCount ){
                topIndex = getModel().getSize() - myCount ;
            }
            if ( topIndex == myTopIndex ){
                return;
            }
            else {
                myTopIndex = topIndex;
            }
        }
        
        doRenderList();
    }

    private void doRenderList(){
        final int top = myTopIndex;
        final int currentIndex = myCurrentIndex;
        
        getForm().invokeLaterSafely(new Runnable() {

            public void run() {
                removeContent();
                
                ListModel model = getModel();
                int size = model.getSize();
                if ( size == 0 ){
                    return;
                }
                SVGListCellRenderer renderer = getRenderer();
                Vector vector = new Vector();
                int i = top;
                do {
                    SVGComponent comp = 
                        renderer.getCellRendererComponent(SVGList.this, model
                            .getElementAt(i), i - top, getSelectionModel()
                            .isSelectedIndex(i), i == currentIndex );
                    vector.addElement( comp );
                    SVGRectangle rectangle = comp.getBounds();
                    if ( myCount == -1 && i== top ){
                        myCount = (int)(myBounds.getBBox().getHeight()/
                                rectangle.getHeight());
                    }
                    i++;
                }
                while( i< Math.min(top + myCount, size) );
                synchronized ( SVGList.this ){
                    myRenderedComponents = vector;
                }
            }
        });
    }

    private void removeContent() {
        removeChildren( myCurrentSelection );
        removeChildren( myContent );
    }
    
    private void removeChildren( SVGLocatableElement element) {
        Node node = element.getFirstElementChild();
        while (node != null) {
            Element next = null;
            if (node instanceof SVGElement) {
                next = ((SVGElement) node).getNextElementSibling();
            }
            if (!MetaData.METADATA.equals(node.getLocalName())) {
                element.removeChild(node);
            }
            else if (node instanceof SVGElement) {
                String display = ((SVGElement) node).getTrait(MetaData.DISPLAY);
                if (!MetaData.NONE.equals(display)) {
                    final Node forRemove = node;
                    element.removeChild(forRemove);
                }
            }
            node = next;
        }
    }

    public interface ListModel {
        Object getElementAt( int index );
        int getSize();
        void addDataListener( DataListener listener );
        void removeDataListener( DataListener listener );
    }
    
    public interface SelectionModel {
        int SINGLE_SELECTION = 0;
        int MULTIPLE_SELECTION = 1;
        
        void clearSelection();
        boolean isSelectedIndex( int index );
        void addSelectionInterval( int from , int to);
        void removeSelectionInterval( int from , int to );
        void addDataListener( DataListener listener );
        void removeDataListener( DataListener listener );
        int getSelectionMode();
        void setSelectionMode( int mode );
        /**
         * Returns the smallest selected cell index; the selection when only a single item is selected in the list.
         */
        int getSelectedIndex();
    }
    
    public static class DefaultListModel implements ListModel {
        
        public DefaultListModel( Vector data ){
            myListeners = new Vector(1);
            myData = new Vector();
            for ( int i=0; i<data.size() ; i++ ){
                myData.addElement( data.elementAt( i ));
            }
        }
        
        public Object getElementAt( int indx ) {
            return myData.elementAt(indx);
        }

        public int getSize() {
            return myData.size();
        }
        
        public  void addDataListener( DataListener listener ) {
            synchronized ( myListeners ) {
                myListeners.addElement( listener );
            }
        }

        public synchronized void removeDataListener( DataListener listener ) {
            synchronized ( myListeners ) {
                myListeners.removeElement( listener );
            }            
        }
        
        protected void fireDataChanged(){
            synchronized ( myListeners ) {
                Enumeration en = myListeners.elements();
                while ( en.hasMoreElements() ){
                    DataListener listener = (DataListener)en.nextElement();
                    listener.contentsChanged( this );
                }
            }
        }
        
        private Vector myData;
        private Vector myListeners;
    }
    
    private class DefaultSelectionModel implements SelectionModel {
        
        DefaultSelectionModel() {
            myListeners = new Vector(1);
            setSelectionMode( SINGLE_SELECTION );
        }

        public boolean isSelectedIndex( int index ) {
            return mySelectedIndeces.contains( new Integer(index));
        }
        
        public void addSelectionInterval( int from, int to ) {
            if ( getSelectionMode() == SINGLE_SELECTION ){
                doClearSelection();
            }
            if ( from <=to ){
                for( int i=from ; i<=to ; i++ ){
                    mySelectedIndeces.addElement( new Integer(i));
                }
            }
            else {
                for( int i=to ; i<=from ; i++ ){
                    mySelectedIndeces.addElement( new Integer(i));
                }
            }
            fireDataChanged();
        }
        
        public void clearSelection() {
            doClearSelection();
            fireDataChanged();
        }
        
        /* (non-Javadoc)
         * @see org.netbeans.microedition.svg.SVGList.SelectionModel#addDataListener(org.netbeans.microedition.svg.DataListener)
         */
        public void addDataListener( DataListener listener ) {
            synchronized (myListeners) {
                myListeners.addElement( listener );
            }
        }

        /* (non-Javadoc)
         * @see org.netbeans.microedition.svg.SVGList.SelectionModel#removeDataListener(org.netbeans.microedition.svg.DataListener)
         */
        public void removeDataListener( DataListener listener ) {
            synchronized (myListeners) {
                myListeners.removeElement( listener );
            }
        }
        
        /* (non-Javadoc)
         * @see org.netbeans.microedition.svg.SVGList.SelectionModel#getSelectionMode()
         */
        public int getSelectionMode() {
            return myMode;
        }

        /* (non-Javadoc)
         * @see org.netbeans.microedition.svg.SVGList.SelectionModel#setSelectionMode(int)
         */
        public void setSelectionMode( int mode ) {
            myMode = mode;
            if ( mode == SINGLE_SELECTION ){
                mySelectedIndeces = new Vector(1);
            }
            else {
                mySelectedIndeces = new Vector();
            }
        }
        
        /* (non-Javadoc)
         * @see org.netbeans.microedition.svg.SVGList.SelectionModel#getSelectedIndex()
         */
        public int getSelectedIndex() {
            if ( mySelectedIndeces.isEmpty() ){
                return -1;
            }
            else {
                Integer index = (Integer)mySelectedIndeces.elementAt( 0 );
                return index.intValue();
            }
        }
        
        public void removeSelectionInterval( int from, int to ) {
            if ( mySelectedIndeces.isEmpty() ){
                return;
            }
            else {
                int begin = ( from <=to )? from: to;
                int end = ( from <=to )? to: from;
                int i = 0;
                while( i< mySelectedIndeces.size() ){
                    Integer index = (Integer)mySelectedIndeces.elementAt( i );
                    if ( begin<=index.intValue() && index.intValue()<=end ){
                        mySelectedIndeces.removeElementAt( i );
                    }
                    else {
                        i++;
                    }
                }
            }
        }
        
        protected void fireDataChanged(){
            synchronized ( myListeners ) {
                Enumeration en = myListeners.elements();
                while ( en.hasMoreElements() ){
                    DataListener listener = (DataListener)en.nextElement();
                    listener.contentsChanged( this );
                }
            }
        }
        
        private void doClearSelection(){
            mySelectedIndeces.removeAllElements();
        }
        
        private Vector mySelectedIndeces;
        private Vector myListeners;
        private int myMode;

    }
    
    private class ListHandler extends InputHandler {

        public boolean handleKeyPress( SVGComponent comp, int keyCode ) {
            return (comp instanceof SVGList) && 
                 ( keyCode == LEFT || keyCode == RIGHT || keyCode == FIRE ); 
        }

        public boolean handleKeyRelease( SVGComponent comp, int keyCode ) {
            boolean ret = false;
            if ( comp instanceof SVGList ){
                if ( keyCode == LEFT ){
                    myCurrentIndex = Math.max( 0 , myCurrentIndex -1 );
                    synchronized (myUILock) {
                        isUIAction = true;
                        getSelectionModel().clearSelection();
                        getSelectionModel().addSelectionInterval(
                                myCurrentIndex, myCurrentIndex);
                        isUIAction = false;
                    }
                    renderList();
                    ret = true;
                }
                else if ( keyCode == RIGHT ){
                    myCurrentIndex = Math.min( myCurrentIndex +1 , 
                            getModel().getSize() -1  );
                    synchronized (myUILock) {
                        isUIAction = true;
                        getSelectionModel().clearSelection();
                        getSelectionModel().addSelectionInterval(
                                myCurrentIndex, myCurrentIndex);
                        isUIAction = false;
                    }
                    renderList();
                    ret = true;
                }
                else if( keyCode == FIRE ){
                    fireActionPerformed();
                }
            }
            return ret;
        }
        
        public void handlePointerPress( PointerEvent event ) {
            requestFocus();
            if ( mySelection.getScreenBBox() == null ){
                super.handlePointerPress(event);
                return;
            }
            synchronized ( SVGList.this ){
                for ( int i=0; i<myRenderedComponents.size(); i++ ){
                    SVGComponent component = 
                        (SVGComponent)myRenderedComponents.elementAt( i );
                    SVGRectangle rect = component.getBounds();
                    if ( rect == null ){
                        continue;    
                    }

                    SVGRect selection = mySelection.getScreenBBox();
                    if ( selection.getHeight() == 0 ){
                        // horizontal layout
                        rect.setSize((int)mySelection.getScreenBBox().getWidth(),
                            (int)rect.getHeight());
                    }
                    else {
                        // vertical layout
                        rect.setSize( (int)rect.getWidth(),
                                (int)mySelection.getScreenBBox().getHeight());
                    }
                    
                    if ( rect.contains(event.getX(), event.getY())){
                        myPressedIndex  = i;
                        if ( selection.getHeight() == 0 ){
                            // horizontal layout
                            myPressedY = event.getY();
                        }
                        else {
                            // vertical layout
                            myPressedY = event.getX();
                        }
                        myPressedComponent = component;
                        break;
                    }
                }
            }
            super.handlePointerPress(event);
        }
        
        public void handlePointerRelease( PointerEvent event ) {
            int index = myPressedIndex +myTopIndex;
            if (myPressedComponent != null && 
                    myPressedComponent.getBounds() != null) 
            {
                SVGRectangle rect = myPressedComponent.getBounds();
                if ( mySelection.getScreenBBox() != null) {
                    if ( mySelection.getScreenBBox().getHeight() == 0 ){
                        // horizontal layout
                        rect.setSize((int)mySelection.getScreenBBox().getWidth(),
                            (int)rect.getHeight());
                    }
                    else {
                        // vertical layout
                        rect.setSize((int)rect.getWidth(),
                                (int)mySelection.getScreenBBox().getHeight());
                    }
                    
                    if (rect.contains( event.getX(), event.getY())) {
                        myCurrentIndex = index;
                        synchronized (myUILock) {
                            isUIAction = true;
                            doSelection();
                            isUIAction = false;
                        }
                        renderList();
                    }
                    else {
                        int count;
                        if ( mySelection.getScreenBBox().getHeight() == 0 ){
                            count = (int)(( event.getY()-myPressedY)/rect.getHeight());
                        }
                        else {
                            // vertical layout
                            count = (int)(( event.getX()-myPressedY)/rect.getWidth());
                        }
                        renderList( count);
                    }
                }
            }
            super.handlePointerRelease( event );
            myPressedIndex = 0;
            myPressedComponent = null;
        }

        private void doSelection() {
            if (getSelectionModel().getSelectionMode() 
                    == SelectionModel.SINGLE_SELECTION)
            {
                //getSelectionModel().clearSelection();
                getSelectionModel().addSelectionInterval(
                        myCurrentIndex, myCurrentIndex);
            }
            else if (getSelectionModel().getSelectionMode() 
                    == SelectionModel.MULTIPLE_SELECTION)
            {
                boolean isSelected = getSelectionModel().isSelectedIndex(
                        myCurrentIndex);
                if ( isSelected ){
                    getSelectionModel().removeSelectionInterval(myCurrentIndex, 
                            myCurrentIndex);
                }
                else {
                    getSelectionModel().addSelectionInterval(
                            myCurrentIndex, myCurrentIndex);
                }
            }
        }
        
        private int myPressedIndex = 0;
        private SVGComponent myPressedComponent;
        private float myPressedY;
        
    }
    
    private SVGListCellRenderer myRenderer;
    private ListModel myModel;
    private SelectionModel mySelectionModel;
    private InputHandler myHandler;
    
    private SVGLocatableElement myHiddenText;
    private SVGLocatableElement myContent;
    private SVGLocatableElement mySelection;
    private SVGLocatableElement myCurrentSelection;
    private SVGLocatableElement myBounds; 
    
    private int myTopIndex ;
    private int myCurrentIndex;
    
    private int myCount=-1;
    
    private boolean isUIAction;
    private Object myUILock = new Object();
    
    private Vector/*<SVGComponent>*/ myRenderedComponents;
    
    /*
     * This flag means that list is not standalone component but
     * used as additional component for rendering list of choices.
     * In this case "selection" should be always hidden/visible along with
     * list.
     */
    private boolean isSlave;

}
