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
