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

import org.dom4j.Document;

import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.InvalidArguments;

public interface IWSProject extends IWSElement
{
	/**
	 * Retrieves the collection of WSElements in this WSProject.
	 *
	 * @return The WSElements in the project.
	 * @throws WorkspaceManagementException to specify workspace errors.
	 */
	public ETList<IWSElement> getElements() throws WorkspaceManagementException;

	/**
	 * Adds a new element to this WSProject from the contents of an external file.
	 *
	 * @param fileName The absolute path to the external file. This can be "", but not 0.
	 * @param name The name to be applied to the new WorkspaceProjectElement.
	 * @param data The data for the new WorkspaceProjectElement.
	 * @return The new element. 
	 * @throws WorkspaceManagementException to specify workspace errors.
	 */
	public IWSElement addElement( String fileName, String Name, String data )
		throws WorkspaceManagementException;

	/**
	 * Adds a new element to this WSProject from the contents of an XML document.
	*/
	public IWSElement addElementFromDoc( Document doc, String Name );

	/**
	 * Retrieves a WSElement that matches the passed in name.
	*/
	public IWSElement getElementByName( String Name );

	/**
	 * Retrieves a WSElement that matches the passed in location.
	*/
	public IWSElement getElementByLocation( String location )
		throws WorkspaceManagementException;

	/**
	 * Opens this WSProject.
	*/
	public void open() throws WorkspaceManagementException;

	/**
	 * Closes this WSProject.
	*/
	public void close( boolean saveFirst ) throws WorkspaceManagementException;

	/**
	 * Checks to see if this WSProject is open.
	*/
	public boolean isOpen();

	/**
	 * property _Open
	*/
	public void setOpen( boolean value );

	/**
	 * Sets / Gets the directory that roots all the elements within this WSProject.
	*/
	public String getBaseDirectory();

	/**
	 * Sets / Gets the directory that roots all the elements within this WSProject.
	*/
	public void setBaseDirectory( String value ) throws WorkspaceManagementException;

	/**
	 * Validates that the path specified is unique within this WSProject. 
	*/
	public boolean verifyUniqueLocation( String newVal )
	 throws WorkspaceManagementException;

	/**
	 * Removes the WSElement from this WSProject.
	*/
	public void removeElement( IWSElement wsElement )
	  throws InvalidArguments;

	/**
	 * Removes the WSElement from this WSProject that is located at the specified location.
	*/
	public boolean removeElementByLocation( String location )
	  throws WorkspaceManagementException;

	/**
	 * Sets / Gets the EventDispatcher that this manager will notify when events on this manager occur.
	*/
	public IWorkspaceEventDispatcher getEventDispatcher();

	/**
	 * Sets / Gets the EventDispatcher that this manager will notify when events on this manager occur.
	*/
	public void setEventDispatcher( IWorkspaceEventDispatcher value );

	/**
	 * Retrieves all WSElement's that have a matching data element to the data passed in.
	*/
	public ETList<IWSElement> getElementsByDataValue( String dataToMatch );

	/**
	 * The ID that identifies this WSProject in the SCM tool.
	*/
	public String getSourceControlID();

	/**
	 * The ID that identifies this WSProject in the SCM tool.
	*/
	public void setSourceControlID( String value );

}
