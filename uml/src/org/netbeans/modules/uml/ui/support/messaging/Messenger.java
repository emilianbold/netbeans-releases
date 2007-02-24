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



package org.netbeans.modules.uml.ui.support.messaging;

/**
 * @author sumitabhk
 *
 *
 */
public class Messenger implements IMessenger
{
	private boolean disableMessaging = false;

	/**
	 *
	 */
	public Messenger()
	{
		super();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.coreapplication.ICoreMessenger#getDisableMessaging()
	 */
	public boolean getDisableMessaging()
	{
		return disableMessaging;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.coreapplication.ICoreMessenger#setDisableMessaging(boolean)
	 */
	public void setDisableMessaging(boolean value)
	{
		disableMessaging = value;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.coreapplication.ICoreMessenger#getParentWindow()
	 */
	public int getParentWindow()
	{
		// TODO Auto-generated method stub
		return 0;
	}

}


