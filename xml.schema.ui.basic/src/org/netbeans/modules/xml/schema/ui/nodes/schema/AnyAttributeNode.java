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

import org.netbeans.modules.xml.schema.model.AnyAttribute;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.ui.basic.editors.AnyNamespaceEditor;
import org.netbeans.modules.xml.schema.ui.basic.editors.ProcessContentsEditor;
import org.netbeans.modules.xml.schema.ui.nodes.*;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.BaseSchemaProperty;
import org.openide.nodes.Children;
import org.openide.nodes.Node.Property;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class AnyAttributeNode extends SchemaComponentNode<AnyAttribute>
{
    /**
     *
     *
     */
    public AnyAttributeNode(SchemaUIContext context, 
		SchemaComponentReference<AnyAttribute> reference,
		Children children)
    {
        super(context,reference,children);

        setIconBaseWithExtension(
			"org/netbeans/modules/xml/schema/ui/nodes/resources/"+
			"attribute.png");
    }


	/**
	 *
	 *
	 */
	@Override
	public String getTypeDisplayName()
	{
		return NbBundle.getMessage(AnyAttributeNode.class,
			"LBL_AnyAttributeNode_TypeDisplayName"); // NOI18N
	}

    @Override
    protected Sheet createSheet()
    {
        Sheet sheet = super.createSheet();
        Sheet.Set set = sheet.get(Sheet.PROPERTIES);
        try {
            
            // processContents
            Property processContentsProp = new BaseSchemaProperty(
                    getReference().get(), // schema component
                    AnyAttribute.ProcessContents.class, // Any.ProcessContents.class as value type
                    AnyAttribute.PROCESS_CONTENTS_PROPERTY,
                    NbBundle.getMessage(AnyAttributeNode.class,"PROP_ProcessContentsProp_DisplayName"), // display name
                    NbBundle.getMessage(AnyAttributeNode.class,"PROP_ProcessContentsProp_ShortDescription"),	// descr
                    ProcessContentsEditor.class);
            set.put(new SchemaModelFlushWrapper(getReference().get(), processContentsProp));
            
            // namespace
            Property namespaceProp = new BaseSchemaProperty(
                    getReference().get(), // schema component
                    String.class, // Any.ProcessContents.class as value type
                    AnyAttribute.NAMESPACE_PROPERTY,
                    NbBundle.getMessage(AnyAttributeNode.class,"PROP_Namespace_DisplayName"), // display name
                    NbBundle.getMessage(AnyAttributeNode.class,"HINT_Namespace_ShortDesc"),	// descr
                    AnyNamespaceEditor.class);
            set.put(new SchemaModelFlushWrapper(getReference().get(), namespaceProp));
            
        } catch (NoSuchMethodException nsme) {
            assert false : "properties should be defined";
        }
        
        return sheet;
    }
}
