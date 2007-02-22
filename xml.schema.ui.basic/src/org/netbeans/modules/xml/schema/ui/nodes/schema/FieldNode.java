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

import java.beans.PropertyChangeEvent;
import org.netbeans.modules.xml.schema.model.Field;
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
public class FieldNode extends SchemaComponentNode<Field>
{
    /**
     *
     *
     */
    public FieldNode(SchemaUIContext context,
		SchemaComponentReference<Field> reference,
		Children children)
    {
        super(context,reference,children);
	setIconBaseWithExtension(
			"org/netbeans/modules/xml/schema/ui/nodes/resources/field.png");
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
        try {
            // xpath property
            Node.Property xpathProp = new BaseSchemaProperty(
                    getReference().get(), // schema component
                    String.class,
                    Field.XPATH_PROPERTY, // property name
                    NbBundle.getMessage(FieldNode.class,"PROP_XPath_DisplayName"), // display name
                    NbBundle.getMessage(FieldNode.class,"PROP_XPath_ShortDescription"),	// descr
                    null
                    );
            props.put(new SchemaModelFlushWrapper(getReference().get(), xpathProp));
        
        } catch (NoSuchMethodException nsme) {
            assert false : "properties should be defined";
        }
        
        return sheet;
    }

    /**
	 *
	 *
	 */
	@Override
	public String getTypeDisplayName()
	{
		return NbBundle.getMessage(FieldNode.class,
			"LBL_FieldNode_TypeDisplayName"); // NOI18N
	}

    public void propertyChange(PropertyChangeEvent event) {
        super.propertyChange(event);
        if (isValid() && event.getSource() == getReference().get() &&
                Field.XPATH_PROPERTY.equalsIgnoreCase(event.getPropertyName())) {
            firePropertyChange(Field.XPATH_PROPERTY,event.getOldValue(),
                    event.getNewValue());
        }
    }
}
