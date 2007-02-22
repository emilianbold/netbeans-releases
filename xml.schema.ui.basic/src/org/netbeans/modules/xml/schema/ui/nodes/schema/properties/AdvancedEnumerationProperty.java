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

/*
 * AdvancedEnumerationProperty.java
 *
 * Created on April 19, 2006, 11:53 AM
 *
 */

package org.netbeans.modules.xml.schema.ui.nodes.schema.properties;

import java.awt.Dialog;
import java.beans.FeatureDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.util.Collection;
import org.netbeans.modules.xml.schema.model.Enumeration;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.model.SimpleRestriction;
import org.netbeans.modules.xml.schema.ui.basic.UIUtilities;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.customizer.EnumerationCustomizer;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;

/**
 *
 * @author Ajit Bhate
 */
public class AdvancedEnumerationProperty extends BaseSchemaProperty
{

	private boolean editable;
	/** Creates a new instance of AdvancedEnumerationProperty */
	public AdvancedEnumerationProperty(SimpleRestriction component,
			String property,
			String propDispName,
			String propDesc,
			boolean editable) 
			throws NoSuchMethodException
	{
		super(component,
				Collection.class,
				SimpleRestriction.class.getMethod(BaseSchemaProperty.
				firstLetterToUpperCase(property, "get"), new Class[0]),
				null,
				property,propDispName,propDesc,null);
		this.editable = editable;
	}
	
	public PropertyEditor getPropertyEditor()
	{
		return new EnumEditor((SimpleRestriction) getComponent(), 
				getName(), canWrite());
	}

	@Override
	public boolean canWrite()
	{
		if(editable) return super.canWrite();
		return false;
	}

	@Override
	public boolean supportsDefaultValue()
	{
		return false;
	}

	public static class EnumEditor 
			extends PropertyEditorSupport implements ExPropertyEditor
	{
		private SimpleRestriction sr;
		private String name;
		private boolean editable;
		public EnumEditor(SimpleRestriction sr, String name, boolean editable)
		{
			this.sr = sr;
			this.name = name;
			this.editable = editable;
		}
		public boolean supportsCustomEditor()
		{
			return true;
		}
		
		public java.awt.Component getCustomEditor()
		{
			DialogDescriptor descriptor = UIUtilities.getCustomizerDialog(new 
					EnumerationCustomizer<SimpleRestriction>(
					SchemaComponentReference.create(sr)), name,editable);
	        Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
			return dlg;
		}
		
		public void attachEnv(PropertyEnv env)
		{
			FeatureDescriptor desc = env.getFeatureDescriptor();
			desc.setValue("canEditAsText", Boolean.FALSE); // NOI18N
		}

		public String getAsText()
		{
			StringBuffer retValue = new StringBuffer();
			Object obj = super.getValue();
			if(obj instanceof Collection)
			{
				Collection enums = (Collection)obj;
				boolean first = true;
				for(Object e:enums)
				{
					if(e instanceof Enumeration)
					{
						if(first)
							first = false;
						else
							retValue.append(", ");
						retValue.append(((Enumeration)e).getValue());
					}
				}
			}
			return retValue.toString();
		}
		
	}
}
