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
 * Suggested SVG snippet :
 * <pre>
 * &lt;g id="checkbox_online" transform="translate(130,220)">
 *      &lt;!-- Metadata information. Please don't edit. -->
 *      &lt;text display="none">type=checkbox&lt;/text>
 *
 *      &lt;rect x="0" y="0" rx="5" ry="5" width="200" height="30" fill="none" stroke="rgb(255,165,0)" stroke-width="2" visibility="hidden">
 *          &lt;set attributeName="visibility" attributeType="XML" begin="checkbox_online.focusin" fill="freeze" to="visible"/>
 *          &lt;set attributeName="visibility" attributeType="XML" begin="checkbox_online.focusout" fill="freeze" to="hidden"/>
 *      &lt;/rect>
 *      &lt;rect x="7.4" y="5" width="20" height="20" fill="white" stroke="black" stroke-width="2"/>
 *      &lt;path  transform="translate(12.5,15.5)"  d="M 0,0 5,5 5,-12" fill="none" stroke="black" stroke-width="2">
 *          &lt;!-- Metadata information. Please don't edit. -->
 *          &lt;text display="none">type=mark&lt;/text>
 *      &lt;/path>
 *      &lt;text  x="33.8" y="21.6" stroke="gray" font-size="15">
 *          &lt;!-- Metadata information. Please don't edit. -->
 *          &lt;text display="none">type=text&lt;/text>
 *      Online&lt;/text>
 *  &lt;/g
 * </pre>
 * @author Pavel Benes
 * @author ads
 */
public class SVGCheckBox extends SVGAbstractButton {
    private static final String MARK = "mark";              // NOI18N
    
    public SVGCheckBox( SVGForm form, String elemId) {
        super(form, elemId);
        myMarkElement = (SVGLocatableElement) getElementByMeta( getElement(), 
                TYPE, MARK );
        setTraitSafely( myMarkElement , TRAIT_VISIBILITY, TR_VALUE_HIDDEN );
        isSelected = false;
    }
    
    public void setSelected( boolean selected) {
        if ( isSelected != selected) {
            setTraitSafely( myMarkElement , TRAIT_VISIBILITY, selected ? 
                    TR_VALUE_VISIBLE : TR_VALUE_HIDDEN);
            isSelected = selected;
            fireActionPerformed();
        }
    }
    
    public boolean isSelected() {
        return isSelected;
    }
         
    public void pressButton() { 
        form.activate(this);
        setSelected( !isSelected);
    }
    
    private final SVGLocatableElement myMarkElement;
    private       boolean             isSelected;
}
