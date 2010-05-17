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
