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
 * NewTypesFactory.java
 *
 * Created on May 2, 2006, 5:08 PM
 *
 */

package org.netbeans.modules.xml.schema.ui.nodes;

import java.util.ArrayList;
import org.netbeans.modules.xml.schema.model.Annotation;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.model.visitor.DeepSchemaVisitor;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.newtype.AdvancedSchemaComponentNewType;
import org.openide.util.datatransfer.NewType;

/**
 *
 * @author Ajit Bhate
 */
public class NewTypesFactory extends DeepSchemaVisitor
{
	private ArrayList<Class<? extends SchemaComponent>> childTypes;
	/**
	 * Creates a new instance of NewTypesFactory
	 */
	public NewTypesFactory()
	{
		childTypes = new ArrayList<Class<? extends SchemaComponent>>();
	}
	
	public NewType[] getNewTypes(
			SchemaComponentReference<? extends SchemaComponent> reference,
			Class<? extends SchemaComponent> filterClass)
	{
		childTypes.clear();
		reference.get().accept(this);
		ArrayList<NewType> result = new ArrayList<NewType>();
		for(Class<? extends SchemaComponent>childType:childTypes)
		{
			if(filterClass==null|| filterClass.isAssignableFrom(childType))
			{
				AdvancedSchemaComponentNewType newType =
						new AdvancedSchemaComponentNewType(reference,childType);
				if (newType.canCreate())
				{
					result.add(newType);
				}
			}
		}
		childTypes.clear();
		return result.toArray(new NewType[result.size()]);
	}
	
	protected void visitChildren(SchemaComponent sc)
	{
		addChildType(Annotation.class);
	}

	protected void addChildType(Class<? extends SchemaComponent> childType)
	{
		childTypes.add(childType);
	}
	
}
