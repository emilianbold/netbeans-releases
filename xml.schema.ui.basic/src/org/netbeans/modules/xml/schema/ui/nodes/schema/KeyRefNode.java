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
