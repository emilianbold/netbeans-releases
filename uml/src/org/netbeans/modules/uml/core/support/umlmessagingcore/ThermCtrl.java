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


package org.netbeans.modules.uml.core.support.umlmessagingcore;

/**
 * @author sumitabhk
 *
 */
public class ThermCtrl
{
	// The busy ctrl interface
	private ICoreBusyCtrl m_BusyCtrl = null;

	// Should we honor the updates?
	private boolean m_CreateIt = false;

	/**
	 *
	 */
	public ThermCtrl()
	{
		super();
	}

	// Use bCreateIt to put the object on the stack, but disable it, for instance
	// set bCreateIt to false if the 'count' in the therm is small (ie 1 or 2) but
	// setit to true if the 'count' is 50.
	public ThermCtrl(String message, long nUpper, boolean bCreateIt)
	{
		//to do
	}

	public ThermCtrl(String message, long nUpper)
	{
		this(message, nUpper, true);
	}

	// Update the therm
	public void update(String message, long nNewPosition)
	{
		//to do
	}
	
	public void update(long nNewPosition)
	{
		//To do
	}
}



