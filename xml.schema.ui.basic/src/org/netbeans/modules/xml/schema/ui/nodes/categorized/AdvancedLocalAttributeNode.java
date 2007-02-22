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

package org.netbeans.modules.xml.schema.ui.nodes.categorized;

import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaUIContext;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.customizer.AdvancedLocalAttributeCustomizer;
import org.netbeans.modules.xml.schema.ui.nodes.schema.LocalAttributeNode;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xam.ui.customizer.Customizer;
import org.netbeans.modules.xml.xam.ui.customizer.CustomizerProvider;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;

/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class AdvancedLocalAttributeNode extends LocalAttributeNode
{
	/**
	 *
	 *
	 */
	public AdvancedLocalAttributeNode(SchemaUIContext context,
			SchemaComponentReference<LocalAttribute> reference,
			Children children)
	{
		super(context,reference,children);
	}
	
	
	@Override
	public boolean hasCustomizer()
	{
		return isEditable();
	}
	
	public CustomizerProvider getCustomizerProvider()
	{
		return new CustomizerProvider()
		{
			
			public Customizer getCustomizer()
			{
				return new AdvancedLocalAttributeCustomizer(getReference());
			}
		};
	}
	
	public void valueChanged(ComponentEvent evt)
	{
		super.valueChanged(evt);
		if(isValid() && evt.getSource()==getReference().get())
		{
			fireDisplayNameChange(null,getDisplayName());
		}
	}
	
	public String getHtmlDisplayName()
	{
		String retValue = getDefaultDisplayName();
                LocalAttribute la = getReference().get();
                NamedComponentReference<GlobalSimpleType> ref = la.getType();
		if (((AbstractDocumentComponent)la).isInDocumentModel() && 
                   ref != null && ref.get() != null)
		{
			String supertypeLabel = NbBundle.getMessage(
					AdvancedLocalAttributeNode.class, "LBL_InstanceOf",
					getReference().get().getType().get().getName());
			retValue = retValue+"<font color='#999999'> ("+supertypeLabel+")</font>";
		}
		return applyHighlights(retValue);
	}
}
