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

import org.netbeans.modules.xml.schema.model.Notation;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.ui.nodes.*;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.BaseSchemaProperty;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.NamespaceProperty;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.nodes.Node.Property;
import org.openide.util.NbBundle;
/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class NotationNode extends SchemaComponentNode<Notation>
{
    /**
     *
     *
     */
    public NotationNode(SchemaUIContext context, 
		SchemaComponentReference<Notation> reference,
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
		return NbBundle.getMessage(NotationNode.class,
			"LBL_NotationNode_TypeDisplayName"); // NOI18N
	}

	/**
	 *
	 *
	 */
    @Override
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        Sheet.Set set = sheet.get(Sheet.PROPERTIES);
        try {
        // Public property
        Property publicProp = new BaseSchemaProperty(
                getReference().get(), // schema component
                String.class, // type
//                Notation.PUBLIC_PROPERTY, // property name
                "publicIdentifier", // property name
                NbBundle.getMessage(NotationNode.class,"PROP_PublicIdentifier_DisplayName"), // display name
                NbBundle.getMessage(NotationNode.class,"PROP_PublicIdentifier_ShortDescription"),	// descr
                null // no property editor required
                ); 
        set.put(new SchemaModelFlushWrapper(getReference().get(), publicProp));

        Property systemProp = new NamespaceProperty(
                getReference().get(), // schema component
//                Notation.PUBLIC_PROPERTY, // property name
                "systemIdentifier", // property name
                NbBundle.getMessage(NotationNode.class,"PROP_SystemIdentifier_DisplayName"), // display name
                NbBundle.getMessage(NotationNode.class,"PROP_SystemIdentifier_ShortDescription"),	// descr
                getTypeDisplayName() // type display name
                ); 
        set.put(new SchemaModelFlushWrapper(getReference().get(), systemProp));
        } catch (NoSuchMethodException nsme) {
            assert false : "properties should be defined";
        }
        
        return sheet;
    }
}
