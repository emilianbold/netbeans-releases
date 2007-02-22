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

import org.netbeans.modules.xml.schema.model.AttributeGroupReference;
import org.netbeans.modules.xml.schema.model.GlobalAttributeGroup;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.ui.nodes.*;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.GlobalReferenceProperty;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 * @author  Jeri Lockhart
 */
public class AttributeGroupReferenceNode extends SchemaComponentNode<AttributeGroupReference>
		
{
	
 	private static final String NAME = "attributeGroup";
	
    /**
     *
     *
     */
    public AttributeGroupReferenceNode(SchemaUIContext context, 
		SchemaComponentReference<AttributeGroupReference> reference,
		Children children)
    {
        super(context,reference,children);

		setIconBaseWithExtension(
			"org/netbeans/modules/xml/schema/ui/nodes/resources/"+
			"attributeGroupAlias2.png");
    }

	/**
	 *
	 *
	 */
	protected void updateDisplayName()
	{
            String name = getSuperDefinitionName();
            if(name==null||name.equals(""))
                name = NAME;
            setDisplayName(name);
	}


	/**
	 *
	 *
	 */
    public String getHtmlDisplayName() {
		String decoration=" (-&gt;)";
                String name = getDefaultDisplayName()+" <font color='#999999'>"+decoration+"</font>";
                return applyHighlights(name);
    }

	/**
	 *
	 *
	 */
	@Override
	public String getTypeDisplayName()
	{
		return NbBundle.getMessage(AttributeGroupReferenceNode.class,
			"LBL_AttributeGroupReferenceNode_TypeDisplayName"); // NOI18N
	}
	
	 
	@Override
	protected GlobalAttributeGroup getSuperDefinition()
	{
		AttributeGroupReference sc = getReference().get();
		GlobalAttributeGroup gt = null;
		if(sc.getGroup()!=null)
			gt = sc.getGroup().get();
		return gt;
	}
        
        private String getSuperDefinitionName()
        {
            String rawString = null;
            AttributeGroupReference sc = getReference().get();
            GlobalAttributeGroup gt = null;
            if(sc.getGroup()!=null)
                rawString = sc.getGroup().getRefString();
            int i = rawString!=null?rawString.indexOf(':'):-1;
            if (i != -1 && i < rawString.length()) {
                rawString = rawString.substring(i);
            }
            return rawString;
        }
    
	@Override
    protected Sheet createSheet() {
		Sheet sheet = null;
//        try {
			sheet = super.createSheet();
			Sheet.Set props = sheet.get(Sheet.PROPERTIES);
			if (props == null) {
				props = Sheet.createPropertiesSet();
				sheet.put(props);
			}
			
            try {
                //reference property
                Node.Property refProp = new GlobalReferenceProperty<GlobalAttributeGroup>(
                        getReference().get(),
                        AttributeGroupReference.GROUP_PROPERTY,
                        NbBundle.getMessage(AttributeGroupReferenceNode.class,
                        "PROP_Reference_DisplayName"), // display name
                        NbBundle.getMessage(AttributeGroupReferenceNode.class,
                        "HINT_Attr_Group_Reference"),	// descr
                        getTypeDisplayName(), // type display name
                        NbBundle.getMessage(AttributeGroupReferenceNode.class,
                        "LBL_GlobalAttributeGroupNode_TypeDisplayName"), // reference type display name
                        GlobalAttributeGroup.class
                        );

                props.put(new SchemaModelFlushWrapper(getReference().get(), refProp));
            } catch (NoSuchMethodException nsme) {
                assert false:"properties must be defined";
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
