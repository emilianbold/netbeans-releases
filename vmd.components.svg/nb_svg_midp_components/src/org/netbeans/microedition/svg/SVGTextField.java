/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt. 
  * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */ 

/*
 * SVGTextField.java
 * 
 * Created on Oct 2, 2007, 4:27:06 PM
 */
package org.netbeans.microedition.svg;

import org.netbeans.microedition.svg.input.InputHandler;
import org.w3c.dom.svg.SVGLocatableElement;
import org.w3c.dom.svg.SVGRect;

/**
 * Suggested svg snippet :
 * <pre>
 * &lt;g id="textfield_name" transform="translate(20,40)">
 *       &lt;!-- Metadata information. Please don't edit. -->
 *   &lt;text display="none">type=textfield&lt;/text>
 *       &lt;text display="none">readOnly="false" enabled="true"&lt;/text>
 *
 *       &lt;rect x="0" y="0" rx="5" ry="5" width="200" height="30" fill="none" stroke="black" stroke-width="2">
 *           &lt;animate attributeName="stroke" attributeType="XML" begin="textfield_name.focusin" dur="0.25s" fill="freeze" to="rgb(255,165,0)"/>
 *           &lt;animate attributeName="stroke" attributeType="XML" begin="textfield_name.focusout" dur="0.25s" fill="freeze" to="black"/>
 *       &lt;/rect>
 *       &lt;text  x="10" y="23" stroke="black" font-size="20" font-family="SunSansSemiBold">John Hilsworths
 *       &lt;!-- Metadata information. Please don't edit. -->
 *       &lt;text display="none">type=text&lt;/text>
 *       &lt;/text>
 *   &lt;g>
 *           &lt;!-- Metadata information. Please don't edit. -->
 *           &lt;text display="none">type=caret&lt;/text>
 *
 *           &lt;rect visibility="visible" x="20" y="4" width="3" height="22" fill="black" stroke="black"/>
 *       &lt;/g>
 *   &lt;/g
 * </pre>
 * @author Pavel Benes
 * @author ads
 */
public class SVGTextField extends SVGComponent {

    protected static final String TRAIT_FONT_FAMILY = "font-family";      // NOI18N
    protected static final String TEXT              = "text";             // NOI18N
    private static final String CARETELEM           = "caret";            // NOI18N
    protected static final String TRAIT_FONT_SIZE   = "font-size";        // NOI18N
    
    public SVGTextField( SVGForm form, SVGLocatableElement element ) {
        super(form, element );
        textElement  = (SVGLocatableElement) getElementByMeta(getElement(), 
                TYPE , TEXT );
        caretElement = (SVGLocatableElement) getNestedElementByMeta(getElement(), 
                TYPE , CARETELEM );

        SVGRect outlineBox = wrapperElement.getBBox();
        SVGRect textBox    = textElement.getBBox();
        
        if (textBox != null) {
            System.out.println("Text width: " + textBox.getWidth());
            elemWidth = (int) (outlineBox.getWidth() + 0.5f - (textBox.getX() - outlineBox.getX()) * 2);
        } else {
            elemWidth = 0;
        }

        hiddenTextElement = (SVGLocatableElement) form.getDocument().
                createElementNS( SVG_NS, TEXT);
        hiddenTextElement.setFloatTrait( TRAIT_X, 
                textElement.getFloatTrait(TRAIT_X));
        hiddenTextElement.setFloatTrait( TRAIT_Y, 
                textElement.getFloatTrait(TRAIT_Y));
        hiddenTextElement.setFloatTrait( TRAIT_FONT_SIZE, 
                textElement.getFloatTrait(TRAIT_FONT_SIZE));
        hiddenTextElement.setTrait( TRAIT_FONT_FAMILY, 
                textElement.getTrait( TRAIT_FONT_FAMILY));
        hiddenTextElement.setTrait( TRAIT_VISIBILITY, TR_VALUE_HIDDEN);
        
        getForm().invokeAndWaitSafely( new  Runnable () {
            public void run() {
                getElement().appendChild(hiddenTextElement);
            }
        }
        );
        
        if (caretElement != null) {
            SVGRect bBox = caretElement.getBBox();
            if ( bBox != null) {
                caretWidth = bBox.getWidth() / 2;
            }
        }
        setCaretPosition(0);
        showCaret( false);
        setText( getTextTrait());
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
        return textValue;
    }
    
    public void setText(String text) {
        if ( !text.equals(this.textValue)) {
            this.textValue = text;
            setTextImpl();
        }    
    }

    public int getStartOffset() {
        return startOffset;
    }
    
    public void setStartOffset( int offset) {
        if ( this.startOffset != offset) {
            this.startOffset = offset;
            setTextImpl();
        }
    }
    
    public int getCaretPosition() {
        return caretPos;
    }
    
    public void setCaretPosition(int caretPos) {
        if (caretPos != this.caretPos) {
            this.caretPos = caretPos;

            if (caretPos < startOffset) {
                setStartOffset(caretPos);
            } else if (caretPos > endOffset) {
                setStartOffset(startOffset + caretPos - endOffset);
            }
            
            if (caretElement != null) {
                float caretLoc = textElement.getFloatTrait(TRAIT_X);
                if ( caretPos > 0) {
                    String beforeCaret = textValue.substring(startOffset, caretPos);
                    caretLoc += getTextWidth(beforeCaret) + caretWidth;
                }
                setTraitSafely(caretElement , TRAIT_X, caretLoc);
            }
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
        return form.getNumPadInputHandler();
    }
    
    private void showCaret(final boolean showCaret) {
        if ( caretElement != null) {
            setTraitSafely( caretElement , TRAIT_VISIBILITY, 
                            showCaret ? TR_VALUE_VISIBLE : TR_VALUE_HIDDEN);
        }
    }   

    /*
     * TODO : this is very non-efficient way to compute text width.
     * Need somehow to improve it. 
     */
    private float getTextWidth(String text) {
        if ( text.endsWith(" ")) {
            return getTextWidthImpl( text + "i") - getTextWidthImpl("i");
        } else {
            return getTextWidthImpl(text);
        }
    }
    
    private float getTextWidthImpl(String text) {
        float width = 0;
        if (text.length() > 0) {
            setTraitSafely( hiddenTextElement , TRAIT_TEXT, text);
            SVGRect bBox = hiddenTextElement.getBBox();
            if ( bBox != null) {
                width = bBox.getWidth();
            } else {
                System.out.println("Error: Null BBox #1");
            }
        }
        return width;
    }
    
    private void setTextImpl() {
        String text = this.textValue;
        if (startOffset > 0) {
            text = text.substring(startOffset);
        }

        while ( getTextWidth(text) > elemWidth) {
            text = text.substring(0, text.length() - 1);
        }
        
        setTextTrait(text);
        endOffset = startOffset + text.length();
    }

    private String getTextTrait() {
        return textElement.getTrait( TRAIT_TEXT);
    }
    
    private void setTextTrait( String text) {
        setTraitSafely( textElement, TRAIT_TEXT , text);
    }
    
    private final SVGLocatableElement textElement;
    private final SVGLocatableElement caretElement;
    private final SVGLocatableElement hiddenTextElement;
    private final int                 elemWidth;
    
    private       String              textValue;
    private       int                 startOffset = 0;
    private       int                 endOffset   = 0;
    private       int                 caretPos = -1;
    private       float               caretWidth = 0;
    private       String              myTitle;

}
