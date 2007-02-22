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
import org.netbeans.modules.xml.schema.model.AttributeGroupReference;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.ui.nodes.RefreshableChildren;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaUIContext;
import org.netbeans.modules.xml.schema.ui.nodes.schema.AttributeGroupReferenceNode;
import org.openide.nodes.Children;

/**
 *
 * @author  Ajit Bhate
 */
public class AdvancedAttributeGroupReferenceNode extends AttributeGroupReferenceNode
        
{
    
    /**
     *
     *
     */
    public AdvancedAttributeGroupReferenceNode(SchemaUIContext context,
            SchemaComponentReference<AttributeGroupReference> reference,
            Children children) {
        super(context,reference,children);
        
    }

    public void propertyChange(PropertyChangeEvent event) {
        if(!isValid()) return;
        super.propertyChange(event);
        if(event.getSource() == getReference().get() &&
                AttributeGroupReference.GROUP_PROPERTY.equals(event.getPropertyName())) {
            ((RefreshableChildren)getChildren()).refreshChildren();
        }
    }
    
}
