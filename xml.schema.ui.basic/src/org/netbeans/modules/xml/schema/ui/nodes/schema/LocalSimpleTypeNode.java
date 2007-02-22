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

package org.netbeans.modules.xml.schema.ui.nodes.schema;

import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.List;
import org.netbeans.modules.xml.schema.model.LocalSimpleType;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.model.SimpleTypeDefinition;
import org.netbeans.modules.xml.schema.model.SimpleTypeRestriction;
import org.netbeans.modules.xml.schema.ui.nodes.*;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;
/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class LocalSimpleTypeNode extends SchemaComponentNode<LocalSimpleType>
{
    /**
     *
     *
     */
    public LocalSimpleTypeNode(SchemaUIContext context, 
		SchemaComponentReference<LocalSimpleType> reference,
		Children children)
    {
        super(context,reference,children);
		setIconBaseWithExtension(
			"org/netbeans/modules/xml/schema/ui/nodes/resources/"+
			"simpletype.png");
    }


	@Override
	protected GlobalSimpleType getSuperDefinition()
	{
		SimpleTypeDefinition definition = getReference().get().getDefinition();
        assert getReference().get().isInDocumentModel() : 
            "node component is no longer part of model, node should have been refreshed";
        assert definition.isInDocumentModel() : "definition is not refreshed";
        
		GlobalSimpleType gt = null;
		if(definition instanceof SimpleTypeRestriction)
		{
			SimpleTypeRestriction str = (SimpleTypeRestriction)definition;
			if(str.getBase()!=null)
			{
				gt = str.getBase().get();
			}
		}
        if(definition instanceof List)
		{
			List list = (List)definition;
			GlobalSimpleType gst = null;
			if(list.getType()!=null)
			{
				gt = list.getType().get();
			}
		}
		return gt;
	}
	
	/**
	 *
	 *
	 */
	@Override
	public String getTypeDisplayName()
	{
		return NbBundle.getMessage(LocalSimpleTypeNode.class,
			"LBL_LocalSimpleTypeNode_TypeDisplayName"); // NOI18N
	}
}
