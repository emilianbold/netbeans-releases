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

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import javax.swing.Icon;

public interface ISCMIntegrator
{
   public final static int SMK_NONE           = 0;
   public final static int SMK_CHECKEDOUT     = 1;
   public final static int SMK_CONFIGURED      = 2;
   public final static int c = 3;

	/**
	 * Retrieves the ISCMTool associated with the given IProject.
	*/
	public ISCMTool getSCMToolByProject( IProject proj );

	/**
	 * Retrieves the ISCMTool associated with the given element.
	*/
	public ISCMTool getSCMToolByElement( IElement pElement );

	/**
	 * Retrieves the ISCMTool associated with the given workspace.
	*/
	public ISCMTool getSCMToolByWorkspace( IWorkspace pSpace );

	/**
	 * Retrieves an SCMTool given an ID and filename.
	*/
	public ISCMTool getSCMToolByID( String fileName, String SCMID );

	/**
	 * Retrieves the ISCMTool associated with the given workspace.
	*/
	public ISCMTool getSCMToolByFile( String fileName );

	/**
	 * Associates the passed in IProject with a new ISCMTool.
	*/
	public ISCMTool associateProjectWithSCMTool( IProject pElement );

	/**
	 * Associates the passed in IWorkspace with a new ISCMTool.
	*/
	public ISCMTool associateWorkspaceWithSCMTool( IWorkspace pSpace );

	/**
	 * Associates the passed in file with a new ISCMTool. If an existing tool can be used, it will be returned instead of creating a new one.
	*/
	public ISCMTool associateFileWithSCMTool( String fileName );

	/**
	 * Determines whether or not the SCM integration is on or not.
	*/
	public boolean isSCMEnabled();

	/**
	 * Determines whether or not the SCM integration is on or not.
	*/
	public void setIsSCMEnabled( boolean value );

	/**
	 * Returns the mask for this particular status.
    *
    * @param nMaskKind The mask kind, must be one of the following values:
    *                  MK_NONE, SMK_CHECKEDOUT, SMK_CONFIGURED, or SMK_CONFIGURED
    * @return The mask details.
	 */
	public Icon getSCMMask( int nMaskKind );

	/**
	 * Returns the mask kind for this tree item.
    *
    * @return Will return the mask kind.  The mask kind will be one of the
    *         following values: SMK_NONE, SMK_CHECKEDOUT, SMK_CONFIGURED, or SMK_CONFIGURED
	 */
	public int getSCMMaskKind( IProjectTreeItem pItem );

   /**
	 * Clears the SCM status associated with the passed in file name.
    *
    * @param fileName The file to clear. This should NOT be an XMI id
	*/
	public void clearSCMStatus( String fileName );

	/**
	 * Clears the SCM status associated with the passed in file name.
	*/
	public void clearSCMStatus( String fileName, IElement Element );

	/**
	 * Clears the entire SCM cache.
	*/
	public void clearSCMStatusCache();

	/**
	 * Retrieves a simple SCM status for the given element.
    *
    * @param pElement The element to check on.
    * @return Will return the mask kind.  The mask kind will be one of the
    *         following values: SMK_NONE, SMK_CHECKEDOUT, SMK_CONFIGURED, or SMK_CONFIGURED
	*/
	public int getSCMStatusForElement( IElement pElement );

	/**
	 * Retrieves a simple SCM status for the given file.
    *
    * @param fileName The file to check on.
    * @return Will return the mask kind.  The mask kind will be one of the
    *         following values: SMK_NONE, SMK_CHECKEDOUT, SMK_CONFIGURED, or SMK_CONFIGURED
	 */
	public int getSCMStatusForFile( String fileName );

	/**
	 * Retrieves a simple SCM status for the given item.
    *
    * @param item The SCM item.
    * @return Will return the mask kind.  The mask kind will be one of the
    *         following values: SMK_NONE, SMK_CHECKEDOUT, SMK_CONFIGURED, or SMK_CONFIGURED
	*/
	public int establishSCMStatus( ISCMItem item );

	/**
	 * Adds the passed in IProject to Version Control
	*/
	public void versionProject( IProject pProject );

   /**
	 * Retrieves all the files that a Workspace is managing by retrieving all the
    * files indicated by the .etw from SCM.  The files names will not be pull
    * from SCM.
	*/
	public IStrings getFilesWithWorkspace( IWorkspace space);

	/**
	 * Retrieves all the files that a Workspace is managing by retrieving all the
    * files indicated by the .etw from SCM.
	*/
	public IStrings getFilesWithWorkspace( IWorkspace space, boolean pullFromSCM );

	/**
	 * Retrieves all the file that make up a project. This is mostly files with
    * an etx extension. This is done by files that need to be retrieved from SCM.
    * The files names will not be pull from SCM.
	*/
	public IStrings getFilesWithProject( IProject proj);

   /**
	 * Retrieves all the file that make up a project. This is mostly files with
    * an etx extension. This is done by files that need to be retrieved from SCM.
	*/
	public IStrings getFilesWithProject( IProject proj, boolean pullFromSCM );

	/**
	 * Opens a Workspace from SCM.
	*/
	public IWorkspace openWorkspaceFromSCM();

	/**
	 * Retrieves the ISCMTool that is the current default tool.
	*/
	public ISCMTool getCurrentDefault();

   /**
    * Refreshes the SCM Status indicators in the Project Tree.  The refresh
    * action will not be posted as an event.
    */
   public void refreshTree();

   /** Refreshes the SCM Status indicators in the Project Tree */
   public void refreshTree( boolean postEvent );

   /**
    * Executes the indicated SCM feature on the element collection
    *
    * @param kind the Type of feature to execute.  The type must be one of the
    *             SCMFeatureKind values.
    * @param elements The elements that are the object of the execution.
    * @param showGUI Determines if the action will be executed silently.
    * @return <code>true</code> if the command was successiful.
    */
   public boolean executeSCMFeature( /*SCMFeatureKind*/ int kind,
                                     ETList < IElement > elements,
                                     boolean bShowGUI);

}
