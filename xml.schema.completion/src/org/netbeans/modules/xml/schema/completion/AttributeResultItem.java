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
import org.netbeans.modules.xml.axi.AbstractAttribute;
import org.netbeans.modules.xml.schema.completion.spi.CompletionContext;
import org.netbeans.modules.xml.schema.completion.CompletionPaintComponent.AttributePaintComponent;

/**
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class AttributeResultItem extends CompletionResultItem {
    
    /**
     * Creates a new instance of AttributeResultItem
     */
    public AttributeResultItem(AbstractAttribute attribute, CompletionContext context) {
        super(attribute, context);
        replacementText = attribute.getName();
        icon = new ImageIcon(CompletionResultItem.class.
                getResource(ICON_LOCATION + ICON_ATTRIBUTE));
    }
    
    /**
     * Creates a new instance of AttributeResultItem
     */
    public AttributeResultItem(AbstractAttribute attribute, String prefix, CompletionContext context) {
        super(attribute, context);
        replacementText = prefix + ":" + attribute.getName();
        icon = new ImageIcon(CompletionResultItem.class.
                getResource(ICON_LOCATION + ICON_ATTRIBUTE));
    }
    
    /**
     * Overwrites getReplacementText of base class.
     */
    public String getReplacementText(){
        return replacementText+"=\"\"";
    }
    
    public String getItemText() {
        displayText = replacementText;        
        return displayText;
    }    
    
    public CompletionPaintComponent getPaintComponent() {
        if(component == null) {
            component = new AttributePaintComponent(this);
        }
        return component;
    }

}
