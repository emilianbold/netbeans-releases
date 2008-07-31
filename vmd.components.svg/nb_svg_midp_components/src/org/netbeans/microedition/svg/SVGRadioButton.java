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
 * SVGButton.java
 * 
 * Created on Oct 4, 2007, 1:56:11 PM
 */
package org.netbeans.microedition.svg;

import org.w3c.dom.svg.SVGLocatableElement;

/**
 * Suggested SVG snippet:
 * <pre>
 * &lt;g transform="translate(20,190)">
 *       &lt;rect x="0" y="0" rx="5" ry="5" width="200" height="60" fill="none" stroke="#85a6cf" stroke-width="2"/>
 *       &lt;g id="radio_male" transform="translate(7,5)">
 *       &lt;!-- Metadata information. Please don't edit. -->
 *       &lt;text display="none">type=radio&lt;/text>
 *
 *           &lt;rect x="0" y="0" rx="5" ry="5" width="185" height="24" fill="none" stroke="rgb(255,165,0)" stroke-width="2" visibility="hidden">
 *               &lt;set attributeName="visibility" attributeType="XML" begin="radio_male.focusin" fill="freeze" to="visible"/>
 *               &lt;set attributeName="visibility" attributeType="XML" begin="radio_male.focusout" fill="freeze" to="hidden"/>
 *           &lt;/rect>
 *           &lt;circle id="radio_male_dot" cx="11" cy="12" r="8" fill="white" stroke="black" stroke-width="2"/>
 *       &lt;g>
 *           &lt;text display="none">type=dot&lt;/text>
 *           &lt;circle cx="11" cy="12" r="4" fill="black" visibility="hidden"/>
 *       &lt;g    
 *          &lt;text id="radio_male_text" x="24" y="17" stroke="gray" font-size="15">
 *          Male&lt;/text>
 *          &lt;!-- Metadata information. Please don't edit. -->
 *          &lt;text display="none">type=text&lt;/text>
 *       &lt;/g>
 *
 *       &lt;g id="radio_female" transform="translate(7,33)">
 *       &lt;!-- Metadata information. Please don't edit. -->
 *       &lt;text display="none">type=radio&lt;/text>
 *
 *           &lt;rect x="0" y="0" rx="5" ry="5" width="185" height="24" fill="none" stroke="rgb(255,165,0)" stroke-width="2" visibility="hidden">
 *               &lt;set attributeName="visibility" attributeType="XML" begin="radio_female.focusin" fill="freeze" to="visible"/>
 *               &lt;set attributeName="visibility" attributeType="XML" begin="radio_female.focusout" fill="freeze" to="hidden"/>
 *           &lt;/rect>
 *           &lt;circle transform="translate(11, 12)" cx="0" cy="0" r="8" fill="white" stroke="black" stroke-width="2"/>
 *       &lt;g>
 *           &lt;!-- Metadata information. Please don't edit. -->
 *       &lt;text display="none">type=dot&lt;/text>
 *
 *       &lt;circle id="radio_female_dot" cx="11" cy="12" r="4" fill="black" visibility="hidden"/>
 *       &lt;g>
 *          &lt;text x="24" y="17" stroke="gray" font-size="15">
 *          Female&lt;/text>
 *          &lt;!-- Metadata information. Please don't edit. -->
 *          &lt;text display="none">type=text&lt;/text>
 *       &lt/g>
 *   &lt;/g>
 * </pre>
 *
 * @author Pavel Benes
 * @author ads
 */
public class SVGRadioButton extends SVGAbstractButton {
    
    private static final String DOT         = "dot";           // NOI18N
    private static final String DOT_SUFFIX  = DASH+DOT;        // NOI18N 
    
    private static final String TEXT        = "text";          // NOI18N
    private static final String TEXT_SUFFIX = DASH +TEXT;
    
    public SVGRadioButton( SVGForm form, String elemId) {
        super(form, elemId);
        initNestedElements();
        
        //isSelected = form.registerRadioButton(this);
        updateTrait();
    }

    /**
     * 
     */
    private void initNestedElements() {
        if ( getElement().getId() != null ){
            myDotElement = (SVGLocatableElement) getElementById( getElement(), 
                    getElement().getId() + DOT_SUFFIX );
            myTextElement = (SVGLocatableElement) getElementById( getElement(), 
                    getElement().getId() + TEXT_SUFFIX );
        }
        if ( myDotElement == null ){
            myDotElement = (SVGLocatableElement) getNestedElementByMeta( getElement(), 
                    TYPE, DOT );
        }
        if ( myTextElement == null ){
            myTextElement = (SVGLocatableElement) getNestedElementByMeta( getElement(), 
                    TYPE, TEXT );
        }
    }
    
    public void setSelected( boolean selected) {
        if ( isSelected != selected) {
            isSelected = selected;
            updateTrait();
            fireActionPerformed();
        }
    }
    
    public boolean isSelected() {
        return isSelected;
    }
         
    public void pressButton() { 
        if ( !isSelected) {
            form.activate(this);
            setSelected( true);
        }
    }
    
    public String getText(){
        return myTextElement.getTrait( TRAIT_TEXT );
    }
    
    public void setText( String text ){
        setTraitSafely( myTextElement, TRAIT_TEXT, text);
    }
    
    private void updateTrait() {
        setTraitSafely(myDotElement , TRAIT_VISIBILITY, 
                isSelected ? TR_VALUE_VISIBLE : TR_VALUE_HIDDEN );
    }
    
    private SVGLocatableElement myDotElement;
    private SVGLocatableElement myTextElement;
    private       boolean             isSelected;
}
