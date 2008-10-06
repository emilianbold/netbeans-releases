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
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGLocatableElement;
import org.w3c.dom.svg.SVGRect;


/**
 * Suggested SVG snippet :
 * <pre>
 * &lt;g id="text_area" transform="translate(130,220)">
 *       &lt;rect x="0" y="-5" rx="5" ry="5" width="100" height="90" fill="none" stroke="rgb(255,165,0)" stroke-width="2" visibility="hidden">
 *           &lt;set attributeName="visibility" attributeType="XML" begin="text_area.focusin" fill="freeze" to="visible"/>
 *           &lt;set attributeName="visibility" attributeType="XML" begin="text_area.focusout" fill="freeze" to="hidden"/>
 *       &lt;/rect>
 *       &lt;rect  x="5.0" y="0.0" width="90" height="80" fill="none" stroke="black" stroke-width="2"/>
 *   &lt;text id="text_area_hidden_text" visibility="hidden" x="10" y="15" stroke="black" font-size="15" font-family="SunSansSemiBold">
 *       HIDDEN TEXT
 *   &lt;/text>
 *       &lt;rect id="text_area_caret" visibility="visible" x="10" y="2" width="2" height="15" fill="black" stroke="black"/>
 *   &lt;/g>
 * </pre>
 * @author ads
 * @deprecated
 *
 */
public class SVGTextArea extends SVGComponent {
    
    private final static String HIDDEN_SUFFIX = "_hidden_text";
    private static final String CARET_SUFFIX  = "_caret";
    
    private final static char LINE_BREAK ='\n';

    public SVGTextArea( SVGForm form, String elemId ) {
        super(form, elemId);
        
        myHiddenText = (SVGLocatableElement)getElementById( 
                wrapperElement ,  elemId + HIDDEN_SUFFIX );
        myCaret = (SVGLocatableElement) getElementById( wrapperElement, 
                elemId + CARET_SUFFIX);
        
        myHandler = new TextAreaInputHandler( );
        
        SVGRect outlineBox = wrapperElement.getBBox();
        SVGRect textBox    = myHiddenText.getBBox();

        if (textBox != null) {
            myLineWidth = (int) (outlineBox.getWidth() + 0.5f - 
                    (textBox.getX() - outlineBox.getX()) * 2);
        } else {
            myLineWidth = 0;
        }
        
        myLineHeight = myHiddenText.getBBox().getHeight();
        myFirstLineY = myHiddenText.getBBox().getY() + myLineHeight;
        if (myCaret != null) {
            SVGRect bBox = myCaret.getBBox();
            if ( bBox != null) {
                myCaretWidth = bBox.getWidth() / 2;
            }
        }
        
        setCaretPosition(0);
        showCaret( false );
    }
    
    public InputHandler getInputHandler() {
        return myHandler;
    }
    
    public String getText() {
        if ( myLines == null ){
            return "";
        }
        StringBuffer buffer = new StringBuffer();
        Enumeration en = myLines.elements();
        while ( en.hasMoreElements() ){
            String line = (String)en.nextElement();
            buffer.append( line );
            buffer.append( LINE_BREAK );
        }
        if ( buffer.length() > 0){
            return buffer.toString().substring( 0 ,  buffer.length() -1 );
        }
        return buffer.toString();
    }
    
    public void setText(String text) {
        if (text == null) {
            text = "";
        }
        StringBuffer buffer = new StringBuffer();
        char ch = ' ';
        myLines = new Vector();
        for ( int i=0; i<text.length() ; i++ ){
            ch = text.charAt( i );
            if ( ch == LINE_BREAK ){
                myLines.addElement( buffer.toString() );
                buffer = new StringBuffer();
            }
            else {
                buffer.append( ch );
            }
        }
        if ( ch == LINE_BREAK ){
            myLines.addElement( "" );
        }
        else {
            myLines.addElement( buffer.toString() );
        }
        doSetText();
    }
    
    public void setCaretVisible( boolean isVisible ) {
        showCaret(isVisible);        
    }
    
    public void focusGained() {
        showCaret(true);
    }

    public void focusLost() {
        showCaret(false);
    }   
    
    public void setCaretPosition(int caretPos) {
        if (caretPos != myCaretPosition) {
            myCaretPosition = caretPos;

            int endOffset;
            if ( myEndOffsets == null ){
                endOffset = 0;
            }
            else {
                endOffset = 
                    ((Integer)myEndOffsets.elementAt( myCurrentLine )).intValue();
            }
            if (caretPos < myStartOffset) {
                setStartOffset(caretPos);
            } else if (caretPos > endOffset) {
                setStartOffset(myStartOffset + caretPos - endOffset);
            }
            
            if (myCaret != null) {
                float caretLoc = myHiddenText.getFloatTrait(TRAIT_X);
                if ( caretPos > 0) {
                    String beforeCaret = ((String)myLines.elementAt(myCurrentLine)).
                        substring(myStartOffset, caretPos);
                    caretLoc += getTextWidth(beforeCaret) + myCaretWidth;
                }
                myCaret.setFloatTrait(TRAIT_X, caretLoc);
            }
        }
    }
    
    public void setStartOffset( int offset) {
        if ( myStartOffset != offset) {
            myStartOffset = offset;
            doSetText();
        }
    }
    
    public int getCaretPosition() {
        return myCaretPosition;
    }
    
    private void showCaret(final boolean showCaret) {
        if ( myCaret != null) {
            form.invokeLaterSafely(new Runnable() {
               public void run() {
                    myCaret.setTrait(TRAIT_VISIBILITY, showCaret ? "visible" : "hidden");
               }
            });
        }
    }   
    
    private void doSetText() {
        if ( myLines == null ){
            return;
        }
        cleanText();
        myEndOffsets = new Vector();
        Enumeration en = myLines.elements();
        float y = myFirstLineY;
        while ( en.hasMoreElements() ){
            String text = (String)en.nextElement();
            if ( myStartOffset > 0) {
                text = text.substring(myStartOffset);
            }

            while ( getTextWidth(text) > myLineWidth) {
                text = text.substring(0, text.length() - 1);
            }
        
            addText( text ,y );
            y+=myLineHeight;
            Integer endOffset = new Integer( myStartOffset + text.length());
            myEndOffsets.addElement( endOffset );
        }
    }

    private void addText( String text , float y) {
        SVGLocatableElement textElement = (SVGLocatableElement) form.
            getDocument().createElementNS( SVGComponent.SVG_NS, "text");
        textElement.setFloatTrait( SVGComponent.TRAIT_X, 
                myHiddenText.getBBox().getX() );
        textElement.setFloatTrait( SVGComponent.TRAIT_Y, y ) ;
        textElement.setFloatTrait( "font-size", 
                myHiddenText.getFloatTrait("font-size"));
        textElement.setTrait( "font-family", myHiddenText.getTrait("font-family"));
        textElement.setTrait( TRAIT_VISIBILITY, "inherit");
        if ( text == null ){
            textElement.setTrait( "#text","");
        }
        else {
            textElement.setTrait("#text",  text );
        }
        wrapperElement.appendChild(textElement);        
    }

    private void cleanText() {
        Element child = wrapperElement.getFirstElementChild();
        while ( child!= null ){
            if ( !(child instanceof SVGElement )){
                return;
            }
            SVGElement svgElement = (SVGElement) child;
            Element next = svgElement.getNextElementSibling();
            if ( "text".equals(child.getLocalName()) 
                    && svgElement.getId() == null )
            {
                wrapperElement.removeChild( child );
            }
            child = next;
        }
    }
    
    private float getTextWidth(String text) {
        float ret;
        if ( text.endsWith(" ")) {
            ret =  computeTextWidth( text + "i") - computeTextWidth("i");
        } else {
            ret = computeTextWidth(text);
        }
        return ret;
    }
    
    /*
     * TODO : this is very non-efficient way to compute text width.
     * Need somehow to improve it. 
     */
    private float computeTextWidth(String text) {
        float width = 0;
        if (text.length() > 0) {
            myHiddenText.setTrait( "#text", text);
            SVGRect bBox = myHiddenText.getBBox();
            if ( bBox != null) {
                width = bBox.getWidth();
            } else {
                //System.out.println("Error: Null BBox #1");
            }
        }
        return width;
    }

    class TextAreaInputHandler extends NumPadInputHandler {

        public TextAreaInputHandler(  ) {
            super(form.getDisplay());
        }
        
        protected int getCaretPosition( SVGComponent comp ) {
            return SVGTextArea.this.getCaretPosition();
        }

        protected String getText( SVGComponent comp ) {
            return SVGTextArea.this.getText();
        }

        protected void setCaretPosition( SVGComponent comp, int position ) {
            SVGTextArea.this.setCaretPosition( position );
        }

        protected void setText( SVGComponent comp, String text ) {
            SVGTextArea.this.setText( text );
        }
    }
    
    private InputHandler myHandler;
    
    private SVGLocatableElement myHiddenText;
    
    private Vector myLines;
    private Vector myEndOffsets;
    
    private int myStartOffset = 0;
    
    private int myLineWidth;
    
    private int myCaretPosition=-1;
    private int myCurrentLine;
    
    private float myLineHeight;
    private float myFirstLineY;
    private final SVGLocatableElement myCaret;
    private float myCaretWidth;
    

}
