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

import org.netbeans.modules.xml.schema.model.FractionDigits;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.ui.nodes.*;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.BooleanProperty;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.NonNegativeIntegerProperty;
import org.openide.nodes.Children;
import org.openide.nodes.Node.Property;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class FractionDigitsNode extends SchemaComponentNode<FractionDigits>
{
    /**
     *
     *
     */
    public FractionDigitsNode(SchemaUIContext context, 
		SchemaComponentReference<FractionDigits> reference,
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
		setDisplayName(String.valueOf(getReference().get().getValue()));
	}


	/**
	 *
	 *
	 */
	@Override
	public String getTypeDisplayName()
	{
		return NbBundle.getMessage(FractionDigitsNode.class,
			"LBL_FractionDigitsNode_TypeDisplayName"); // NOI18N
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
            // Fixed property
            Property fixedProp = new BooleanProperty(
                    getReference().get(), // schema component
                    FractionDigits.FIXED_PROPERTY, // property name
                    NbBundle.getMessage(FractionDigitsNode.class,"PROP_Facet_Fixed_DisplayName"), // display name
                    NbBundle.getMessage(FractionDigitsNode.class,"PROP_Facet_Fixed_ShortDescription"),	// descr
                    true // default value is false
                    );
            props.put(new SchemaModelFlushWrapper(getReference().get(), fixedProp));
            
            // Value property
            Property valueProp = new NonNegativeIntegerProperty.Primitive(
                    getReference().get(), // schema component
                    FractionDigits.VALUE_PROPERTY,
                    NbBundle.getMessage(FractionDigitsNode.class,"PROP_Facet_Value_DisplayName"), // display name
                    NbBundle.getMessage(FractionDigitsNode.class,"PROP_Facet_Value_ShortDescription")	// descr
                    );
            props.put(new SchemaModelFlushWrapper(getReference().get(), valueProp));
        } catch (NoSuchMethodException nsme) {
            assert false : "properties should be defined";
        }
        
//			PropertiesNotifier.addChangeListener(listener = new
//					ChangeListener() {
//				public void stateChanged(ChangeEvent ev) {
//					firePropertyChange("value", null, null);
//				}
//			});
        return sheet;
    }
}
