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

package org.netbeans.microedition.svg;

import org.netbeans.microedition.svg.input.InputHandler;
import org.netbeans.microedition.svg.input.TextInputHandler;
import org.netbeans.microedition.svg.meta.MetaData;
import org.w3c.dom.svg.SVGLocatableElement;
import org.w3c.dom.svg.SVGRect;

/**
 * Suggested svg snippet :
 * <pre>
 * &lt;g id="textfield_name" transform="translate(20,40)">
 *       &lt;!-- Metadata information. Please don't edit. -->
 *   &lt;text display="none">type=textfield&lt;/text>
 *       &lt;text display="none">editable=true&lt;/text>
 *       &lt;text display="none">enabled=true&lt;/text>
 *
 *       &lt;rect id="textfield_name_border" x="0" y="0" rx="5" ry="5" width="200" height="30" fill="none" stroke="black" stroke-width="2">
 *           &lt;animate attributeName="stroke" attributeType="XML" begin="textfield_name.focusin" dur="0.25s" fill="freeze" to="rgb(255,165,0)"/>
 *           &lt;animate attributeName="stroke" attributeType="XML" begin="textfield_name.focusout" dur="0.25s" fill="freeze" to="black"/>
 *       &lt;/rect>
 *       &lt;g>
 *             &lt;text  id="textfield_name_text" x="10" y="23" stroke="black" font-size="20" 
 *                 font-family="SunSansSemiBold">textField&lt;/text>
 *             &lt;!-- Metadata information. Please don't edit. -->
 *             &lt;text display="none">type=text&lt;/text>
 *       &lt;/g>
 *                 
 *   &lt;g>
 *           &lt;!-- Metadata information. Please don't edit. -->
 *           &lt;text display="none">type=caret&lt;/text>
 *
 *           &lt;rect id="textfield_name_caret" visibility="visible" x="20" y="4" 
 *              width="3" height="22" fill="black" stroke="black"/>
 *       &lt;/g>
 *   &lt;/g>
 * </pre>
 * @author Pavel Benes
 * @author ads
 */
public class SVGTextField extends AbstractTextRenderingComponent {

    protected static final String TRAIT_FONT_FAMILY = "font-family";      // NOI18N
    protected static final String TEXT              = "text";             // NOI18N
    private static final String CARETELEM           = "caret";            // NOI18N
    protected static final String TRAIT_FONT_SIZE   = "font-size";        // NOI18N
    protected static final String BORDER            ="border";            // NOI18N
    
    private static final String CARET_SUFFIX        = DASH+CARETELEM;     // NOI18N
    protected static final String TEXT_SUFFIX         = DASH + TEXT;        // NOI18N
    protected static final String BORDER_SUFFIX     = DASH+BORDER;        // NOI18N
    
    private static final String EDITABLE            = "editable";         // NOI18N

    public SVGTextField( SVGForm form, SVGLocatableElement element ) {
        super(form, element );

        SVGLocatableElement border = initNestedElements();
        verify( border );

        SVGRect outlineBox = border.getBBox();
        SVGRect textBox    = myTextElement.getBBox();

        float width = outlineBox.getWidth();
        float x = outlineBox.getX();

        initTextField( textBox,  width , x);
    }
    
    public SVGTextField( SVGForm form, SVGLocatableElement element ,
            float borderWidth , float xCoord )
    {
        super(form, element );
        
        initNestedElements();
        verify();

        SVGRect textBox = myTextElement.getBBox();
        
        initTextField( textBox,  borderWidth , xCoord );
            
    }

    public SVGTextField( SVGForm form, String elemId ) {
        this( form , (SVGLocatableElement) 
                form.getDocument().getElementById(elemId));
    }
    
    public String getTitle() {
        if ( myTitle == null ) {
            SVGLabel label = getLabel();
            if ( label != null ){
                setTitle( label.getText() );
            }
        }
        return myTitle;
    }
    
    public void setTitle( String title ){
        myTitle = title;
    }
    
    public String getText() {
        return myTextValue;
    }
    
    public void setText(String text) {
        if (text == null) {
            text = "";
        }
        if ( !text.equals(myTextValue)) {
            myTextValue = text;
            setTextImpl();
            fireActionPerformed();
        }    
    }

    public int getStartOffset() {
        return myStartOffset;
    }
    
    public void setStartOffset( int offset) {
        if ( myStartOffset != offset) {
            myStartOffset = offset;
            setTextImpl();
        }
    }
    
    public int getCaretPosition() {
        return myCaretPos;
    }
    
    public void setCaretPosition(int caretPos) {
        if (caretPos != myCaretPos && myCaretElement != null) {
            myCaretPos = caretPos;

            if (caretPos < myStartOffset) {
                setStartOffset(caretPos);
            } else if (caretPos > myEndOffset) {
                setStartOffset(myStartOffset + caretPos - myEndOffset);
            }

            float caretLoc = myTextElement.getFloatTrait(TRAIT_X);
            if (caretPos > 0) {
                String beforeCaret = myTextValue.substring(myStartOffset,
                        caretPos);
                caretLoc += getTextWidth(beforeCaret) + caretWidth;
            }
            setTraitSafely(myCaretElement, TRAIT_X, caretLoc);
        }

    }
    
    public void setCaretVisible( boolean isVisible) {
        showCaret(isVisible);
    }
    
    public void focusGained() {
        showCaret(true);
    }

    public void focusLost() {
        showCaret(false);
    }    
    
    public InputHandler getInputHandler() {
        if ( myTextInputHandler != null ){
            return myTextInputHandler;
        }
        return getForm().getNumPadInputHandler();
    }
    
    public boolean isEditable(){
        return !isReadOnly;
    }
    
    public void setEditable( boolean editable ){
        isReadOnly = !editable;
        if ( isReadOnly ){
            myTextInputHandler = new TextInputHandler( getForm().getDisplay() );
        }
        else {
            myTextInputHandler = null;
        }
    }
    
    protected SVGLocatableElement getHiddenTextElement() {
        return myHiddenTextElement;
    }

    private SVGLocatableElement initNestedElements() {

        SVGLocatableElement border = null;
        
        if ( getElement().getId() != null ) {
            myTextElement = (SVGLocatableElement) getElementById( getElement(), 
                getElement().getId()+ TEXT_SUFFIX );
            myCaretElement = (SVGLocatableElement) getElementById(getElement(),
                    getElement().getId()+ CARET_SUFFIX );
            border = (SVGLocatableElement) getElementById(getElement(),
                    getElement().getId()+ BORDER_SUFFIX );
        }
        
        if ( myTextElement == null ){ 
            myTextElement  = (SVGLocatableElement) getNestedElementByMeta(
                    getElement(), TYPE , TEXT );
        }
        
        if ( myCaretElement == null ){
            myCaretElement = (SVGLocatableElement) getNestedElementByMeta(
                    getElement(),TYPE , CARETELEM );
        }

        return border;
    }

    private void initTextField(SVGRect textBox, float width , float x ) {
        if (textBox != null && width >= 0 ) {
            elemWidth = (int) (width + 0.5f -(textBox.getX() - x) * 2);
        } else {
            elemWidth = 0;
        }
        addHiddenElement(form);
        if (myCaretElement != null) {
            SVGRect bBox = myCaretElement.getBBox();
            if (bBox != null) {
                caretWidth = bBox.getWidth() / 2;
            }
        }
        setCaretPosition(0);
        showCaret(false);
        readMeta();
        //myTextElement.setTrait(TRAIT_TEXT , getTextTrait()) ;
        setText(getTextTrait().trim());
    }
    
    private void verify() {
        /*
         *  Should we check meta information f.e. type of component here
         *  for preventing creation based on incorrect element ? 
         */
        // TODO : check type of element.
        
        if ( myTextElement == null ){
            throw new IllegalArgumentException("Element with id=" +
                    getElement().getId()+" couldn't be used for Text Field." +
                            " It doesn't have nested 'text' element. " +
                            "See javadoc for SVG snippet format");
        }
    }

    private void verify( SVGLocatableElement border) {
        verify();

        if ( border == null ){
            throw new IllegalArgumentException("Element with id=" +
                    getElement().getId()+" couldn't be used for Text Field." +
                            " It doesn't have nested border element. " +
                            "See javadoc for SVG snippet format");
        }
    }
    
    private void showCaret(final boolean showCaret) {
        if ( myCaretElement != null) {
            setTraitSafely( myCaretElement , TRAIT_VISIBILITY, 
                            showCaret ? TR_VALUE_VISIBLE : TR_VALUE_HIDDEN);
        }
    }   
    
    private void addHiddenElement( SVGForm form ) {
        myHiddenTextElement = (SVGLocatableElement) form.getDocument().
                createElementNS( SVG_NS, TEXT);
        myHiddenTextElement.setFloatTrait( TRAIT_X, 
                myTextElement.getFloatTrait(TRAIT_X));
        myHiddenTextElement.setFloatTrait( TRAIT_Y, 
                myTextElement.getFloatTrait(TRAIT_Y));
        myHiddenTextElement.setFloatTrait( TRAIT_FONT_SIZE, 
                myTextElement.getFloatTrait(TRAIT_FONT_SIZE));
        myHiddenTextElement.setTrait( TRAIT_FONT_FAMILY, 
                myTextElement.getTrait( TRAIT_FONT_FAMILY));
        myHiddenTextElement.setTrait( TRAIT_VISIBILITY, TR_VALUE_HIDDEN);
        
        getForm().invokeAndWaitSafely( new  Runnable () {
            public void run() {
                getElement().appendChild(myHiddenTextElement);
            }
        }
        );
        
        initRenderer( myTextElement );
        if ( isEmpiricInitialized() ){
            setTraitSafely( myHiddenTextElement , TRAIT_TEXT, TEXT);
            initRenderer(myHiddenTextElement);
        }
    }
    

    private void readMeta() {
        MetaData meta = new MetaData();
        meta.loadFromElement( getElement() );
        String editable = (String)meta.get( EDITABLE );
        
        isReadOnly = !Boolean.TRUE.toString().equals( editable );
        
        if ( isReadOnly ){
            myTextInputHandler = new TextInputHandler( getForm().getDisplay() );
        }
    }

    private void setTextImpl() {
        String text = myTextValue;
        if (myStartOffset > 0) {
            text = text.substring(myStartOffset);
        }

        text = truncateToShownText(text,elemWidth);
        
        setTextTrait(text);
        myEndOffset = myStartOffset + text.length();
    }

    private String getTextTrait() {
        return myTextElement.getTrait( TRAIT_TEXT);
    }
    
    private void setTextTrait( String text) {
        setTraitSafely( myTextElement, TRAIT_TEXT , text);
    }
    
    private SVGLocatableElement myTextElement;
    private SVGLocatableElement myCaretElement;
    private int                 elemWidth;
    private SVGLocatableElement myHiddenTextElement;
    
    private       String              myTextValue;
    private       int                 myStartOffset = 0;
    private       int                 myEndOffset   = 0;
    private       int                 myCaretPos = -1;
    private       float               caretWidth = 0;
    private       String              myTitle;
    
    private boolean isReadOnly;
    
    private InputHandler              myTextInputHandler;

}
