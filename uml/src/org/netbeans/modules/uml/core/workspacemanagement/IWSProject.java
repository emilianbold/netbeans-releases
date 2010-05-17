/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
