/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
