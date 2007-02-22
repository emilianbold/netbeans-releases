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
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.ui.nodes.*;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.BooleanProperty;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class ComplexContentNode extends SchemaComponentNode<ComplexContent>
{
    /**
     *
     *
     */
    public ComplexContentNode(SchemaUIContext context, 
		SchemaComponentReference<ComplexContent> reference,
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
		return NbBundle.getMessage(ComplexContentNode.class,
			"LBL_ComplexContentNode_TypeDisplayName"); // NOI18N
	}

    @Override
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        Sheet.Set set = sheet.get(Sheet.PROPERTIES);
        try {
        // Mixed property
        Property mixedProp = new BooleanProperty(
                getReference().get(), // schema component
                ComplexContent.MIXED_PROPERTY, // property name
                NbBundle.getMessage(ComplexContentNode.class,"PROP_Mixed_DisplayName"), // display name
                NbBundle.getMessage(ComplexContentNode.class,"PROP_Mixed_ShortDescription"),	// descr
                false // default value is null
                );
        set.put(new SchemaModelFlushWrapper(getReference().get(), mixedProp));
        } catch (NoSuchMethodException nsme) {
            assert false : "properties should be defined";
        }
        
        return sheet;
    }
}
