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

import org.netbeans.modules.xml.schema.model.ComplexContent;
import org.netbeans.modules.xml.schema.model.ComplexContentDefinition;
import org.netbeans.modules.xml.schema.model.ComplexContentRestriction;
import org.netbeans.modules.xml.schema.model.ComplexExtension;
import org.netbeans.modules.xml.schema.model.ComplexTypeDefinition;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.LocalComplexType;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.model.SimpleContent;
import org.netbeans.modules.xml.schema.model.SimpleContentDefinition;
import org.netbeans.modules.xml.schema.model.SimpleContentRestriction;
import org.netbeans.modules.xml.schema.model.SimpleExtension;
import org.netbeans.modules.xml.schema.ui.nodes.*;

import org.openide.nodes.Children;
import org.openide.nodes.Node.Property;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.BooleanProperty;
/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class LocalComplexTypeNode extends SchemaComponentNode<LocalComplexType>
{
    /**
     *
     *
     */
    public LocalComplexTypeNode(SchemaUIContext context, 
		SchemaComponentReference<LocalComplexType> reference,
		Children children)
    {
        super(context,reference,children);
		setIconBaseWithExtension(
			"org/netbeans/modules/xml/schema/ui/nodes/resources/"+
			"complextype.png");
    }


	protected GlobalType getSuperDefinition()
	{
		ComplexTypeDefinition definition = getReference().get().getDefinition();
		GlobalType gt = null;
		if(definition instanceof ComplexContent)
		{
			ComplexContentDefinition contentDef =
					((ComplexContent)definition).getLocalDefinition();
			if (contentDef instanceof ComplexContentRestriction)
			{
				ComplexContentRestriction ccr = (ComplexContentRestriction)contentDef;
				if(ccr.getBase()!= null)
				{
					gt=ccr.getBase().get();
				}
			}
			if (contentDef instanceof ComplexExtension)
			{
				ComplexExtension ce = (ComplexExtension)contentDef;
				if(ce.getBase()!= null)
				{
					gt=ce.getBase().get();
				}
			}
		}
		else if(definition instanceof SimpleContent)
		{
			SimpleContentDefinition contentDef =
					((SimpleContent)definition).getLocalDefinition();
			if (contentDef instanceof SimpleContentRestriction)
			{
				SimpleContentRestriction scr = (SimpleContentRestriction)contentDef;
				if(scr.getBase()!= null)
				{
					gt=scr.getBase().get();
				}
			}
			if (contentDef instanceof SimpleExtension)
			{
				SimpleExtension se = (SimpleExtension)contentDef;
				if(se.getBase()!= null)
				{
					gt=se.getBase().get();
				}
			}
		}
		return gt;
	}

    @Override
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        Sheet.Set set = sheet.get(Sheet.PROPERTIES);
        try {
            // Mixed property
            Property mixedProp = new BooleanProperty(
                    getReference().get(), // schema component
                    LocalComplexType.MIXED_PROPERTY, // property name
                    NbBundle.getMessage(LocalComplexTypeNode.class,"PROP_Mixed_DisplayName"), // display name
                    NbBundle.getMessage(LocalComplexTypeNode.class,"PROP_Mixed_ShortDescription"),	// descr
                    true // default value is false
                    );
            set.put(new SchemaModelFlushWrapper(getReference().get(), mixedProp));
        } catch (NoSuchMethodException nsme) {
            assert false : "properties should be defined";
        }
        return sheet;
    }
    
    
	/**
	 *
	 *
	 */
	@Override
	public String getTypeDisplayName()
	{
		return NbBundle.getMessage(LocalComplexTypeNode.class,
			"LBL_LocalComplexTypeNode_TypeDisplayName"); // NOI18N
	}
}
