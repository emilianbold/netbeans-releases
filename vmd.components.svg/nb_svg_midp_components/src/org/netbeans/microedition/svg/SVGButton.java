
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

import org.w3c.dom.svg.SVGRGBColor;

/**
 * Suggested SVG snippet:
 * <pre>
 * &lt;g transform="translate(130,276)" id="button_ok">
 *       &lt;metadata> &lt;text>type=button&lt;/text> &lt;/metadata>
 *       &lt;rect x="-2" y="-2" rx="5" ry="5" width="80" height="30" fill="white"/>
 *       &lt;rect x="1" y="1" rx="5" ry="5" width="81" height="31" fill="rgb(0,0,128)"/>
 *   &lt;rect transform="matrix(1.060988,0.003826782,-0.003826782,1.060988,4.617886,1.9321077)"   
 *       x="0" y="0" rx="5" ry="5" width="80" height="30" fill="rgb(176,196,222)" stroke="rgb(255,165,0)" stroke-width="0">
 *       &lt;metadata> &lt;text>type=body&lt;/text> &lt;/metadata>
 *           &lt;animate attributeName="stroke-width" attributeType="XML" begin="button_ok.focusin" dur="0.25s" fill="freeze" to="2"/>
 *           &lt;animate attributeName="stroke-width" attributeType="XML" begin="button_ok.focusout" dur="0.25s" fill="freeze" to="0"/>
 *           &lt;animate attributeName="fill" attributeType="XML" begin="indefinite" dur="0.25s" fill="freeze" to="rgb(156,176,202)">
 *       &lt;metadata> &lt;text>type=pressed&lt;/text> &lt;/metadata>
 *       &lt;/animate>
 *           &lt;animate attributeName="fill" attributeType="XML" begin="indefinite" dur="0.25s" fill="freeze" to="rgb(176,196,222)">
 *       &lt;metadata> &lt;text>type=released&lt;/text> &lt;/metadata>
 *       &lt;/animate>
 *       &lt;/rect>
 *   &lt;text x="24" y="23" fill="black" font-size="20">
 *       &lt;metadata> &lt;text>type=shadow_text&lt;/text> &lt;/metadata>
 *       OK&lt;/text>
 *       &lt;text x="23" y="21" fill="gray" font-size="20">
 *       &lt;metadata> &lt;text>type=text&lt;/text> &lt;/metadata>
 *       OK&lt;/text>
 *   &lt;/g>
 * </pre>
 * @author Pavel Benes
 * @author ads
 */
public class SVGButton extends SVGAbstractButton {
    private static final String TEXT       = "text";            // NOI18N
    private static final String SHADOW     = "shadow_text";     // NOI18N
    
    private       SVGRGBColor         bodyColor;
    private       boolean             isSelected = false;
    
    public SVGButton( SVGForm form, String elemId) {
        super(form, elemId);
    }
        
    public void pressButton() { 
        /*
        form.activate(this);
        if (bodyElement != null) {
            bodyColor = bodyElement.getRGBColorTrait(TRAIT_FILL);
            SVGColor color = new SVGColor(bodyColor);
            color.darken();
            //TODO Perseus API required here !!??!!
            bodyElement.setRGBColorTrait(TRAIT_FILL, color);
        }
         */
        setSelected(true);
        super.pressButton();
    }
    
    public void releaseButton() {
        /*
        if (bodyElement != null && bodyColor != null) {
            bodyElement.setRGBColorTrait(TRAIT_FILL, bodyColor);
        }
        bodyColor = null;
         */
        setSelected(false);
        super.releaseButton();
    }
    
    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        if ( this.isSelected != isSelected) {
            this.isSelected = isSelected;
            fireActionPerformed();
        }
    }
}
