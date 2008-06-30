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
 *
 * @author Pavel Benes
 */
public class SVGTextField extends SVGComponent {
    private static final String TEXTELEM_SUFFIX   = "_text";
    private static final String CARETELEM_SUFFIX  = "_caret";
    private static final String TITLEELEM_SUFFIX  = "_title";
    private static final String TRAIT_TEXT        = "#text";
    private static final String TRAIT_FONT_SIZE   = "font-size";
    
    private final SVGLocatableElement textElement;
    private final SVGLocatableElement caretElement;
    private final SVGLocatableElement hiddenTextElement;
    private final int                 elemWidth;
    
    private       String              textValue;
    private       int                 startOffset = 0;
    private       int                 endOffset   = 0;
    private       int                 caretPos = -1;
    private       float               caretWidth = 0;

    public SVGTextField( SVGForm form, String elemId) {
        super(form, elemId);
        textElement  = (SVGLocatableElement) getElementById( wrapperElement, elemId + TEXTELEM_SUFFIX);
        caretElement = (SVGLocatableElement) getElementById( wrapperElement, elemId + CARETELEM_SUFFIX);
        SVGLocatableElement telem = (SVGLocatableElement) form.getSVGElementById("textfield_name_title");

        SVGRect outlineBox = wrapperElement.getBBox();
        SVGRect textBox    = textElement.getBBox();
        System.out.println("outline: " + outlineBox);
        System.out.println("text: " + textBox);

        if (textBox != null) {
            String t = textElement.getTrait(TRAIT_TEXT);
            System.out.println("Text width: " + textBox.getWidth());
            elemWidth = (int) (outlineBox.getWidth() + 0.5f - (textBox.getX() - outlineBox.getX()) * 2);
        } else {
            elemWidth = 0;
        }

        hiddenTextElement = (SVGLocatableElement) form.getDocument().createElementNS( SVG_NS, "text");
        hiddenTextElement.setFloatTrait( TRAIT_X, textElement.getFloatTrait(TRAIT_X));
        hiddenTextElement.setFloatTrait( TRAIT_Y, textElement.getFloatTrait(TRAIT_Y));
        hiddenTextElement.setFloatTrait( TRAIT_FONT_SIZE, textElement.getFloatTrait(TRAIT_FONT_SIZE));
        hiddenTextElement.setTrait( "font-family", "SunSansSemiBold");
        hiddenTextElement.setTrait( TRAIT_VISIBILITY, "visible");
        wrapperElement.appendChild(hiddenTextElement);
        
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
    
    public String getTitle() {
        SVGLocatableElement titleElement = form.getSVGLocatableElementById(elemId + TITLEELEM_SUFFIX);
        return titleElement != null ? titleElement.getTrait( TRAIT_TEXT) : null;
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
                caretElement.setFloatTrait(TRAIT_X, caretLoc);
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
            form.invokeAndWaitSafely(new Runnable() {
               public void run() {
                    caretElement.setTrait(TRAIT_VISIBILITY, showCaret ? "visible" : "hidden");
               }
            });
        }
    }   

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
            hiddenTextElement.setTrait( TRAIT_TEXT, text);
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
        textElement.setTrait( TRAIT_TEXT, text);
    }
}
