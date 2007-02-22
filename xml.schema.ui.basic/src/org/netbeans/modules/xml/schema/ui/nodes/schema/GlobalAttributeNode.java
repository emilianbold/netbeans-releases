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

import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.ui.nodes.*;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.DefaultProperty;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.FixedProperty;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class GlobalAttributeNode extends SchemaComponentNode<GlobalAttribute>
{
   /**
     *
     *
     */
    public GlobalAttributeNode(SchemaUIContext context, 
		SchemaComponentReference<GlobalAttribute> reference,
		Children children)
    {
        super(context,reference,children);

		setIconBaseWithExtension(
			"org/netbeans/modules/xml/schema/ui/nodes/resources/attribute.png");
    }


	@Override
	protected GlobalSimpleType getSuperDefinition()
	{
		GlobalAttribute sc = getReference().get();
		GlobalSimpleType gt = null;
		if(sc.getType()!=null)
			gt = sc.getType().get();
		return gt;
	}
	
	/**
	 *
	 *
	 */
	@Override
	public String getTypeDisplayName()
	{
		return NbBundle.getMessage(GlobalAttributeNode.class,
			"LBL_GlobalAttributeNode_TypeDisplayName"); // NOI18N
	}

    @Override
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        Sheet.Set set = sheet.get(Sheet.PROPERTIES);
        try {
            // form and type should have a custom editor
            
            // fixed property
            Node.Property fixedProp = new FixedProperty(
                    getReference().get(), // schema component
                    NbBundle.getMessage(GlobalAttributeNode.class,"PROP_Fixed_DisplayName"), // display name
                    NbBundle.getMessage(GlobalAttributeNode.class,"PROP_Fixed_ShortDescription")	// descr
                    );
            set.put(new SchemaModelFlushWrapper(getReference().get(),fixedProp));
            
            // default property
            Node.Property defaultProp = new DefaultProperty(
                    getReference().get(), // schema component
                    NbBundle.getMessage(GlobalAttributeNode.class,"PROP_Default_DisplayName"), // display name
                    NbBundle.getMessage(GlobalAttributeNode.class,"PROP_Default_ShortDescription")	// descr
                    );
            set.put(new SchemaModelFlushWrapper(getReference().get(),defaultProp));
            
        } catch (NoSuchMethodException nsme) {
            assert false : "properties should be defined";
        }
        
        return sheet;
    }
}
