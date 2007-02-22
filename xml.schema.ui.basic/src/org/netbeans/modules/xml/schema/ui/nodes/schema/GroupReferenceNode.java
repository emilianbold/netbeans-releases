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

import org.netbeans.modules.xml.schema.model.GlobalGroup;
import org.netbeans.modules.xml.schema.model.GroupReference;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.ui.basic.editors.MaxOccursEditor;
import org.netbeans.modules.xml.schema.ui.nodes.*;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.BaseSchemaProperty;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.GlobalReferenceProperty;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.NonNegativeIntegerProperty;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class GroupReferenceNode extends SchemaComponentNode<GroupReference>
{
 	private static final String NAME = "group";
    /**
     *
     *
     */
    public GroupReferenceNode(SchemaUIContext context, 
		SchemaComponentReference<GroupReference> reference,
		Children children)
    {
        super(context,reference,children);

		setIconBaseWithExtension(
			"org/netbeans/modules/xml/schema/ui/nodes/resources/groupAlias2.png");
    }


	/**
	 *
	 *
	 */
	@Override
	public String getTypeDisplayName()
	{
		return NbBundle.getMessage(GroupReferenceNode.class,
			"LBL_GroupReferenceNode_TypeDisplayName"); // NOI18N
	}
	 
	@Override
	protected GlobalGroup getSuperDefinition()
	{
		GroupReference sc = getReference().get();
		GlobalGroup gt = null;
		if(sc.getRef()!=null)
			gt = sc.getRef().get();
		return gt;
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
			
            Node.Property minOccursProp = new NonNegativeIntegerProperty(
                    getReference().get(),		// SchemaComponent
                    GroupReference.MIN_OCCURS_PROPERTY,
                    NbBundle.getMessage(GroupReferenceNode.class,"PROP_MinOccurs_DisplayName"), // display name
                    NbBundle.getMessage(GroupReferenceNode.class,"PROP_MinOccurs_ShortDescription")	// descr
                    );
            props.put(new SchemaModelFlushWrapper(getReference().get(), minOccursProp));

                // maxOccurs
                Property maxOccursProp = new BaseSchemaProperty(
                        getReference().get(), // schema component
                        String.class,
                        GroupReference.MAX_OCCURS_PROPERTY,
                        NbBundle.getMessage(GroupReferenceNode.class,"PROP_MaxOccurs_DisplayName"), // display name
                        NbBundle.getMessage(GroupReferenceNode.class,"PROP_MaxOccurs_ShortDescription"),	// descr
                        MaxOccursEditor.class
                        );
            props.put(new SchemaModelFlushWrapper(getReference().get(), maxOccursProp));

            //reference property
            Node.Property refProp = new GlobalReferenceProperty<GlobalGroup>(
                    getReference().get(),
                    GroupReference.REF_PROPERTY,
                    NbBundle.getMessage(GroupReferenceNode.class,
                    "PROP_Reference_DisplayName"), // display name
                    NbBundle.getMessage(GroupReferenceNode.class,
                    "HINT_Group_Reference"),	// descr
                    getTypeDisplayName(), // type display name
                    NbBundle.getMessage(GroupReferenceNode.class,
                    "LBL_GlobalGroupNode_TypeDisplayName"), // reference type display name
                    GlobalGroup.class
                    );

                props.put(new SchemaModelFlushWrapper(getReference().get(), refProp));
         } catch (NoSuchMethodException nsme) {
                assert false : "properties should be defined";
            }
			
            return sheet;
    }

	/**
	 * The display name is name of reference if present
	 *
	 */
	protected void updateDisplayName()
	{
		GlobalGroup ref = getSuperDefinition(); 
		String name = ref==null?null:ref.getName();
		if(name==null||name.equals(""))
			name = NAME;
		setDisplayName(name);
	}
}
