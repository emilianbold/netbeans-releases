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
