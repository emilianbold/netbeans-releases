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

import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.ui.basic.editors.MaxOccursEditor;
import org.netbeans.modules.xml.schema.ui.nodes.*;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.BaseSchemaProperty;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.BooleanProperty;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.DefaultProperty;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.DerivationTypeProperty;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.FixedProperty;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.NonNegativeIntegerProperty;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.FormProperty;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class LocalElementNode extends SchemaComponentNode<LocalElement>
{
    /**
     *
     *
     */
    public LocalElementNode(SchemaUIContext context,
	SchemaComponentReference<LocalElement> reference,
	Children children) {
	super(context,reference,children);
	
	setIconBaseWithExtension(
	    "org/netbeans/modules/xml/schema/ui/nodes/resources/element.png");
    }
    
    
    /**
     *
     *
     */
    @Override
	public String getTypeDisplayName() {
	return NbBundle.getMessage(LocalElementNode.class,
	    "LBL_LocalElementNode_TypeDisplayName"); // NOI18N
    }
    
	@Override
	protected GlobalType getSuperDefinition()
	{
		LocalElement sc = getReference().get();
		GlobalType gt = null;
		if(sc.getType()!=null)
			gt = sc.getType().get();
		return gt;
	}
	
    @Override
	protected Sheet createSheet() {
	Sheet sheet = super.createSheet();
	Sheet.Set set = sheet.get(Sheet.PROPERTIES);
	try {
	    // The methods are used because the Node.Property support for
	    // netbeans doesn't recognize the is.. for boolean properties
	    
	    // nillable property
	    Node.Property nillableProp = new BooleanProperty(
		getReference().get(), // schema component
		LocalElement.NILLABLE_PROPERTY, // property name
		NbBundle.getMessage(LocalElementNode.class,"PROP_Nillable_DisplayName"), // display name
		NbBundle.getMessage(LocalElementNode.class,"PROP_Nillable_ShortDescription"),	// descr
		true // default value is false
		);
	    set.put(new SchemaModelFlushWrapper(getReference().get(),nillableProp));
	    
	    // fixed property
	    Node.Property fixedProp = new FixedProperty(
		getReference().get(), // schema component
		NbBundle.getMessage(LocalElementNode.class,"PROP_Fixed_DisplayName"), // display name
		NbBundle.getMessage(LocalElementNode.class,"PROP_Fixed_ShortDescription")	// descr
		);
	    set.put(new SchemaModelFlushWrapper(getReference().get(),fixedProp));
	    
	    // default property
	    Node.Property defaultProp = new DefaultProperty(
		getReference().get(), // schema component
		NbBundle.getMessage(LocalElementNode.class,"PROP_Default_DisplayName"), // display name
		NbBundle.getMessage(LocalElementNode.class,"PROP_Default_ShortDescription")	// descr
		);
	    set.put(new SchemaModelFlushWrapper(getReference().get(),defaultProp));
	    
	    if (getReference().get().allowsFullMultiplicity()) {
		
		// maxOccurs
		Property maxOccursProp = new BaseSchemaProperty(
		    getReference().get(), // schema component
		    String.class,
		    LocalElement.MAX_OCCURS_PROPERTY,
		    NbBundle.getMessage(LocalElementNode.class,"PROP_MaxOccurs_DisplayName"), // display name
		    NbBundle.getMessage(LocalElementNode.class,"PROP_MaxOccurs_ShortDescription"),	// descr
		    MaxOccursEditor.class
		    );
		set.put(new SchemaModelFlushWrapper(getReference().get(), maxOccursProp));
		
	    }
	    //TODO
	    // if (getReference().get().allowsFullMultiplicity()) {
	    // Add code here to support only zero or one for min occurs
	    // }
	    
	    // minOccurs
	    Property minOccursProp = new NonNegativeIntegerProperty(
		getReference().get(), // schema component
		LocalElement.MIN_OCCURS_PROPERTY,
		NbBundle.getMessage(LocalElementNode.class,"PROP_MinOccurs_DisplayName"), // display name
		NbBundle.getMessage(LocalElementNode.class,"PROP_MinOccurs_ShortDescription")	// descr
		);
	    set.put(new SchemaModelFlushWrapper(getReference().get(), minOccursProp));
	    
        // form property
        Node.Property formProp = new FormProperty(
                getReference().get(), // schema component
                LocalElement.FORM_PROPERTY, //property name
                NbBundle.getMessage(LocalElementNode.class,"PROP_Form_DisplayName"), // display name
                NbBundle.getMessage(LocalElementNode.class,"PROP_Form_ElementShortDescription")	// descr
                );
	    set.put(new SchemaModelFlushWrapper(getReference().get(),formProp));
	    
	    // block property
	    Node.Property blockProp = new DerivationTypeProperty(
	    		getReference().get(),
			    LocalElement.BLOCK_PROPERTY,
			    NbBundle.getMessage(LocalElementNode.class,"PROP_Block_DisplayName"), // display name
			    NbBundle.getMessage(LocalElementNode.class,"HINT_Block_ShortDesc"),	// descr
			    getTypeDisplayName()
			    );
	    set.put(new SchemaModelFlushWrapper(getReference().get(), blockProp));
	    
	    
	} catch (NoSuchMethodException nsme) {
	    assert false : "properties should be defined";
	}
	
	return sheet;
    }

}
