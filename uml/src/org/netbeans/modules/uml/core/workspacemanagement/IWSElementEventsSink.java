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

package org.netbeans.modules.uml.core.workspacemanagement;

import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;

public interface IWSElementEventsSink
{
	/**
	 * Fired whenever a WSElement is about to be created.
	*/
	public void onWSElementPreCreate( IWSProject wsProject, String location, String Name, String data, IResultCell cell );

	/**
	 * Fired right after a WSElement is created.
	*/
	public void onWSElementCreated( IWSElement element, IResultCell cell );

	/**
	 * Fired whenever a WSElement is about to be saved.
	*/
	public void onWSElementPreSave( IWSElement element, IResultCell cell );

	/**
	 * Fired right after a WSElement is saved.
	*/
	public void onWSElementSaved( IWSElement element, IResultCell cell );

	/**
	 * Fired whenever a WSElement is about to be removed from the WSProject.
	*/
	public void onWSElementPreRemove( IWSElement element, IResultCell cell );

	/**
	 * Fired right after a WSElement is removed.
	*/
	public void onWSElementRemoved( IWSElement element, IResultCell cell );

	/**
	 * Fired whenever the name of the WSElement is about to be changed.
	*/
	public void onWSElementPreNameChanged( IWSElement element, String proposedValue, IResultCell cell );

	/**
	 * Fired right after a WSElement's name has changed.
	*/
	public void onWSElementNameChanged( IWSElement element, IResultCell cell );

	/**
	 * Fired whenever the owner of the WSElement is about to be changed.
	*/
	public void onWSElementPreOwnerChange( IWSElement element, IWSProject newOwner, IResultCell cell );

	/**
	 * Fired right after a WSElement's owner has changed.
	*/
	public void onWSElementOwnerChanged( IWSElement element, IResultCell cell );

	/**
	 * Fired whenever the location of the WSElement is about to be changed.
	*/
	public void onWSElementPreLocationChanged( IWSElement element, String proposedLocation, IResultCell cell );

	/**
	 * Fired right after a WSElement's location has changed.
	*/
	public void onWSElementLocationChanged( IWSElement element, IResultCell cell );

	/**
	 * Fired whenever the data of the WSElement is about to be changed.
	*/
	public void onWSElementPreDataChanged( IWSElement element, String newData, IResultCell cell );

	/**
	 * Fired right after a WSElement's data has changed.
	*/
	public void onWSElementDataChanged( IWSElement element, IResultCell cell );

	/**
	 * Fired whenever the documentation field of the WSElement is about to be changed.
	*/
	public void onWSElementPreDocChanged( IWSElement element, String doc, IResultCell cell );

	/**
	 * Fired right after a WSElement's documentation field has changed.
	*/
	public void onWSElementDocChanged( IWSElement element, IResultCell cell );

	/* (non-Javadoc)
	 * @see com.embarcadero.describe.workspacemanagement.IWSElementEventsSink#onWSElementPreAliasChanged(com.embarcadero.describe.workspacemanagement.IWSElement, java.lang.String, com.embarcadero.describe.umlsupport.IResultCell)
	 */
	public void onWSElementPreAliasChanged(IWSElement element, String proposedValue, IResultCell cell);

	/* (non-Javadoc)
	 * @see com.embarcadero.describe.workspacemanagement.IWSElementEventsSink#onWSElementAliasChanged(com.embarcadero.describe.workspacemanagement.IWSElement, com.embarcadero.describe.umlsupport.IResultCell)
	 */
	public void onWSElementAliasChanged(IWSElement element, IResultCell cell);

}
