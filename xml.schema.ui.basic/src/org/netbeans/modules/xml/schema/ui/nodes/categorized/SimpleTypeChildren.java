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

import org.netbeans.modules.xml.schema.model.Enumeration;
import org.netbeans.modules.xml.schema.model.SimpleType;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAdapter;
import org.openide.util.NbBundle;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.ui.nodes.DefaultExpandedCookie;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaComponentNode;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaUIContext;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.List;
import org.netbeans.modules.xml.schema.model.LocalSimpleType;
import org.netbeans.modules.xml.schema.model.SimpleTypeDefinition;
import org.netbeans.modules.xml.schema.model.SimpleTypeRestriction;

/**
 *
 * @author  Ajit Bhate
 */
public class SimpleTypeChildren<C extends SimpleType>
        extends CategorizedChildren<C>
{
    /**
     *
     *
     */
    public SimpleTypeChildren(SchemaUIContext context,
            SchemaComponentReference<C> reference) {
        super(context,reference);
    }
    
    
    /**
     *
     *
     */
    protected java.util.List<Node> createKeys() {
        java.util.List<Node> keys=super.createKeys();
        
        int index=0;
        
        // Insert the inherited node after the details node
        if (keys.size() > 0 && keys.get(0) instanceof DetailsNode)
            index=1;
        
        LocalSimpleType inlineType = null;
        SimpleTypeDefinition definition = getReference().get().getDefinition();
        if(definition instanceof SimpleTypeRestriction) {
            SimpleTypeRestriction str = (SimpleTypeRestriction)definition;
            inlineType = str.getInlineType();
        }
        if(definition instanceof List) {
            List list = (List)definition;
            inlineType = list.getInlineType();
        }
        if(definition instanceof SimpleTypeRestriction ||
                definition instanceof List) {
            for (int i=0;i<keys.size();i++) {
                Node n = keys.get(i);
                SchemaComponentNode scn = (SchemaComponentNode)n.getCookie
                        (SchemaComponentNode.class);
                if(scn!=null && scn.getReference().get()==definition) {
                    index = i;
                    keys.remove(index);
                    break;
                }
            }
        }
        if(inlineType != null) {
            Node inlineNode=
                    getContext().getFactory().createNode(inlineType);
            
            DefaultExpandedCookie expanded=(DefaultExpandedCookie)
            inlineNode.getCookie(DefaultExpandedCookie.class);
            if (expanded!=null)
                expanded.setDefaultExpanded(true);
            
            keys.add(index++,inlineNode);
        }
        if(definition instanceof SimpleTypeRestriction) {
            // add enum children
            java.util.List<Enumeration> enumChildren =
                    definition.getChildren(Enumeration.class);
            for(Enumeration e:enumChildren) {
                keys.add(index++,getContext().getFactory().createNode(e));
            }
        }
        return keys;
    }
}
