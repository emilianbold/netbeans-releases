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
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * A standard children object for schema component nodes.  This class can be
 * used directly by a node, in which case it will defer to its parent node
 * to create nodes for child components.  Or, this class can be used as a
 * superclass, in which case the developer should override the 
 * <code>createChildNodes()</code> method to create new node children.
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class SchemaComponentNodeChildren<T extends SchemaComponent>
	extends RefreshableChildren
{
    /**
     *
     *
     */
    public SchemaComponentNodeChildren(SchemaUIContext context, 
		SchemaComponentReference<T> reference)
    {
        super();
		this.context=context;
		this.reference=reference;
    }


	/**
	 *
	 *
	 */
	public SchemaUIContext getContext()
	{
		return context;
	}


	/**
	 *
	 *
	 */
	public SchemaComponentReference<T> getReference()
	{
		return reference;
	}


	/**
	 *
	 *
	 */
	@Override
	protected void addNotify()
	{
		super.addNotify();
//        getModel().addPropertyChangeListener(this);
		refreshChildren();
	}


	/**
	 *
	 *
	 */
	@Override
	protected void removeNotify()
	{
		super.removeNotify();
		super.nodes.clear();
		refresh();
//		getModel().removePropertyChangeListener(this);
	}


	/**
	 *
	 *
	 */
	@Override
	public void refreshChildren()
	{
		T parentComponent=getReference().get();

//		// Build a list of the children of the parent component
//		List<SchemaComponentReference> references=
//			new ArrayList<SchemaComponentReference>();
//		for (SchemaComponent component: parentComponent.getChildren())
//			references.add(SchemaComponentReference.create(component));
//
//		setKeys(references);
		setKeys(parentComponent.getChildren());
	}


	/**
	 *
	 *
	 */
	@Override
	protected Node[] createNodes(Object key)
	{
		Node[] result=null;

		assert key==null || key instanceof SchemaComponent:
			"Key was not a SchemaComponent (key = \""+
			key+"\", class = "+key.getClass()+")";

		if (key instanceof SchemaComponent)
		{
			Node node=createChildNode(SchemaComponent.class.cast(key));
			if (node!=null)
				result=new Node[] { node };
		}

		return result;
	}


	/**
	 * Subclasses should override this method to create nodes for a given
	 * schema component
	 *
	 */
	protected <C extends SchemaComponent> Node createChildNode(
		SchemaComponent component)
	{
		Node result=getContext().getFactory().createNode(component);
		return result;
	}

//	/**
//	 * Subclasses should override this method to create nodes for a given
//	 * schema component
//	 *
//	 */
//	protected <C extends SchemaComponent> Node createChildNode(
//		SchemaComponentReference<C> reference)
//	{
//		Node result=getContext().getFactory().createNode(getNode(),reference.get());
//		return result;
//	}
//

//	/**
//	 *
//	 *
//	 */
//	public Collection<SchemaComponent> getKeys()
//	{
//		return keys;
//	}
//
//
//	/**
//	 *
//	 *
//	 */
//	public void setKeys(Collection<SchemaComponent> value)
//	{
//		keys=value;
//		super.setKeys(value);
//	}




	////////////////////////////////////////////////////////////////////////////
	// Instance variables
	////////////////////////////////////////////////////////////////////////////

	private SchemaUIContext context;
	private SchemaComponentReference<T> reference;
//    private Collection<SchemaComponent> keys;
}
