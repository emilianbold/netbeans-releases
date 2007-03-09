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

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.netbeans.modules.xml.schema.model.Import;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.ui.nodes.*;

/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class ImportNode extends SchemaComponentNode<Import>
{
    /**
     *
     *
     */
    public ImportNode(SchemaUIContext context, 
		SchemaComponentReference<Import> reference,
		Children children)
    {
        super(context,reference,children);
		setIconBaseWithExtension(
			"org/netbeans/modules/xml/schema/ui/nodes/resources/import-include-redefine.png");
    }


    @Override
    protected Sheet createSheet() 
    {
        Sheet sheet = super.createSheet();
        Sheet.Set props = sheet.get(Sheet.PROPERTIES);
        if (props == null) {
            props = Sheet.createPropertiesSet();
            sheet.put(props);
        }
		// Namespace property
		Property nsProp = new PropertySupport.ReadOnly(
				Import.NAMESPACE_PROPERTY, String.class,
				NbBundle.getMessage(ImportNode.class,"PROP_Namespace_DisplayName"),
				NbBundle.getMessage(ImportNode.class,"HINT_Namespace_ShortDesc")
				)
		{
			public Object getValue() throws
					IllegalAccessException,InvocationTargetException
			{
				return getReference().get().getNamespace();
			}
		};
		props.put(nsProp);
		
		// Schema Location property
		Property slProp = new PropertySupport.ReadOnly(
				Import.SCHEMA_LOCATION_PROPERTY, String.class,
				NbBundle.getMessage(ImportNode.class,"PROP_SchemaLocation_DisplayName"),
				NbBundle.getMessage(ImportNode.class,"HINT_SchemaLocation_ShortDesc")
				)
		{
			public Object getValue() throws
					IllegalAccessException,InvocationTargetException
			{
				return getReference().get().getSchemaLocation();
			}
		};
		props.put(slProp);
 
		// Prefix property
		Property prefixProp = new PropertySupport.ReadOnly(
				"prefix", //NOI18N
				String.class,
				NbBundle.getMessage(ImportNode.class,"PROP_Prefix_DisplayName"),
				NbBundle.getMessage(ImportNode.class,"HINT_Prefix_ShortDesc")
				)
		{
			public Object getValue() throws
					IllegalAccessException,InvocationTargetException
			{
				Map<String,String> prefixMap =
						getReference().get().getModel().getSchema().getPrefixes();
				String namespace = getReference().get().getNamespace();
				if(prefixMap.containsValue(namespace))
				{
					for (Map.Entry<String,String> entry :prefixMap.entrySet())
					{
						if (entry.getValue().equals(namespace))
						{
							return entry.getKey();
						}
					}
				}
				return null;
			}
		};
		props.put(prefixProp);
		
		return sheet;
    }

    @Override
    protected void updateDisplayName() {
        // Force the display name to be updated.
        fireDisplayNameChange("NotTheDefaultName", getDefaultDisplayName());
    }

    @Override
    public String getHtmlDisplayName() {
        String name = getDefaultDisplayName() +
                " {" + getReference().get().getNamespace() + "}";
        return applyHighlights(name);
    }

    @Override
    public String getTypeDisplayName() {
        return NbBundle.getMessage(ImportNode.class,
                "LBL_ImportNode_TypeDisplayName");
    }
}
