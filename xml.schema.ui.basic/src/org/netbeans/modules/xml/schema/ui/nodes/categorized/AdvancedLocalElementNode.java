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

package org.netbeans.modules.xml.schema.ui.nodes.categorized;

import java.beans.PropertyChangeEvent;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.ui.nodes.NewTypesFactory;
import org.netbeans.modules.xml.schema.ui.nodes.RefreshableChildren;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaUIContext;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.customizer.AdvancedLocalElementCustomizer;
import org.netbeans.modules.xml.schema.ui.nodes.schema.LocalElementNode;
import org.netbeans.modules.xml.xam.ui.customizer.Customizer;
import org.netbeans.modules.xml.xam.ui.customizer.CustomizerProvider;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;

/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class AdvancedLocalElementNode extends LocalElementNode {
    /**
     *
     *
     */
    public AdvancedLocalElementNode(SchemaUIContext context,
            SchemaComponentReference<LocalElement> reference,
            Children children) {
        super(context,reference,children);
    }
    
    
    /**
     *
     *
     */
    @Override
    public String getHtmlDisplayName() {
        LocalElement element=getReference().get();
        
        String max=element.getMaxOccursEffective();
        if (max.equals("unbounded"))
            max="*";
        
        String decoration="["+element.getMinOccursEffective()+".."+max+"]";
        if(element.getType()!=null && element.getType().get()!=null) {
            String supertypeLabel = NbBundle.getMessage(
                    AdvancedLocalElementNode.class, "LBL_InstanceOf",
                    element.getType().get().getName());
            decoration = decoration+" ("+supertypeLabel+")";
        }
        String name = getDefaultDisplayName()+" <font color='#999999'>"+decoration+"</font>";
        return applyHighlights(name);
    }
    
    
    /**
     *
     *
     */
    protected NewTypesFactory getNewTypesFactory() {
        return new AdvancedNewTypesFactory();
    }
    
    @Override
    public boolean hasCustomizer() {
        return isEditable();
    }
    
    public CustomizerProvider getCustomizerProvider() {
        return new CustomizerProvider() {
            
            public Customizer getCustomizer() {
                return new AdvancedLocalElementCustomizer(getReference());
            }
        };
    }
    
    public void propertyChange(PropertyChangeEvent event) {
        if(!isValid()) return;
        super.propertyChange(event);
        String property = event.getPropertyName();
        if(event.getSource() == getReference().get()) {
            if(LocalElement.TYPE_PROPERTY.equals(property)) {
                ((RefreshableChildren)getChildren()).refreshChildren();
                fireDisplayNameChange(null,getDisplayName());
            }
            if(LocalElement.MIN_OCCURS_PROPERTY.equals(property) ||
                    LocalElement.MAX_OCCURS_PROPERTY.equals(property)) {
                fireDisplayNameChange(null,getDisplayName());
            }
        }
    }
    
}
