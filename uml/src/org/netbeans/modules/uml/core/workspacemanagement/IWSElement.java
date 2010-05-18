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
