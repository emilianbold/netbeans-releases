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

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import org.netbeans.modules.xml.schema.model.Constraint;
import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.schema.model.KeyRef;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.model.visitor.FindReferredConstraintVisitor;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaComponentNode;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaModelFlushWrapper;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaUIContext;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.BaseSchemaProperty;
import org.openide.ErrorManager;
import org.openide.nodes.Node.Property;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class KeyRefNode extends SchemaComponentNode<KeyRef>
{
    /**
     *
     *
     */
    public KeyRefNode(SchemaUIContext context,
            SchemaComponentReference<KeyRef> reference,
            Children children)
    {
        super(context,reference,children);
	setIconBaseWithExtension(
			"org/netbeans/modules/xml/schema/ui/nodes/resources/keyRef.png");
    }
    
    
    /**
     *
     *
     */
    @Override
    public String getTypeDisplayName()
    {
        return NbBundle.getMessage(KeyRefNode.class,
                "LBL_KeyRefNode_TypeDisplayName"); // NOI18N
    }
    
    @Override
    protected Sheet createSheet()
    {
        Sheet sheet = super.createSheet();
        Sheet.Set props = sheet.get(Sheet.PROPERTIES);
        if (props == null)
        {
            props = Sheet.createPropertiesSet();
            sheet.put(props);
        }
        try
        {
            // Referer property
            Property refererProp = new BaseSchemaProperty(
                    getReference().get(), // schema component
                    Constraint.class, // property value type
                    KeyRef.REFERER_PROPERTY, // property name
                    NbBundle.getMessage(KeyRefNode.class,"PROP_KeyRef_Referer_DisplayName"), // display name
                    NbBundle.getMessage(KeyRefNode.class,"PROP_KeyRef_Referer_ShortDescription"),	// descr
                    null // prop editor class
                    )
            {
                public boolean supportsDefaultValue()
                {
                    return false;
                }

                public PropertyEditor getPropertyEditor()
                {
                    return new ConstraintEditor(getReference().get());
                }
            };
            props.put(new SchemaModelFlushWrapper(getReference().get(), refererProp));
            
        }
        catch (NoSuchMethodException nsme)
        {
            assert false : "properties should be defined";
        }
        
        return sheet;
    }
    
    public static class ConstraintEditor extends PropertyEditorSupport
    {
        private SchemaComponent component;
        public ConstraintEditor(SchemaComponent component)
        {
            super();
            this.component = component;
        }

        public void setAsText(String text) throws IllegalArgumentException
        {
            SchemaComponent parent = findOutermostParentElement();
            FindReferredConstraintVisitor visitor =
                    new FindReferredConstraintVisitor();
            
            Constraint c =  visitor.findReferredConstraint(parent, text);
            if(c!=null)
            {
                setValue(c);
            }
            else
            {
                throwError(text);
            }
       }
        
        public String getAsText()
        {
            Object val = getValue();
            if (val instanceof Constraint)
            {
                return ((Constraint)val).getName();
            }
            return null;
        }

        private SchemaComponent findOutermostParentElement()
        {
            SchemaComponent element = null;
            //go up the tree and look for the last instance of <element>
            SchemaComponent sc = component.getParent();
            while(sc != null)
            {
                if(sc instanceof Element)
                {
                    element = sc;
                }
                sc = sc.getParent();
            }
            return element;
        }

        private void throwError(String text)
        {
            String msg = NbBundle.getMessage(KeyRefNode.class, "LBL_Illegal_Referer_Value", text); //NOI18N
            IllegalArgumentException iae = new IllegalArgumentException(msg);
            ErrorManager.getDefault().annotate(iae, ErrorManager.USER,
                    msg, msg, null, new java.util.Date());
            throw iae;
            
        }
    }
}
