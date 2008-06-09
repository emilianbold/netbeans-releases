
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
import org.w3c.dom.svg.SVGRGBColor;

/**
 *
 * @author Pavel Benes
 */
public class SVGButton extends SVGAbstractButton {
    private static final String BODYELEM_SUFFIX       = "_body";
    private static final String TEXTELEM_SUFFIX       = "_text";
    private static final String SHADOWTEXTELEM_SUFFIX = "_stext";
    
    private final SVGLocatableElement bodyElement;
    private       SVGRGBColor         bodyColor;
    private       boolean             isSelected = false;
    
    public SVGButton( SVGForm form, String elemId) {
        super(form, elemId);
        bodyElement = (SVGLocatableElement) getElementById( wrapperElement, elemId + BODYELEM_SUFFIX);
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
