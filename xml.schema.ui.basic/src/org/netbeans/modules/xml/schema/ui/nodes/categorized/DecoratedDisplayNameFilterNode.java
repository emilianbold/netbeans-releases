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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;

/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class DecoratedDisplayNameFilterNode extends FilterNode
{
	/**
	 *
	 *
	 */
	public DecoratedDisplayNameFilterNode(final Node original,
		String template, String initialName)
	{
		super(original);

		this.template=template;

		// Manage our own display name
		disableDelegation(
			FilterNode.DELEGATE_GET_DISPLAY_NAME | 
			FilterNode.DELEGATE_SET_DISPLAY_NAME);

		setDecoratedDisplayName(initialName);

		// Listen to display name changes in the original node, and set
		// our display name to incorporate the original's name
		original.addPropertyChangeListener(
			new PropertyChangeListener()
			{
				public void propertyChange(PropertyChangeEvent event)
				{
					if (event.getSource()==original && 
						event.getPropertyName().equals(
							Node.PROP_DISPLAY_NAME))
					{
						setDecoratedDisplayName((String)event.getNewValue());
					}
				}
			});
	}


	/**
	 *
	 *
	 */
	public String getTemplate()
	{
		return template;
	}


	/**
	 *
	 *
	 */
	public void setTemplate(String value)
	{
		template=value;
		setDecoratedDisplayName(lastName);
	}


	/**
	 *
	 *
	 */
	public void setDecoratedDisplayName(String name)
	{
		setDisplayName(MessageFormat.format(getTemplate(),name));
		lastName=name;
	}




	////////////////////////////////////////////////////////////////////////////
	// Instance members
	////////////////////////////////////////////////////////////////////////////

	private String template;
	private transient String lastName;
}
