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
import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaUIContext;

/**
 *
 * @author  Ajit Bhate
 */
public class ElementChildren<C extends Element>
        extends CategorizedChildren<C>
{
    /**
     *
     *
     */
    public ElementChildren(SchemaUIContext context,
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
        
        GlobalType superType = null;
        if(getReference().get() instanceof GlobalElement) {
            GlobalElement element = (GlobalElement) getReference().get();
            if(element.getType()!=null)
                superType = element.getType().get();
        } else if(getReference().get() instanceof LocalElement) {
            LocalElement element = (LocalElement) getReference().get();
            if(element.getType()!=null)
                superType = element.getType().get();
        }
        if (superType != null) {
            // Create a readonly node to show the supertype's content
            keys.add(index, new ReadOnlySchemaComponentNode(
                    getContext().getFactory().createNode(superType),
                    NbBundle.getMessage(ElementChildren.class,
                    "LBL_InheritedFrom")));
        }
        return keys;
    }
}
