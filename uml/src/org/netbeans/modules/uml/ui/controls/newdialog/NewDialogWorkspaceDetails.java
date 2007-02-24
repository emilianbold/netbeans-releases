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



package org.netbeans.modules.uml.ui.controls.newdialog;

/**
 * @author sumitabhk
 *
 */
public class NewDialogWorkspaceDetails implements INewDialogWorkspaceDetails
{
	private String m_Name;
	private String m_Location;

	/**
	 *
	 */
	public NewDialogWorkspaceDetails()
	{
		super();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.newdialog.INewDialogWorkspaceDetails#getName()
	 */
	public String getName()
	{
		return m_Name;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.newdialog.INewDialogWorkspaceDetails#setName(java.lang.String)
	 */
	public void setName(String value)
	{
		m_Name = value;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.newdialog.INewDialogWorkspaceDetails#getLocation()
	 */
	public String getLocation()
	{
		return m_Location;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.newdialog.INewDialogWorkspaceDetails#setLocation(java.lang.String)
	 */
	public void setLocation(String value)
	{
		m_Location = value;
	}

}



