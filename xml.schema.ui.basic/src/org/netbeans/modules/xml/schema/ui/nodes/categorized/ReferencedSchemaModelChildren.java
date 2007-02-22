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
import java.util.List;
import org.netbeans.modules.xml.schema.model.Import;
import org.netbeans.modules.xml.schema.ui.nodes.ReadOnlyCookie;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelReference;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaUIContext;
import org.netbeans.modules.xml.xam.Model;

/**
 *
 * @author  Ajit Bhate
 */
public class ReferencedSchemaModelChildren<C extends SchemaModelReference> 
		extends CategorizedChildren<C>
{
	/**
	 *
	 *
	 */
	public ReferencedSchemaModelChildren(SchemaUIContext context,
			SchemaComponentReference<C> reference)
	{
		super(context,reference);
	}
	
	
	@Override
	protected void removeNotify()
	{
		setKeys(Collections.emptyList());
	}
	
	@Override
	protected void addNotify()
	{
		Children.MUTEX.postWriteRequest(new Runnable()
		{
			public void run()
			{
				setKeys(createKeys());
			}
		});
	}
	
	@Override
	public void refreshChildren()
	{
		Children.MUTEX.postWriteRequest(new Runnable()
		{
			public void run()
			{
				setKeys(createKeys());
			}
		});
	}


	/**
	 *
	 *
	 */
	protected List<Node> createKeys()
	{
		List<Node> keys=super.createKeys();
		
		// do not show reference children in readonly context
		ReadOnlyCookie roc = (ReadOnlyCookie) getContext().getLookup().lookup(
				ReadOnlyCookie.class);
		if(roc!=null && roc.isReadOnly()) return keys;

		C component = getReference().get();
		if(component instanceof Import &&
				component.getModel()!=getContext().getModel())
			return keys;
		SchemaModel refModel;
		try
		{
			refModel = component.resolveReferencedModel();
		} catch (CatalogModelException ex)
		{
			refModel = null;
		}
		if (refModel != null && refModel.getState() != Model.State.NOT_WELL_FORMED)
		{
			Node refNode = getContext().getFactory().createNode(refModel.getSchema());
			Node dummyNode = new DummySchemaNode(refNode);
			for(Node child:dummyNode.getChildren().getNodes())
			{
				keys.add(new ReadOnlySchemaComponentNode(child));
			}
		}
		else
		{
			AbstractNode node=new AbstractNode(Children.LEAF);
			node.setIconBaseWithExtension(
					"org/netbeans/modules/xml/schema/core/resources/Schema_File.png"); // NOI18N
			node.setDisplayName(
					NbBundle.getMessage(ReferencedSchemaModelChildren.class,
					"LBL_BrokenReference")); // NOI18N
			keys.add(node);
		}
		return keys;
	}
}
