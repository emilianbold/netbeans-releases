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

import org.dom4j.Element;

public interface IWSElement
{
	/**
	 * Sets / Gets the name of this element.
	*/
	public String getName();

	/**
	 * Sets / Gets the name of this element.
	*/
	public void setName( String value );

	public String getNameWithAlias();
	public void setNameWithAlias(String newVal);
	public String getAlias();
	public void setAlias(String newVal);

	/**
	 * Sets / Gets the owner of this element.
	*/
	public IWSProject getOwner();

	/**
	 * Sets / Gets the owner of this element.
	*/
	public void setOwner( IWSProject value );

	/**
	 * Sets / Gets the DOM element behind this WSElement.
	*/
	public Element getElement();

	/**
	 * Sets / Gets the DOM element behind this WSElement.
	*/
	public void setElement( Element value );

	/**
	 * Sets / Gets the location of the external file this element represents.
	*/
	public String getLocation() throws WorkspaceManagementException;

	/**
	 * Sets / Gets the location of the external file this element represents.
	*/
	public void setLocation( String value );

	/**
	 * property TwoPhaseCommit
	*/
	public ITwoPhaseCommit getTwoPhaseCommit();

	/**
	 * property TwoPhaseCommit
	*/
	public void setTwoPhaseCommit( ITwoPhaseCommit value );

	/**
	 * Sets / Gets the dirty flag of this element.
	*/
	public boolean isDirty();

	/**
	 * Sets / Gets the dirty flag of this element.
	*/
	public void setIsDirty( boolean value );

	/**
	 * Saves this WSElement
	 *
	 * @throws WorkspaceManagementException Thrown when an error occurs
	 *         while saving the element. 
	 */
	public void save( String location ) throws WorkspaceManagementException;
	
	/**
	 * Saves this WSElement
	 *
	 * @throws WorkspaceManagementException Thrown when an error occurs
	 *         while saving the element. 
	 */
	public void save( ) throws WorkspaceManagementException;

	/**
	 * Sets / Gets the data for this element.
	*/
	public String getData();

	/**
	 * Sets / Gets the data for this element.
	*/
	public void setData( String value );

	/**
	 * The documentation specific to this WSElement.
	*/
	public String getDocumentation();

	/**
	 * The documentation specific to this WSElement.
	*/
	public void setDocumentation( String value );

}
