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

import org.netbeans.modules.xml.schema.model.AttributeReference;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.ui.nodes.*;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.BaseSchemaProperty;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.DefaultProperty;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.FixedProperty;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.GlobalReferenceProperty;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class AttributeReferenceNode extends SchemaComponentNode<AttributeReference>
{
 	private static final String NAME = "attribute";
    /**
     *
     *
     */
    public AttributeReferenceNode(SchemaUIContext context,
	    SchemaComponentReference<AttributeReference> reference,
	    Children children) {
	super(context,reference,children);

	setIconBaseWithExtension(
		"org/netbeans/modules/xml/schema/ui/nodes/resources/"+
		"attribute_reference.png");
    }
    
    
    /**
     *
     *
     */
    @Override
	    public String getTypeDisplayName() {
	return NbBundle.getMessage(LocalAttributeNode.class,
		"LBL_AttributeReferenceNode_TypeDisplayName"); // NOI18N
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
		    NbBundle.getMessage(AttributeReferenceNode.class,"PROP_Fixed_DisplayName"), // display name
		    NbBundle.getMessage(AttributeReferenceNode.class,"PROP_Fixed_ShortDescription")	// descr
		    );
	    set.put(new SchemaModelFlushWrapper(getReference().get(),fixedProp));
	    
	    // default property
	    Node.Property defaultProp = new DefaultProperty(
		    getReference().get(), // schema component
		    NbBundle.getMessage(AttributeReferenceNode.class,"PROP_Default_DisplayName"), // display name
		    NbBundle.getMessage(AttributeReferenceNode.class,"PROP_Default_ShortDescription")	// descr
		    );
	    set.put(new SchemaModelFlushWrapper(getReference().get(),defaultProp));
	    
        // use property
	    Node.Property useProp = new BaseSchemaProperty(
		    getReference().get(), // schema component
			AttributeReference.Use.class, //as value type
			AttributeReference.USE_PROPERTY, //property name	
		    NbBundle.getMessage(AttributeReferenceNode.class,"PROP_Use_DisplayName"), // display name
		    NbBundle.getMessage(AttributeReferenceNode.class,"PROP_Use_ShortDescription"),	// descr
		    LocalAttributeNode.UseEditor.class);
	    set.put(new SchemaModelFlushWrapper(getReference().get(),useProp));
	    
        //reference property
        Node.Property refProp = new GlobalReferenceProperty<GlobalAttribute>(
                getReference().get(),
                AttributeReference.REF_PROPERTY,
                NbBundle.getMessage(AttributeReferenceNode.class,
                "PROP_Reference_DisplayName"), // display name
                NbBundle.getMessage(AttributeReferenceNode.class,
                "HINT_Attribute_Reference"),	// descr
                getTypeDisplayName(), // type display name
                NbBundle.getMessage(AttributeReferenceNode.class,
                "LBL_GlobalAttributeNode_TypeDisplayName"),	// reference type display name
                GlobalAttribute.class
                );
        set.put(new SchemaModelFlushWrapper(getReference().get(), refProp));
	    
        // remove name property
	    set.remove(GlobalAttribute.NAME_PROPERTY);
	} catch (NoSuchMethodException nsme) {
	    assert false : "properties should be defined";
	}
	
	return sheet;
    }
    
	@Override
	protected GlobalAttribute getSuperDefinition()
	{
		AttributeReference sc = getReference().get();
		GlobalAttribute gt = null;
		if(sc.getRef()!=null)
			gt = sc.getRef().get();
		return gt;
	}

	/**
	 *
	 *
	 */
	public String getHtmlDisplayName()
	{
		String decoration=" (-&gt;)";
		String name = getDefaultDisplayName()+" <font color='#999999'>"+decoration+"</font>";
		return applyHighlights(name);
	}
	
	/**
	 * The display name is name of reference if present
	 *
	 */
	protected void updateDisplayName()
	{
		GlobalAttribute ref = getSuperDefinition(); 
		String name = ref==null?null:ref.getName();
		if(name==null||name.equals(""))
			name = NAME;
		setDisplayName(name);
	}

}
