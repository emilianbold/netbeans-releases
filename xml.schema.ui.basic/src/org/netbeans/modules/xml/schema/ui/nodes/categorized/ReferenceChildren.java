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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.schema.ui.nodes.categorized;

import java.util.List;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.schema.model.AttributeGroupReference;
import org.netbeans.modules.xml.schema.model.AttributeReference;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.GroupReference;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaUIContext;

/**
 *
 * @author  Ajit Bhate
 */
public class ReferenceChildren<C extends SchemaComponent>
        extends CategorizedChildren<C>
{
    /**
     *
     *
     */
    public ReferenceChildren(SchemaUIContext context,
            SchemaComponentReference<C> reference) {
        super(context,reference);
    }
    
    
    /**
     *
     *
     */
    protected List<Node> createKeys() {
        List<Node> keys=super.createKeys();
        
        int index=0;
        
        // Insert the inherited node after the details node
        if (keys.size() > 0 && keys.get(0) instanceof DetailsNode)
            index=1;
        
        NamedComponentReference<? extends ReferenceableSchemaComponent> ref = null;
        if(getReference().get() instanceof AttributeReference) {
            ref = ((AttributeReference)getReference().get()).getRef();
        } else if(getReference().get() instanceof AttributeGroupReference) {
            ref = ((AttributeGroupReference)getReference().get()).getGroup();
        } else if(getReference().get() instanceof ElementReference) {
            ref = ((ElementReference)getReference().get()).getRef();
        } else if(getReference().get() instanceof GroupReference) {
            ref = ((GroupReference)getReference().get()).getRef();
        }
        if (ref != null && !ref.isBroken() && ref.get() != null) {
            // Create a readonly node to show the references's content
            keys.add(index, new ReadOnlySchemaComponentNode(
                    getContext().getFactory().createNode(ref.get()),
                    NbBundle.getMessage(ReferenceChildren.class,
                    "LBL_InheritedFrom")));
        }
        return keys;
    }
}
