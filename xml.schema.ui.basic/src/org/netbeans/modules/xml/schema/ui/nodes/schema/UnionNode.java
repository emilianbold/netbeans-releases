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

import org.netbeans.modules.xml.schema.model.Union;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.ui.nodes.*;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.MemberTypesProperty;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class UnionNode extends SchemaComponentNode<Union>
{
    /**
     *
     *
     */
    public UnionNode(SchemaUIContext context, 
		SchemaComponentReference<Union> reference,
		Children children)
    {
        super(context,reference,children);
	setIconBaseWithExtension(
			"org/netbeans/modules/xml/schema/ui/nodes/resources/union.png");
    }


	/**
	 *
	 *
	 */
	@Override
	public String getTypeDisplayName()
	{
		return NbBundle.getMessage(UnionNode.class,
			"LBL_UnionNode_TypeDisplayName"); // NOI18N
	}
    
    /**
     *
     *
     */
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
	
            Node.Property memberTypeProp = new MemberTypesProperty(
                    getReference().get(),
                    Union.MEMBER_TYPES_PROPERTY,
                    NbBundle.getMessage(UnionNode.class,
                    "PROP_MemberTypes_DisplayName"), // display name
                    NbBundle.getMessage(UnionNode.class,
                    "HINT_MemberTypes_ShortDesc")	// descr
                    );
            props.put(new SchemaModelFlushWrapper(getReference().get(), memberTypeProp));
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
            assert false : "properties should be defined";
        }
        return sheet;
    }

}
