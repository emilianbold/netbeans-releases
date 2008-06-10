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
 *
 * @author Pavel Benes
 */
public class SVGCheckBox extends SVGAbstractButton {
    private static final String MARKELEM_SUFFIX = "_mark";
    
    private final SVGLocatableElement markElement;
    private       boolean             isSelected;
    
    public SVGCheckBox( SVGForm form, String elemId) {
        super(form, elemId);
        markElement = (SVGLocatableElement) getElementById( wrapperElement, elemId + MARKELEM_SUFFIX);
        markElement.setTrait(TRAIT_VISIBILITY, "hidden");
        this.isSelected = false;
    }
    
    public void setSelected( boolean isSelected) {
        if ( this.isSelected != isSelected) {
            markElement.setTrait(TRAIT_VISIBILITY, isSelected ? "visible" : "hidden");
            this.isSelected = isSelected;
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
}
