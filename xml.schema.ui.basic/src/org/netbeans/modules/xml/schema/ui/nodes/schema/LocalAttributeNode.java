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

import java.beans.PropertyEditorSupport;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.ui.nodes.*;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.BaseSchemaProperty;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.DefaultProperty;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.FixedProperty;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.FormProperty;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class LocalAttributeNode extends SchemaComponentNode<LocalAttribute>
{
    /**
     *
     *
     */
    public LocalAttributeNode(SchemaUIContext context,
	    SchemaComponentReference<LocalAttribute> reference,
	    Children children) {
		super(context,reference,children);
	
		setIconBaseWithExtension(
		"org/netbeans/modules/xml/schema/ui/nodes/resources/"+
		"attribute.png");
    }
    
    
    /**
     *
     *
     */
    @Override
	    public String getTypeDisplayName() {
	return NbBundle.getMessage(LocalAttributeNode.class,
		"LBL_LocalAttributeNode_TypeDisplayName"); // NOI18N
    }
    
	@Override
	protected GlobalSimpleType getSuperDefinition()
	{
		LocalAttribute sc = getReference().get();
		GlobalSimpleType gt = null;
		if(sc.getType()!=null)
			gt = sc.getType().get();
		return gt;
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
		    NbBundle.getMessage(LocalAttributeNode.class,"PROP_Fixed_DisplayName"), // display name
		    NbBundle.getMessage(LocalAttributeNode.class,"PROP_Fixed_ShortDescription")	// descr
		    );
	    set.put(new SchemaModelFlushWrapper(getReference().get(),fixedProp));
	    
	    // default property
	    Node.Property defaultProp = new DefaultProperty(
		    getReference().get(), // schema component
		    NbBundle.getMessage(LocalAttributeNode.class,"PROP_Default_DisplayName"), // display name
		    NbBundle.getMessage(LocalAttributeNode.class,"PROP_Default_ShortDescription")	// descr
		    );
	    set.put(new SchemaModelFlushWrapper(getReference().get(),defaultProp));
        
        // use property
	    Node.Property useProp = new BaseSchemaProperty(
		    getReference().get(), // schema component
			LocalAttribute.Use.class, //as value type
			LocalAttribute.USE_PROPERTY, //property name	
		    NbBundle.getMessage(LocalAttributeNode.class,"PROP_Use_DisplayName"), // display name
		    NbBundle.getMessage(LocalAttributeNode.class,"PROP_Use_ShortDescription"),	// descr
		    UseEditor.class);
	    set.put(new SchemaModelFlushWrapper(getReference().get(),useProp));
	    
	    // form property
	    Node.Property formProp = new FormProperty(
		    getReference().get(), // schema component
		    LocalAttribute.FORM_PROPERTY, //property name
		    NbBundle.getMessage(LocalAttributeNode.class,"PROP_Form_DisplayName"), // display name
		    NbBundle.getMessage(LocalAttributeNode.class,"PROP_Form_ElementShortDescription")	// descr
		    );
	    set.put(new SchemaModelFlushWrapper(getReference().get(),formProp));
	    
	    // type property
	    
	} catch (NoSuchMethodException nsme) {
	    assert false : "properties should be defined";
	}
	
	return sheet;
    }
    
    public static class UseEditor extends PropertyEditorSupport {
	
	/**
	 * Creates a new instance of ProcessContentsEditor
	 */
	public UseEditor() {
	}
	
	public String[] getTags() {
	    return new String[] {NbBundle.getMessage(LocalAttributeNode.class,"LBL_Empty"),
	    NbBundle.getMessage(LocalAttributeNode.class,"LBL_Prohibited"),
	    NbBundle.getMessage(LocalAttributeNode.class,"LBL_Optional"),
	    NbBundle.getMessage(LocalAttributeNode.class,"LBL_Required")};
	}
	
	public void setAsText(String text) throws IllegalArgumentException {
	    if (text.equals(NbBundle.getMessage(LocalAttributeNode.class,"LBL_Empty"))){
		setValue(null);
	    } else if (text.equals(NbBundle.getMessage(LocalAttributeNode.class,"LBL_Prohibited"))){
		setValue(LocalAttribute.Use.PROHIBITED);
	    } else if (text.equals(NbBundle.getMessage(LocalAttributeNode.class,"LBL_Optional"))){
		setValue(LocalAttribute.Use.OPTIONAL);
	    } else if (text.equals(NbBundle.getMessage(LocalAttributeNode.class,"LBL_Required"))){
		setValue(LocalAttribute.Use.REQUIRED);
	    }
	}
	
	public String getAsText() {
	    Object val = getValue();
	    if (val instanceof LocalAttribute.Use){
		if (LocalAttribute.Use.PROHIBITED.equals(val)) {
		    return NbBundle.getMessage(LocalAttributeNode.class,"LBL_Prohibited");
		} else if (LocalAttribute.Use.OPTIONAL.equals(val)) {
		    return NbBundle.getMessage(LocalAttributeNode.class,"LBL_Optional");
		} else if (LocalAttribute.Use.REQUIRED.equals(val)) {
		    return NbBundle.getMessage(LocalAttributeNode.class,"LBL_Required");
		}
	    }
	    // TODO how to display invalid values?
	    return NbBundle.getMessage(LocalAttributeNode.class,"LBL_Empty");
	}
    }
}
