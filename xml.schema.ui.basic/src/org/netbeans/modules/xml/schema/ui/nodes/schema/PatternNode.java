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

import org.netbeans.modules.xml.schema.model.Pattern;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.ui.nodes.*;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.BaseSchemaProperty;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class PatternNode extends SchemaComponentNode<Pattern>
{
    /**
     *
     *
     */
    public PatternNode(SchemaUIContext context, 
		SchemaComponentReference<Pattern> reference,
		Children children)
    {
        super(context,reference,children);
    }


	/**
	 *
	 *
	 */
	protected void updateDisplayName()
	{
		setDisplayName(getReference().get().getValue());
	}


	/**
	 *
	 *
	 */
	@Override
	public String getTypeDisplayName()
	{
		return NbBundle.getMessage(PatternNode.class,
			"LBL_PatternNode_TypeDisplayName"); // NOI18N
	}

	@Override
	protected Sheet createSheet() {
		Sheet sheet = super.createSheet();
		Sheet.Set set = sheet.get(Sheet.PROPERTIES);
		try {
			Node.Property patternProp = new BaseSchemaProperty(
					getReference().get(),
					String.class,
					Pattern.VALUE_PROPERTY,
					NbBundle.getMessage(PatternNode.class,
					"PROP_Value_DisplayName"),
					NbBundle.getMessage(PatternNode.class,
					"PROP_Value_ShortDescription"),
					null);
			patternProp = 
				new SchemaModelFlushWrapper(getReference().get(), 
				patternProp);
			set.put(patternProp);
		} catch (NoSuchMethodException ex) {
			assert false : "properties should be defined";
		}
		return sheet;
	}
}
