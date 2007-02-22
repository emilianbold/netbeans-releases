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

/*
 * DummySchemaNode.java
 *
 * Created on April 12, 2006, 2:41 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.ui.nodes.categorized;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;

/**
 *
 * @author Ajit Bhate
 */
public class DummySchemaNode extends FilterNode implements Node.Cookie
{
	
	public DummySchemaNode(Node original)
	{
		this(original, new Children(original));
	}
	
	private DummySchemaNode(Node original, org.openide.nodes.Children children)
	{
		super(original, children);
	}
	
	private static class Children extends FilterNode.Children
	{
		public Children(Node original)
		{
			super(original);
		}
		
		@Override
		protected void removeNotify()
		{
			setKeys((Collection)Collections.emptyList());
		}
		
		@Override
		protected void addNotify()
		{
			setKeys(createKeys());
		}
		
		private ArrayList<Node> createKeys()
		{
			ArrayList<Node> keys = new ArrayList<Node>();
			keys.add(new DummyInnerSchemaNode(original));
			Node[] children = original.getChildren().getNodes();
			for(Node child:children)
			{
				if(child.getCookie(CategoryNode.class)!=null)
					keys.add(child);
			}
			return keys;
		}
		
		
	}
	public static class DummyInnerSchemaNode extends DummySchemaNode
	{
		
		public DummyInnerSchemaNode(final Node original)
		{
			super(original, new FilterNode.Children(original)
			{
				@Override
				protected Node[] createNodes(Node n)
				{
					if (n.getCookie(CategoryNode.class) != null)
						return new Node[]{};
					return super.createNodes(n);
				}
			});
		}
	}
}
