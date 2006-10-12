/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
