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

import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.ui.nodes.*;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.BooleanProperty;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.DefaultProperty;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.DerivationTypeProperty;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.FixedProperty;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.GlobalReferenceProperty;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class GlobalElementNode extends SchemaComponentNode<GlobalElement>
{
    /**
     *
     *
     */
    public GlobalElementNode(SchemaUIContext context, 
		SchemaComponentReference<GlobalElement> reference,
		Children children)
    {
        super(context,reference,children);

		setIconBaseWithExtension(
			"org/netbeans/modules/xml/schema/ui/nodes/resources/element.png");
    }


	/**
	 *
	 *
	 */
	@Override
	public String getTypeDisplayName()
	{
		return NbBundle.getMessage(GlobalElementNode.class,
			"LBL_GlobalElementNode_TypeDisplayName"); // NOI18N
	}
        
	@Override
	protected GlobalType getSuperDefinition()
	{
		GlobalElement sc = getReference().get();
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
                
                // block, final needs custom editor
                
                // Abstract property
                Property abstractProp = new BooleanProperty(
                        getReference().get(), // schema component
                        GlobalElement.ABSTRACT_PROPERTY, // property name
                        NbBundle.getMessage(GlobalElementNode.class,"PROP_Abstract_DisplayName"), // display name
                        NbBundle.getMessage(GlobalElementNode.class,"PROP_Abstract_ShortDescription"),	// descr
                        true // default value is false
                        );
                set.put(new SchemaModelFlushWrapper(getReference().get(), abstractProp));
                
                // nillable property
                Node.Property nillableProp = new BooleanProperty(
                        getReference().get(), // schema component
                        GlobalElement.NILLABLE_PROPERTY, // property name
                        NbBundle.getMessage(GlobalElementNode.class,"PROP_Nillable_DisplayName"), // display name
                        NbBundle.getMessage(GlobalElementNode.class,"PROP_Nillable_ShortDescription"),	// descr
                        true // default value is false
                        );
                set.put(new SchemaModelFlushWrapper(getReference().get(),nillableProp));
                
                // fixed property
                Node.Property fixedProp = new FixedProperty(
                        getReference().get(), // schema component
                        NbBundle.getMessage(GlobalElementNode.class,"PROP_Fixed_DisplayName"), // display name
                        NbBundle.getMessage(GlobalElementNode.class,"PROP_Fixed_ShortDescription")	// descr
                        );
                set.put(new SchemaModelFlushWrapper(getReference().get(),fixedProp));
                
                // default property
                Node.Property defaultProp = new DefaultProperty(
                        getReference().get(), // schema component
                        NbBundle.getMessage(GlobalElementNode.class,"PROP_Default_DisplayName"), // display name
                        NbBundle.getMessage(GlobalElementNode.class,"PROP_Default_ShortDescription")	// descr
                        );
                set.put(new SchemaModelFlushWrapper(getReference().get(),defaultProp));

                // final property
                Node.Property finalProp = new DerivationTypeProperty(
                        getReference().get(),
                        GlobalElement.FINAL_PROPERTY,
                        NbBundle.getMessage(GlobalElementNode.class,"PROP_Final_DisplayName"), // display name
                        NbBundle.getMessage(GlobalElementNode.class,"HINT_Final_ShortDesc"),	// descr
                        getTypeDisplayName()
                        );
                set.put(new SchemaModelFlushWrapper(getReference().get(), finalProp));
                
                // block property
                Node.Property blockProp = new DerivationTypeProperty(
                        getReference().get(),
                        GlobalElement.BLOCK_PROPERTY,
                        NbBundle.getMessage(GlobalElementNode.class,"PROP_Block_DisplayName"), // display name
                        NbBundle.getMessage(GlobalElementNode.class,"HINT_Block_ShortDesc"),	// descr
                        getTypeDisplayName()
                        );
                set.put(new SchemaModelFlushWrapper(getReference().get(), blockProp));

                // substitutionGroup 
                Node.Property substitutionGroupProp = new GlobalReferenceProperty<GlobalElement>(
                        getReference().get(),
                        GlobalElement.SUBSTITUTION_GROUP_PROPERTY,
                        NbBundle.getMessage(GlobalElementNode.class,
                        "PROP_SubstitutionGroup_DisplayName"), // display name
                        NbBundle.getMessage(GlobalElementNode.class,
                        "HINT_SubstitutionGroup_ShortDesc"),	// descr
                        getTypeDisplayName(), // type display name
                        NbBundle.getMessage(GlobalElementNode.class,
                        "LBL_GlobalElementNode_TypeDisplayName"), // reference type display name
                        GlobalElement.class
                        ) {
                            //allows null value as default value
                            public boolean supportsDefaultValue() {
                                return true;
                            }
                };
                set.put(new SchemaModelFlushWrapper(getReference().get(), substitutionGroupProp));
            } catch (NoSuchMethodException nsme) {
                assert false : "properties should be defined";
            }
            
            return sheet;
        }

}
