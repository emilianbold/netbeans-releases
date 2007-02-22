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
 * AbstractSchemaComponentCustomizer.java
 *
 * Created on May 8, 2006, 4:43 PM
 *
 */

package org.netbeans.modules.xml.schema.ui.nodes.categorized.customizer;

import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.xam.Nameable;
import org.netbeans.modules.xml.xam.dom.Utils;
import org.netbeans.modules.xml.xam.ui.customizer.AbstractComponentCustomizer;
import org.netbeans.modules.xml.xam.ui.customizer.MessageDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author Ajit Bhate
 */
abstract class AbstractSchemaComponentCustomizer<T extends SchemaComponent> 
		extends AbstractComponentCustomizer<SchemaComponent>
{
	
	/** Creates a new instance of AbstractSchemaComponentCustomizer */
	AbstractSchemaComponentCustomizer(SchemaComponentReference<T> reference)
	{
		this(reference,null);
	}

	/** Creates a new instance of AbstractSchemaComponentCustomizer */
	AbstractSchemaComponentCustomizer(SchemaComponentReference<T> reference,
			SchemaComponent parent)
	{
                super(reference.get());
		this.reference = reference;
		this.parent = parent;
	}
	
	protected SchemaComponentReference<T> getReference()
	{
		return reference;
	}

	protected SchemaComponent getParentComponent()
	{
		SchemaComponent sc = getReference().get().getParent();
		if(sc!=null) return sc;
		return this.parent;
	}

	protected boolean hasParent()
	{
		return getReference().get().getParent()!=null;
	}

	protected boolean isNameChanged()
	{
		if(!isNameable()) return false;
		String modelName = _getName();
		String uiName = getUIName();
		if(uiName==null || uiName.trim().length()==0)
			return modelName!=null && modelName.trim().length()!=0;
		return !uiName.equals(modelName);
	}

	protected boolean isNameValid()
	{
		if(!isNameable()) return true;
		String uiName = getUIName();
		if(uiName==null || !Utils.isValidNCName(uiName)) {
            return false;
        }
		for(SchemaComponent child :getParentComponent().getChildren(
				getReference().get().getComponentType()))
		{
			if(uiName.equals(((Nameable)child).getName())) return false;
		}
		return true;
	}

	protected void saveName()
	{
		if(isNameable() && isNameValid() && isNameChanged())
		{
			Nameable n = (Nameable)getReference().get();
			// refactor???
			n.setName(getUIName());
		}
	}
	
	protected boolean isNameable()
	{
		return getReference().get() instanceof Nameable;
	}

	protected String _getName()
	{
		if(isNameable())
			return ((Nameable)getReference().get()).getName();
		return null;
	}

	protected String getUIName()
	{
		return null;
	}

	private transient SchemaComponentReference<T> reference;
	/**
	 * parent component for new type
	 */
	private transient SchemaComponent parent;

	protected void setSaveEnabled(boolean flag)
	{
		super.setSaveEnabled(flag);
		if(isNameable() && isNameChanged())
		{
			if(isNameValid())
			{
				if(hasParent())
				{
					getMessageDisplayer().annotate(NbBundle.getMessage(
							AbstractSchemaComponentCustomizer.class,
							"MSG_Name_Warning"),
							MessageDisplayer.Type.WARNING);
				}
			}
			else
			{
				getMessageDisplayer().annotate(NbBundle.getMessage(
						AbstractSchemaComponentCustomizer.class,
						"MSG_Name_Error"),
						MessageDisplayer.Type.ERROR);
			}
		}
	}

}
