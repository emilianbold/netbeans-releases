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

import org.netbeans.modules.xml.schema.model.AnyElement;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.ui.basic.editors.AnyNamespaceEditor;
import org.netbeans.modules.xml.schema.ui.basic.editors.MaxOccursEditor;
import org.netbeans.modules.xml.schema.ui.basic.editors.ProcessContentsEditor;
import org.netbeans.modules.xml.schema.ui.nodes.*;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.BaseSchemaProperty;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.NonNegativeIntegerProperty;
import org.openide.nodes.Children;
import org.openide.nodes.Node.Property;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class AnyNode extends SchemaComponentNode<AnyElement>
{
    /**
     *
     *
     */
    public AnyNode(SchemaUIContext context, 
		SchemaComponentReference<AnyElement> reference,
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
		return NbBundle.getMessage(AnyNode.class,
			"LBL_AnyNode_TypeDisplayName"); // NOI18N
	}

    @Override
    protected Sheet createSheet()
    {
        Sheet sheet = super.createSheet();
        Sheet.Set set = sheet.get(Sheet.PROPERTIES);
        try {
            
            // maxOccurs
                // maxOccurs
                Property maxOccursProp = new BaseSchemaProperty(
                        getReference().get(), // schema component
                        String.class,
                        AnyElement.MAX_OCCURS_PROPERTY,
                        NbBundle.getMessage(AnyNode.class,"PROP_MaxOccurs_DisplayName"), // display name
                        NbBundle.getMessage(AnyNode.class,"PROP_MaxOccurs_ShortDescription"),	// descr
                        MaxOccursEditor.class
                        );
            set.put(new SchemaModelFlushWrapper(getReference().get(), maxOccursProp));
            
            // minOccurs
            Property minOccursProp = new NonNegativeIntegerProperty(
                    getReference().get(), // schema component
                    AnyElement.MIN_OCCURS_PROPERTY,
                    NbBundle.getMessage(AnyNode.class,"PROP_MinOccurs_DisplayName"), // display name
                    NbBundle.getMessage(AnyNode.class,"PROP_MinOccurs_ShortDescription")	// descr
                    );
            set.put(new SchemaModelFlushWrapper(getReference().get(), minOccursProp));
            
            // processContents
            Property processContentsProp = new BaseSchemaProperty(
                    getReference().get(), // schema component
                    AnyElement.ProcessContents.class, // Any.ProcessContents.class as value type
                    AnyElement.PROCESS_CONTENTS_PROPERTY,
                    NbBundle.getMessage(AnyNode.class,"PROP_ProcessContentsProp_DisplayName"), // display name
                    NbBundle.getMessage(AnyNode.class,"PROP_ProcessContentsProp_ShortDescription"),	// descr
                    ProcessContentsEditor.class);
            set.put(new SchemaModelFlushWrapper(getReference().get(), processContentsProp));
            
            // namespace
            Property namespaceProp = new BaseSchemaProperty(
                    getReference().get(), // schema component
                    String.class, // Any.ProcessContents.class as value type
                    AnyElement.NAMESPACE_PROPERTY,
                    NbBundle.getMessage(AnyNode.class,"PROP_Namespace_DisplayName"), // display name
                    NbBundle.getMessage(AnyNode.class,"HINT_Namespace_ShortDesc"),	// descr
                    AnyNamespaceEditor.class);
            set.put(new SchemaModelFlushWrapper(getReference().get(), namespaceProp));
            
        } catch (NoSuchMethodException nsme) {
            assert false : "properties should be defined";
        }
        
        return sheet;
    }
}
