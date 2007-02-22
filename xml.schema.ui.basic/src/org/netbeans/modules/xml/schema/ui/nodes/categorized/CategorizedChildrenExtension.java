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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.visitor.DefaultSchemaVisitor;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaUIContext;
import org.openide.nodes.Node;

/**
 * This class provides the ability to add node extensions to children based on
 * the underlying schema component type.
 * @author Chris Webster
 */
public class CategorizedChildrenExtension extends DefaultSchemaVisitor {
    
    /**
     * Determine if an extension is available for a specific SchemaComponent.
     * @return the array of nodes to be added, will be empty if no extensions are
     * required.
     */
    public List<Node> getExtension(SchemaComponent component, 
		SchemaUIContext c)
	{
		nodes = Collections.emptyList();
		context = c;
		component.accept(this);
		return nodes;
    }


	/**
	 *
	 *
	 */
    @Override
    public void visit(Schema s)
	{
		if(s==context.getModel().getSchema())
		{
			nodes = new LinkedList<Node>();
			nodes.add(context.getFactory().createPrimitiveTypesNode());
		}
    }
    
    private List<Node> nodes;
    private SchemaUIContext context;
}
