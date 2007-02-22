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
 * AdvancedLocalAttributeCustomizer.java
 *
 * Created on January 17, 2006, 10:26 PM
 */

package org.netbeans.modules.xml.schema.ui.nodes.categorized.customizer;

import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.openide.util.HelpCtx;
//local imports
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.LocalSimpleType;
import org.netbeans.modules.xml.schema.model.SchemaComponentFactory;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.customizer.AttributeCustomizer.AttributeTypeStyle;

/**
 * Global Element customizer
 *
 * @author  Ajit Bhate
 */
public class AdvancedLocalAttributeCustomizer extends AttributeCustomizer<LocalAttribute>
{
	
	static final long serialVersionUID = 1L;
	
	/**
	 * Creates new form AdvancedLocalAttributeCustomizer
	 */
	public AdvancedLocalAttributeCustomizer(
			SchemaComponentReference<LocalAttribute> reference)
	{
		this(reference, null, null);
	}
	
	public AdvancedLocalAttributeCustomizer(
			SchemaComponentReference<LocalAttribute> reference,
			SchemaComponent parent, GlobalSimpleType currentGlobalSimpleType)
	{
		super(reference, parent, currentGlobalSimpleType);
	}
		
	/**
	 * initializes non ui elements
	 */
	protected void initializeModel()
	{
		LocalAttribute attribute = getReference().get();
		if(!hasParent())
		{
			_setType(_getType());
		}
		else if (attribute.getType() != null)
		{
			_setType(attribute.getType().get());
		}
		else
		{
			_setType(attribute.getInlineType());
		}
	}
	
	/**
	 * Changes the type of attribute
	 *
	 */
	protected void setModelType()
	{
		LocalAttribute attribute = getReference().get();
		
		AttributeTypeStyle newStyle = getUIStyle();
		if(newStyle == AttributeTypeStyle.EXISTING)
		{
			GlobalSimpleType newType = getUIType();
			_setType(newType);
			if(attribute.getInlineType()!=null)
			{
				attribute.setInlineType(null);
			}
			SchemaComponentFactory factory = attribute.getModel().getFactory();
			attribute.setType(factory.createGlobalReference(
					newType, GlobalSimpleType.class, attribute));
		}
		else
		{
			if(attribute.getType()!=null)
			{
				attribute.setType(null);
			}
			LocalSimpleType lt = createLocalType();
			attribute.setInlineType(lt);
			_setType(lt);
		}
	}
	
	public HelpCtx getHelpCtx()
	{
		return new HelpCtx(AdvancedLocalAttributeCustomizer.class);
	}
}
