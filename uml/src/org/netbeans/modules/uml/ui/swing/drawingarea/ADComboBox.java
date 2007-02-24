/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.uml.ui.swing.drawingarea;

import javax.swing.JComboBox;

/**
 * This class implements a zoom combo box.
 */
public class ADComboBox extends JComboBox
{

	/**
	 * This is the constructor. The items in its drop down list are
	 * specified by <code>items</code>.
	 */
	public ADComboBox(Object[] items)
	{
		super(items);
		this.fireEvents = true;
	}

	/**
	 * This method sets whether or not events are actually fired by
	 * <code>fireActionEvent</code>.
	 */
	public void setFireEvents(boolean fireEvents)
	{
		this.fireEvents = fireEvents;
	}

	/** 
	 * This method overrides <code>JComboBox.fireActionEvent</code> to
	 * fire events only if the <code>fireEvents</code> flag is true.
	 */
	protected void fireActionEvent()
	{
		if (this.fireEvents)
		{
			super.fireActionEvent();
		}
	}

	// ---------------------------------------------------------------------
	// Section: Instance variables
	// ---------------------------------------------------------------------

	/**
	 * This variable stores whether or not events will be fired.
	 */
	boolean fireEvents;
}
