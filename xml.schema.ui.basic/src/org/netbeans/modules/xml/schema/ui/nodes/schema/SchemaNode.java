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

import java.awt.datatransfer.Transferable;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

//local imports
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.model.Form;
import org.netbeans.modules.xml.schema.ui.basic.editors.FormPropertyEditor;
import org.netbeans.modules.xml.schema.ui.basic.editors.StringEditor;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.BaseSchemaProperty;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.DerivationTypeProperty;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.NamespaceProperty;
import org.netbeans.modules.xml.schema.ui.nodes.*;

/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class SchemaNode extends SchemaComponentNode<Schema>
{
    /**
     *
     *
     */
    public SchemaNode(SchemaUIContext context, 
		SchemaComponentReference<Schema> reference,
		Children children)
    {
        super(context,reference,children);
		setIconBaseWithExtension(
			"org/netbeans/modules/xml/schema/resources/"+
			"Schema_File.png");
    }


	/**
	 *
	 *
	 */
	protected void updateDisplayName()
	{
        String name = getReference().get().getTargetNamespace();
        if (name == null) {
            name = NbBundle.getMessage(SchemaNode.class,
				"LBL_SchemaNode_NoTargetNamespace");
        }

		setDisplayName(name);
	}


	/**
	 *
	 *
	 */
	@Override
	public String getTypeDisplayName()
	{
		return NbBundle.getMessage(SchemaNode.class,
			"LBL_SchemaNode_TypeDisplayName"); // NOI18N
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
            // attribute form property
            Node.Property attrFormProp = new BaseSchemaProperty(
                    getReference().get(), // schema component
                    Form.class, // value type
                    Schema.ATTRIBUTE_FORM_DEFAULT_PROPERTY, //property name
                    NbBundle.getMessage(SchemaNode.class,"PROP_AttributeFormDefault_DisplayName"), // display name
                    NbBundle.getMessage(SchemaNode.class,"PROP_AttributeFormDefault_ShortDescription"),	// descr
                    FormPropertyEditor.SchemaFormPropertyEditor.class); // editor class
            props.put(new SchemaModelFlushWrapper(getReference().get(),attrFormProp));

            // element form property
            Node.Property elemFormProp = new BaseSchemaProperty(
                    getReference().get(), // schema component
                    Form.class, // value type
                    Schema.ELEMENT_FORM_DEFAULT_PROPERTY, //property name
                    NbBundle.getMessage(SchemaNode.class,"PROP_ElementFormDefault_DisplayName"), // display name
                    NbBundle.getMessage(SchemaNode.class,"PROP_ElementFormDefault_ShortDescription"),	// descr
                    FormPropertyEditor.SchemaFormPropertyEditor.class); // editor class
            props.put(new SchemaModelFlushWrapper(getReference().get(),elemFormProp));

            // block default property
            Node.Property blockDefaultProp = new DerivationTypeProperty(
                    getReference().get(),
                    Schema.BLOCK_DEFAULT_PROPERTY,
                    NbBundle.getMessage(SchemaNode.class,"PROP_BlockDefault_DisplayName"), // display name
                    NbBundle.getMessage(SchemaNode.class,"HINT_BlockDefault_ShortDesc"),	// descr
                    getTypeDisplayName()
                    );
            props.put(new SchemaModelFlushWrapper(getReference().get(), blockDefaultProp));

            // final default property
            Node.Property finalDefaultProp = new DerivationTypeProperty(
                    getReference().get(),
                    Schema.FINAL_DEFAULT_PROPERTY,
                    NbBundle.getMessage(SchemaNode.class,"PROP_FinalDefault_DisplayName"), // display name
                    NbBundle.getMessage(SchemaNode.class,"HINT_FinalDefault_ShortDesc"),	// descr
                    getTypeDisplayName()
                    );
            props.put(new SchemaModelFlushWrapper(getReference().get(), finalDefaultProp));

            // version property
            Node.Property versionProp = new BaseSchemaProperty(
                    getReference().get(),
                    String.class,
                    Schema.VERSION_PROPERTY,
                    NbBundle.getMessage(SchemaNode.class,"PROP_Version_DisplayName"), // display name
                    NbBundle.getMessage(SchemaNode.class,"PROP_Version_ShortDescription"),	// descr
                    StringEditor.class
                    );
            props.put(new SchemaModelFlushWrapper(getReference().get(), versionProp));

            // version property
            Node.Property tnsProp = new NamespaceProperty(
                    getReference().get(),
                    Schema.TARGET_NAMESPACE_PROPERTY,
                    NbBundle.getMessage(SchemaNode.class,"PROP_TargetNamespace_DisplayName"), // display name
                    NbBundle.getMessage(SchemaNode.class,"PROP_TargetNamespace_ShortDescription"),	// descr
                    getTypeDisplayName() // type display name
                    ) {
                public void setValue(Object o) throws IllegalAccessException, InvocationTargetException {
                    if(o instanceof String && "".equals(o)) {
                        super.setValue(null);
                    } else super.setValue(o);
                }
            };
            props.put(new SchemaModelFlushWrapper(getReference().get(), tnsProp));
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

    @Override
    protected void createPasteTypes(Transferable transferable, List list) {
        // We do not allow pasting on the root node.
        list.clear();
    }

    @Override
    public boolean canCut() {
        return false;
    }

    @Override
    public boolean canCopy() {
        return false;
    }
}
