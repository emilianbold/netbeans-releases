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

import org.netbeans.modules.xml.schema.model.Cardinality;
import org.netbeans.modules.xml.schema.model.Choice;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.ui.basic.editors.MaxOccursEditor;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaComponentNode;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaModelFlushWrapper;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaUIContext;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.BaseSchemaProperty;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.NonNegativeIntegerProperty;
import org.openide.nodes.Children;
import org.openide.nodes.Node.Property;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 * @author  Jeri Lockhart
 */
public class ChoiceNode extends SchemaComponentNode<Choice>
{
    /**
     *
     *
     */
    public ChoiceNode(SchemaUIContext context, 
		SchemaComponentReference<Choice> reference,
		Children children)
    {
        super(context,reference,children);

		setIconBaseWithExtension(
			"org/netbeans/modules/xml/schema/ui/nodes/resources/"+
			"choice.png");

		setDefaultExpanded(true);
    }


	/**
	 *
	 *
	 */
	@Override
	public String getTypeDisplayName()
	{
		return NbBundle.getMessage(ChoiceNode.class,
			"LBL_ChoiceNode_TypeDisplayName"); // NOI18N
	}
             	 
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
	
	    Cardinality choiceCardinality = getReference().get().getCardinality();
            
	    if (choiceCardinality != null) {
		// minOccurs
		Property minOccursProp = new NonNegativeIntegerProperty(
		    getReference().get(), // schema component
		    Choice.MIN_OCCURS_PROPERTY,
		    NbBundle.getMessage(ChoiceNode.class,"PROP_MinOccurs_DisplayName"), // display name
		    NbBundle.getMessage(ChoiceNode.class,"PROP_MinOccurs_ShortDescription")	// descr
		    );
		props.put(new SchemaModelFlushWrapper(getReference().get(), minOccursProp));
		
		// maxOccurs
		Property maxOccursProp = new BaseSchemaProperty(
		    getReference().get(), // schema component
		    String.class,
		    Choice.MAX_OCCURS_PROPERTY,
		    NbBundle.getMessage(ChoiceNode.class,"PROP_MaxOccurs_DisplayName"), // display name
		    NbBundle.getMessage(ChoiceNode.class,"PROP_MaxOccurs_ShortDescription"),	// descr
		    MaxOccursEditor.class
		    );
		props.put(new SchemaModelFlushWrapper(getReference().get(), maxOccursProp));
	    }
            
        } catch (NoSuchMethodException ex) {
            assert false : "properties should be defined";
        }
        return sheet;
    }
}
