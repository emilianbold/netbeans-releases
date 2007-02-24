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

package org.netbeans.modules.uml.core.scm;

import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;

/**
 * A collection of ISCMItem types.
 */
public interface ISCMItemGroup
{
	/**
	 * property Count
	*/
	public int getCount();

	/**
	 * method Add
	*/
	public void add( ISCMItem attr );

	/**
	 * method Item
	*/
	public ISCMItem item( int index );

	/**
	 * method Remove
	*/
	public void remove( int index );

	/**
	 * The IProject associated with this group.
	*/
	public IProject getProject();

	/**
	 * The IProject associated with this group.
	*/
	public void setProject( IProject value );

	/**
	 * The ISCMTool associated with this group.
	*/
	public ISCMTool getTool();

	/**
	 * The ISCMTool associated with this group.
	*/
	public void setTool( ISCMTool value );

	/**
	 * Calls Prepare() on all internal ISCMItems.
	*/
	public boolean prepare( ISCMIntegrator integrator );

	/**
	 * Retrieves the file locations from all the internal ISCMItems.
	*/
	public IStrings getFiles();

	/**
	 * Calls ClearStatus() on all internal ISCMItems
	*/
	public void clearStatus( ISCMIntegrator integrator );

	/**
	 * Calls RemoveVersionInformation() on all internal ISCMItems
	*/
	public void removeVersionInformation( ISCMOptions pOptions );

	/**
	 * Retrieves the ISCMElementItem that represents the IProject. If there is none, 0 is returned.
	*/
	public ISCMElementItem getProjectItem();

	/**
	 * Retrieves the ISCMWorkspaceItem that represents the IWorkspace. If there is none, 0 is returned.
	*/
	public ISCMWorkspaceItem getWorkspaceItem();

	/**
	 * Retrieves a sub group that only contains ISCMDiagramItems.
	*/
	public ISCMItemGroup getDiagrams();

	/**
	 * Calls Validate() on all internal ISCMItems
	*/
	public void validate();

}
