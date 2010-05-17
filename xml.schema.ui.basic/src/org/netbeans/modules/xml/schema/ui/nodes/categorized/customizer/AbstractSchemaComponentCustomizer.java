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
