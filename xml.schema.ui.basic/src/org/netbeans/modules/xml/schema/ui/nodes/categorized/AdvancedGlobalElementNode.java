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
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.ui.nodes.NewTypesFactory;
import org.netbeans.modules.xml.schema.ui.nodes.RefreshableChildren;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaUIContext;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.customizer.AdvancedGlobalElementCustomizer;
import org.netbeans.modules.xml.schema.ui.nodes.schema.GlobalElementNode;
import org.netbeans.modules.xml.xam.ui.customizer.Customizer;
import org.netbeans.modules.xml.xam.ui.customizer.CustomizerProvider;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;

/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 * @author  Nathan Fiedler
 */
public class AdvancedGlobalElementNode extends GlobalElementNode {
    /**
     *
     *
     */
    public AdvancedGlobalElementNode(SchemaUIContext context,
            SchemaComponentReference<GlobalElement> reference,
            Children children) {
        super(context,reference,children);
    }
    
    
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
                return new AdvancedGlobalElementCustomizer(getReference());
            }
        };
    }
    
    public void propertyChange(PropertyChangeEvent event) {
        if(!isValid()) return;
        super.propertyChange(event);
        if(event.getSource() == getReference().get() &&
                GlobalElement.TYPE_PROPERTY.equals(event.getPropertyName())) {
            ((RefreshableChildren)getChildren()).refreshChildren();
            fireDisplayNameChange(null,getDisplayName());
        }
    }
    
    public String getHtmlDisplayName() {
        String retValue = getDefaultDisplayName();
        String rawString = null;
        
        if(getReference().get().getType()!=null &&
                (rawString = getReference().get().getType().getRefString()) !=null) {
            int i = rawString!=null?rawString.indexOf(':'):-1;
            if (i != -1 && i < rawString.length()) {
                rawString = rawString.substring(i);
            }
            String supertypeLabel = NbBundle.getMessage(
                    AdvancedGlobalElementNode.class, "LBL_InstanceOf",
                    rawString);
            retValue = retValue+"<font color='#999999'> ("+supertypeLabel+")</font>";
        }
        return applyHighlights(retValue);
    }
}
