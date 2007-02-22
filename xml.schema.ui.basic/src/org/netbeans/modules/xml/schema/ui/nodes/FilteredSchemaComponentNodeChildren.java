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

import java.util.*;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;

/**
 * A children object that shows only a single type of child
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class FilteredSchemaComponentNodeChildren<C extends SchemaComponent>
	extends SchemaComponentNodeChildren<C>
{
    /**
     *
     *
     */
    public <T extends SchemaComponent> FilteredSchemaComponentNodeChildren(
		SchemaUIContext context, SchemaComponentReference<C> reference, 
		Class<T> childType, Comparator<SchemaComponent> comparator)
    {
        super(context,reference);
		this.childType=childType;
		this.comparator=comparator;
	}

	
	/**
	 * The type of child to create
	 *
	 */
	public Class<? extends SchemaComponent> getChildType()
	{
		return childType;
	}


	/**
	 *
	 *
	 */
	public Comparator<SchemaComponent> getComparator()
	{
		return comparator;
	}


	/**
	 *
	 *
	 */
	@Override
	public void refreshChildren()
	{
		C parentComponent=getReference().get();

		List<SchemaComponent> children=new java.util.ArrayList<SchemaComponent>(
			parentComponent.getChildren(getChildType()));

//		// Build a list of references for the children of the parent component
//		List<SchemaComponentReference> references=
//			new ArrayList<SchemaComponentReference>();
//		for (SchemaComponent component: children)
//		{
//			references.add(SchemaComponentReference.create(component));
//		}
//
		if (getComparator()!=null)
			Collections.sort(children,getComparator());

		setKeys(children);
	}




	////////////////////////////////////////////////////////////////////////////
	// Instance members
	////////////////////////////////////////////////////////////////////////////

	private Class<? extends SchemaComponent> childType;
	private Comparator<SchemaComponent> comparator;
}
