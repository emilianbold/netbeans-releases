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
import org.netbeans.modules.xml.schema.model.SimpleExtension;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.ui.nodes.*;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.GlobalReferenceProperty;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class SimpleExtensionNode extends SchemaComponentNode<SimpleExtension>
{
    /**
     *
     *
     */
    public SimpleExtensionNode(SchemaUIContext context, 
		SchemaComponentReference<SimpleExtension> reference,
		Children children)
    {
        super(context,reference,children);
    }


	/**
	 *
	 *
	 */
	@Override
	public String getTypeDisplayName()
	{
		return NbBundle.getMessage(SimpleExtensionNode.class,
			"LBL_SimpleExtensionNode_TypeDisplayName"); // NOI18N
	}

    @Override
    protected Sheet createSheet() {
        Sheet sheet = null;
        try {
            sheet = super.createSheet();
            Sheet.Set props = sheet.get(Sheet.PROPERTIES);
            if (props == null) {
                    props = Sheet.createPropertiesSet();
                    sheet.put(props);
            }
	
            Node.Property baseTypeProp = new GlobalReferenceProperty<GlobalSimpleType>(
                    getReference().get(),
                    SimpleExtension.BASE_PROPERTY,
                    NbBundle.getMessage(SimpleExtensionNode.class,
                    "PROP_BaseType_DisplayName"), // display name
                    NbBundle.getMessage(SimpleExtensionNode.class,
                    "HINT_BaseType__SimpleContent_ShortDesc"),	// descr
                    getTypeDisplayName(), // type display name
                    NbBundle.getMessage(SimpleExtensionNode.class,
                    "LBL_GlobalSimpleTypeNode_TypeDisplayName"), // reference type display name
                    GlobalSimpleType.class
                    );
            props.put(new SchemaModelFlushWrapper(getReference().get(), baseTypeProp));
        } catch (NoSuchMethodException ex) {
            assert false : "properties should be defined";
        }
        return sheet;
    }

}
