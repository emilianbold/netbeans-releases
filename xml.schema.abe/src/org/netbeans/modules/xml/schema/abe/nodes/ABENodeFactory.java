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

package org.netbeans.modules.xml.schema.abe.nodes;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIDocument;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.AbstractElement;
import org.netbeans.modules.xml.axi.AnyElement;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xml.axi.ContentModel;
import org.netbeans.modules.xml.axi.datatype.CustomDatatype;
import org.netbeans.modules.xml.axi.datatype.Datatype;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class ABENodeFactory<T extends AXIComponent> extends Object
{
    /**
     *
     *
     */
    public ABENodeFactory(AXIModel model, Lookup lookup)
    {
        super();
		context=createContext(model,lookup);
    }

	/**
	 * Creates the ABEUIContext.  Subclasses can override this method to
	 * customize the ABEUIContext instance.
	 */
	protected ABEUIContext createContext(AXIModel model, Lookup lookup)
	{
		return new ABEUIContext(model,this,lookup);
	}


	/**
	 * Returns the context object used by this factory.  All nodes created by
	 * this factory will share this context object.
	 *
	 */
	public ABEUIContext getContext()
	{
		return context;
	}

	/**
	 * Convenience method to create a "root" node for representing the schema.
	 * This method is a convenience for calling <code>createNode()</code> and 
	 * passing it a reference to the <code>Schema</code> component.
	 *
	 */
	public Node createRootNode()
	{
		return createRootNode(null);
	}

	/**
	 * Convenience method to create a "root" node for representing the schema.
	 * This method is a convenience for calling <code>createNode()</code> and 
	 * passing it a reference to the <code>Schema</code> component.
	 *
	 */
	public Node createRootNode(List<Class> filterTypes)
	{
		return new CategorizedDocumentNode(context,
                        context.getModel().getRoot(), filterTypes);
	}	



	////////////////////////////////////////////////////////////////////////////
	// Primary factory methods
	////////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a node for the specified schema component
	 *
	 */
	public Node createNode(
		Node parent, AXIComponent component)
	{
		if(component instanceof AnyElement)
			return new AnyElementNode((AnyElement) component);
		else if(component instanceof AbstractElement)
			return new ElementNode((AbstractElement) component);
		else if(component instanceof ContentModel)
			return new ContentModelNode((ContentModel) component);
                else if(component instanceof Attribute)
			return new AttributeNode((Attribute) component);
		return null;
	}
	

	/**
	 * Creates a node for the specified schema component
	 *
	 */
	public Node createNode(
		Node parent, Datatype component)
	{
		if(component instanceof CustomDatatype)
			return new CustomDatatypeNode((CustomDatatype) component);
		else
			return new DatatypeNode(component);
		
	}



	/**
	 * Creates the children object for the specified component reference. The
	 * defalut implementation returns <code>Children.LEAF</code>, meaning that
	 * any nodes created via <code>createNode()</code> will be lead nodes with
	 * no sub-structure.  Subclasses should override this method to return
	 * more functional children objects.<p>
	 *
	 * Note, this method is only used by convention by methods in this class;
	 * subclasses are free to create and use any children object in the 
	 * various node factory methods.  This method provides a way to override
	 * the default children created by this class, but its use by particular
	 * node factory methods is not guaranteed.
	 *
	 * @param	parent
	 *			The parent node of the about-to-be created node for which this
	 *			method will return the children object.  Note, this node is
	 *			<em>not</em> the node with which the return children object
	 *			will be associated.
	 * @param	reference
	 *			The schema component reference associated with the about-to-be-
	 *			created node.
	 */
	public Children createChildren(
		Node parent, AXIComponent component)
	{
		return Children.LEAF;
	}

	////////////////////////////////////////////////////////////////////////////
	// Instance members
	////////////////////////////////////////////////////////////////////////////

	private ABEUIContext context;	
}
