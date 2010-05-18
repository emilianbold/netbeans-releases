/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
