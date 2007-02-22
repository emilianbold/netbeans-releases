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
 * AdvancedGlobalElementCustomizer.java
 *
 * Created on January 17, 2006, 10:26 PM
 */

package org.netbeans.modules.xml.schema.ui.nodes.categorized.customizer;

import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.openide.util.HelpCtx;
//local imports
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.LocalType;
import org.netbeans.modules.xml.schema.model.SchemaComponentFactory;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.customizer.ElementCustomizer.ElementTypeStyle;

/**
 * Global Element customizer
 *
 * @author  Ajit Bhate
 */
public class AdvancedGlobalElementCustomizer extends ElementCustomizer<GlobalElement>
{
	
	static final long serialVersionUID = 1L;
	
	/**
	 * Creates new form AdvancedGlobalElementCustomizer
	 */
	/**
	 * Creates new form AdvancedLocalElementCustomizer
	 */
	public AdvancedGlobalElementCustomizer(
			SchemaComponentReference<GlobalElement> reference)
	{
		this(reference, null);
	}
	
	public AdvancedGlobalElementCustomizer(
			SchemaComponentReference<GlobalElement> reference,
			SchemaComponent parent)
	{
		super(reference, parent);
	}
	
	/**
	 * initializes non ui elements
	 */
	protected void initializeModel()
	{
		GlobalElement element = getReference().get();
		if (element.getType() != null)
		{
			_setType(element.getType().get());
		}
		else
		{
			_setType(element.getInlineType());
		}
	}
	
	/**
	 * Changes the type of element
	 *
	 */
	protected void setModelType()
	{
		GlobalElement element = getReference().get();
		SchemaComponentFactory factory = element.getModel().getFactory();
		
		ElementTypeStyle newStyle = getUIStyle();
		GlobalType newType = getUIType();
		_setType(newType);
		if(newStyle == ElementTypeStyle.EXISTING)
		{
			if(element.getInlineType()!=null)
			{
				element.setInlineType(null);
			}
			element.setType(factory.createGlobalReference(
					newType, GlobalType.class, element));
		}
		else
		{
			if(element.getType()!=null)
			{
				element.setType(null);
			}
			LocalType lt = createLocalType(factory, newStyle, newType);
			element.setInlineType(lt);
		}
	}

	public HelpCtx getHelpCtx()
	{
		return new HelpCtx(AdvancedGlobalElementCustomizer.class);
	}
}
