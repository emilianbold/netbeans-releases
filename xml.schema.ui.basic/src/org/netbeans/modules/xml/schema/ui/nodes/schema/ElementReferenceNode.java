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

import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.ui.basic.editors.MaxOccursEditor;
import org.netbeans.modules.xml.schema.ui.nodes.*;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.BaseSchemaProperty;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.GlobalReferenceProperty;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.NonNegativeIntegerProperty;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 * @author  Nathan Fiedler
 */
public class ElementReferenceNode extends SchemaComponentNode<ElementReference>
{
 	private static final String NAME = "element";
    /**
     *
     *
     */
    public ElementReferenceNode(SchemaUIContext context,
	    SchemaComponentReference<ElementReference> reference,
	    Children children) {
		super(context,reference,children);

		setIconBaseWithExtension(
			"org/netbeans/modules/xml/schema/ui/nodes/resources/element_reference.png");
    }


	@Override
	protected GlobalElement getSuperDefinition()
	{
		ElementReference sc = getReference().get();
		GlobalElement gt = null;
		if(sc.getRef()!=null)
			gt = sc.getRef().get();
		return gt;
	}
	
    /**
     *
     *
     */
    @Override
    public String getTypeDisplayName() {
		return NbBundle.getMessage(LocalElementNode.class,
			"LBL_ElementReferenceNode_TypeDisplayName"); // NOI18N
    }
    
    @Override
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        Sheet.Set set = sheet.get(Sheet.PROPERTIES);
        try {
            // The methods are used because the Node.Property support for
            // netbeans doesn't recognize the is.. for boolean properties

            if (getReference().get().allowsFullMultiplicity()) {

                // maxOccurs
                Property maxOccursProp = new BaseSchemaProperty(
                        getReference().get(), // schema component
                        String.class,
                        ElementReference.MAX_OCCURS_PROPERTY,
                        NbBundle.getMessage(ElementReferenceNode.class,"PROP_MaxOccurs_DisplayName"), // display name
                        NbBundle.getMessage(ElementReferenceNode.class,"PROP_MaxOccurs_ShortDescription"),	// descr
                        MaxOccursEditor.class
                        );
                set.put(new SchemaModelFlushWrapper(getReference().get(), maxOccursProp));

            }

            //TODO
            // if (getReference().get().allowsFullMultiplicity()) {
            // Add code here to support only zero or one for min occurs
            ///
            // }

            // minOccurs
            Property minOccursProp = new NonNegativeIntegerProperty(
                    getReference().get(), // schema component
                    LocalElement.MIN_OCCURS_PROPERTY,
                    NbBundle.getMessage(ElementReferenceNode.class,"PROP_MinOccurs_DisplayName"), // display name
                    NbBundle.getMessage(ElementReferenceNode.class,"PROP_MinOccurs_ShortDescription")	// descr
                    );
            set.put(new SchemaModelFlushWrapper(getReference().get(), minOccursProp));

            //reference property
            Node.Property refProp = new GlobalReferenceProperty<GlobalElement>(
                    getReference().get(),
                    LocalElement.REF_PROPERTY,
                    NbBundle.getMessage(ElementReferenceNode.class,
                    "PROP_Reference_DisplayName"), // display name
                    NbBundle.getMessage(ElementReferenceNode.class,
                    "HINT_Element_Reference"),	// descr
                    getTypeDisplayName(), // type display name
                    NbBundle.getMessage(ElementReferenceNode.class,
                    "LBL_GlobalElementNode_TypeDisplayName"), // reference type display name
                    GlobalElement.class
                    );
            set.put(new SchemaModelFlushWrapper(getReference().get(), refProp));

            // remove name property
            set.remove(LocalElement.NAME_PROPERTY);
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
	public String getHtmlDisplayName()
	{
		ElementReference element=getReference().get();
		
		String max=element.getMaxOccursEffective();
		if (max.equals("unbounded"))
			max="*";
		
		String decoration="["+element.getMinOccursEffective()+".."+max+"]"+" (-&gt;)";
		String name = getDefaultDisplayName()+" <font color='#999999'>"+decoration+"</font>";
		return applyHighlights(name);
	}

	/**
	 * The display name is name of reference if present
	 *
	 */
	protected void updateDisplayName()
	{
		GlobalElement ref = getSuperDefinition(); 
		String name = ref==null?null:ref.getName();
		if(name==null||name.equals(""))
			name = NAME;
		setDisplayName(name);
	}
	
}
