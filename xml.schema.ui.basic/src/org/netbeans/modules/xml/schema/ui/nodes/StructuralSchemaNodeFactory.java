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

package org.netbeans.modules.xml.schema.ui.nodes;

import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.PrimitiveSimpleTypesNode;
import org.netbeans.modules.xml.schema.ui.nodes.schema.SchemaNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * A simple node factory that creates instances of
 * <code>SchemaComponentNodeChildren</code> to present a structural hierarchy
 * of schema nodes.
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class StructuralSchemaNodeFactory extends SchemaNodeFactory
{
    /**
     *
     *
     */
    public StructuralSchemaNodeFactory(SchemaModel model, Lookup lookup)
    {
        super(model,lookup);
    }


	/**
	 *
	 *
	 */
	public Node createNode(SchemaComponent component)
	{
		Node node=super.createNode(component);
		if (node instanceof SchemaComponentNode && 
			!(node instanceof SchemaNode))
		{
			node=new TypeNameFilterNode((SchemaComponentNode)node);
		}

		return node;
	}


	/**
	 *
	 *
	 */
	@Override
	public <C extends SchemaComponent> Children createChildren(
			SchemaComponentReference<C> reference)
	{
		return new SchemaComponentNodeChildren<C>(getContext(),reference);
	}

        public Node createPrimitiveTypesNode() {
            return new PrimitiveSimpleTypesNode(getContext());
        }
}
