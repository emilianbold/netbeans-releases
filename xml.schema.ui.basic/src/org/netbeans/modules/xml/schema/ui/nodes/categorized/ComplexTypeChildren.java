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

import java.util.List;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.ComplexContent;
import org.netbeans.modules.xml.schema.model.ComplexContentDefinition;
import org.netbeans.modules.xml.schema.model.ComplexContentRestriction;
import org.netbeans.modules.xml.schema.model.ComplexExtension;
import org.netbeans.modules.xml.schema.model.ComplexType;
import org.netbeans.modules.xml.schema.model.ComplexTypeDefinition;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.model.SimpleContent;
import org.netbeans.modules.xml.schema.model.SimpleContentDefinition;
import org.netbeans.modules.xml.schema.model.SimpleContentRestriction;
import org.netbeans.modules.xml.schema.model.SimpleExtension;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaComponentNode;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaUIContext;

/**
 *
 * @author  Ajit Bhate
 */
public class ComplexTypeChildren<C extends ComplexType>
        extends CategorizedChildren<C>
{
    /**
     *
     *
     */
    public ComplexTypeChildren(SchemaUIContext context,
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
        
        SchemaComponent extensionOrRestriction = null;
        GlobalType extensionBase = null;
        ComplexType type = getReference().get();
        if (type.getModel() == null) {
            // Without a model, this will all fail.
            return keys;
        }
        ComplexTypeDefinition definition = type.getDefinition();
        if(definition instanceof ComplexContent) {
            ComplexContentDefinition contentDef =
                    ((ComplexContent)definition).getLocalDefinition();
            if (contentDef instanceof ComplexContentRestriction) {
                extensionOrRestriction = contentDef;
            }
            if (contentDef instanceof ComplexExtension) {
                extensionOrRestriction = contentDef;
                ComplexExtension ce = (ComplexExtension)extensionOrRestriction;
                if(ce.getBase()!=null) extensionBase = ce.getBase().get();
            }
        } else if(definition instanceof SimpleContent) {
            SimpleContentDefinition contentDef =
                    ((SimpleContent)definition).getLocalDefinition();
            if (contentDef instanceof SimpleContentRestriction) {
                extensionOrRestriction = contentDef;
            }
            if (contentDef instanceof SimpleExtension) {
                extensionOrRestriction = contentDef;
                SimpleExtension ce = (SimpleExtension)extensionOrRestriction;
                if(ce.getBase()!=null) extensionBase = ce.getBase().get();
            }
        }
        if(definition instanceof ComplexContent ||
                definition instanceof SimpleContent) {
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
        if(extensionOrRestriction!= null) {
            if(extensionBase!=null) {
            keys.add(index++, new ReadOnlySchemaComponentNode(
                    getContext().getFactory().createNode(extensionBase),
                    NbBundle.getMessage(ComplexTypeChildren.class,
                    "LBL_InheritedFrom")));
            }
            for (SchemaComponent c : extensionOrRestriction.getChildren()) {
                Node cNode = getContext().getFactory().createNode(c);
                keys.add(index++,cNode);
            }
        }
        return keys;
    }
}
