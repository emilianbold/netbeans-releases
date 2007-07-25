/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.schema.completion;

import javax.swing.ImageIcon;
import org.netbeans.modules.xml.axi.AbstractElement;
import org.netbeans.modules.xml.schema.completion.spi.CompletionContext;
import org.netbeans.modules.xml.schema.completion.CompletionPaintComponent.ElementPaintComponent;

/**
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class ElementResultItem extends CompletionResultItem {
    
    /**
     * Creates a new instance of ElementResultItem
     */
    public ElementResultItem(AbstractElement element, CompletionContext context) {
        super(element, context);
        replacementText = element.getName();
        icon = new ImageIcon(CompletionResultItem.class.
                getResource(ICON_LOCATION + ICON_ELEMENT));
    }
    
    /**
     * Creates a new instance of ElementResultItem
     */
    public ElementResultItem(AbstractElement element, String prefix, CompletionContext context) {
        super(element, context);        
        replacementText = prefix + ":" + element.getName();
        icon = new ImageIcon(CompletionResultItem.class.
                getResource(ICON_LOCATION + ICON_ELEMENT));
    }
        
    public String getItemText() {
        AbstractElement element = (AbstractElement)axiComponent;
        String cardinality = null;
        if(axiComponent.supportsCardinality() &&
           element.getMinOccurs() != null &&
           element.getMaxOccurs() != null) {
            cardinality = "["+element.getMinOccurs()+".."+element.getMaxOccurs()+"]";
        }
        displayText = getReplacementText();
        if(cardinality != null)
            displayText = displayText + " " + cardinality;
        
        return displayText;
    }
    
    public CompletionPaintComponent getPaintComponent() {
        if(component == null) {
            component = new ElementPaintComponent(this);
        }
        return component;
    }

}
