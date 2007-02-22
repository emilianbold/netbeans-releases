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

import org.openide.nodes.Children;
import org.openide.nodes.Node.Property;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

import org.netbeans.modules.xml.schema.model.Enumeration;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaComponentNode;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaModelFlushWrapper;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaUIContext;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.BaseSchemaProperty;

/**
 * 
 * @author Todd Fast, todd.fast@sun.com
 * @author Ajit Bhate
 */
public class EnumerationNode extends SchemaComponentNode<Enumeration>
{
    /**
     *
     *
     */
    public EnumerationNode(SchemaUIContext context,
            SchemaComponentReference<Enumeration> reference,
            Children children) {
        super(context,reference,children);
	setIconBaseWithExtension(
			"org/netbeans/modules/xml/schema/ui/nodes/resources/enumeration.png");
    }
    
    
    /**
     *
     *
     */
    protected void updateDisplayName() {
        setDisplayName("\""+getReference().get().getValue()+"\"");
    }
    
    
    /**
     *
     *
     */
    @Override
    public String getTypeDisplayName() {
        return NbBundle.getMessage(EnumerationNode.class,
                "LBL_EnumerationNode_TypeDisplayName"); // NOI18N
    }
    
    @Override
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        Sheet.Set props = sheet.get(Sheet.PROPERTIES);
        if (props == null) {
            props = Sheet.createPropertiesSet();
            sheet.put(props);
        }
        try {
            // Fixed property
            Property fixedProp = new BaseSchemaProperty(
                    getReference().get(), // schema component
                    String.class, // value type
                    Enumeration.VALUE_PROPERTY, // property name
                    NbBundle.getMessage(EnumerationNode.class,"PROP_Facet_Value_DisplayName"), // display name
                    NbBundle.getMessage(EnumerationNode.class,"PROP_Enumeration_Value_ShortDescription"),	// descr
                    null
                    );
            props.put(new SchemaModelFlushWrapper(getReference().get(), fixedProp));
        } catch (NoSuchMethodException nsme) {
            assert false : "properties should be defined";
        }
        
        return sheet;
    }
}
