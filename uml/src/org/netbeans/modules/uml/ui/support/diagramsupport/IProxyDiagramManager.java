/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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


package org.netbeans.modules.uml.ui.support.diagramsupport;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.diagrams.DiagramDetails;
import org.netbeans.modules.uml.core.metamodel.diagrams.IBroadcastAction;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IProxyDiagramManager
{

        /**
         * Notifies the diagram to refresh the node graphical object that 
         * is associated with the presentation element.
         * 
         * @param presentation The presentation element that needs to be refreshed.
         */
        public void refresh(IPresentationElement presentation,boolean resizetocontent);
        
	/**
	 * This verifies that the diagram name is correct (ie no bad filename 
	 * characters like %*&(...) and returns the corrected one if not.
	 * 
	 * @param The name that was suggested.
	 * @return A valid diagram name.
	 */
//	public String getValidDiagramName( String sSuggestedDiagramName );

	/**
	 * Verifies that the diagram name is unique within the project base 
	 * directory.  The name that is return is the valid diagram name.
	 * 
	 * @param sProjectBaseDirectory The directory to contain the diagram.
	 * @param sProposedDiagramName The proposed name of the diagram.
	 * @return The valid diagram name.
	 */
	public String verifyUniqueDiagramName( String sProjectBaseDirectory, 
	                                       String sProposedDiagramName );

	/**
	 * Verifies that the diagram name is unique within the project base 
	 * directory.  The name that is return is the valid diagram name.
	 * 
	 * @param pElementInProject The used to retrieve the base directory.
	 * @param sProposedDiagramName The proposed name of the diagram.
	 * @return The valid diagram name.
	 */
	public String verifyUniqueDiagramName( IElement pElementInProject, 
	                                       String sProposedDiagramName );

	/**
	 * Moves the two diagram files (etl and etlp) files to the DiagramBackup
	 * folder underneith project.
	 * 
	 * @param sDiagramFullFilename The file name of the diagram.
	 */
	public void removeDiagram( String sDiagramFullFilename );

	/**
	 * Returns all the diagrams in this directory location.
	 */
	public ETList<IProxyDiagram> getDiagramsInDirectory( String sProjectBaseDirectory );

	/**
	 * Returns all the diagrams in this directory location.
	 */
	public ETList<IProxyDiagram> getDiagramsInDirectory( IElement pElementInProject );

	/**
	 * Returns a diagram proxy for this tom filename.  The diagram 
	 * proxy may represent a closed diagram.
	 */
	public IProxyDiagram getDiagram( String sTOMFilename );

	/**
	 * Returns a diagram proxy for this xmiid.  The diagram proxy may 
	 * represent a closed diagram.
	 */
	public IProxyDiagram getDiagramForXMIID( String sXMIID );

	/**
	 * Returns a diagram proxy for this IDiagram.
	 */
	public IProxyDiagram getDiagram( IDiagram pDiagram );

	/**
	 * Returns a diagram proxy for this name.  The diagram proxy may 
	 * represent a closed diagram.
	 */
	public ETList<IProxyDiagram> getDiagramsByName( String sName );

	/**
	 * Returns a diagram proxy for this name and namespace.  The diagram 
	 * proxy may represent a closed diagram.
	 */
	public ETList<IProxyDiagram> getDiagrams( String sName, INamespace pNamespace );

	/**
	 * Returns all the diagram proxies in the workspace.  The diagram 
	 * proxy may represent a closed diagram.
	 */
	public ETList<IProxyDiagram> getDiagramsInWorkspace();

	/**
	 * Returns all the diagram proxies in the namespace.  The diagram 
	 * proxy may represent a closed diagram.
	 */
	public ETList<IProxyDiagram> getDiagramsInNamespace( String sToplevelXMIID, 
	                                               String sXMIID );

   /**
    * Returns the namespace that ownes the diagram.
    * 
    * @param bDiagramFilename The file that defines the diagram.
    * @return The owner namespace.
    */
   public INamespace getDiagramNamespace(String bDiagramFilename);
   
	/**
	 * Returns all the diagram proxies in the namespace.  The diagram 
	 * proxy may represent a closed diagram.
	 */
	public ETList<IProxyDiagram> getDiagramsInNamespace( INamespace pNamespace );

	/**
	 * Returns a diagram proxy for this project.  The diagram proxy 
	 * may represent a closed diagram.
	 */
	public ETList<IProxyDiagram> getDiagramsInProject( IProject pProject );

	/**
	 * Returns a diagram proxy for this project.  The diagram proxy may 
	 * represent a closed diagram.
	 */
	public ETList<IProxyDiagram> getDiagramsInProject( String sProjectXMIID );

	public DiagramDetails getDiagramDetails(String bDiagramFilename);
	/**
	 * Returns true if bDiagramFilename represents a valid filename.  
	 * It looks for both .etp and .etl files.
	 */
	public boolean isValidDiagram( String bDiagramFilename );

	/**
	 * Get the presentation targets for a closed diagram
	 */
	public ETList<IPresentationTarget> getPresentationTargetsFromClosedDiagram( IElement pModelElement, 
	                                                                     String sDiagramFilename );

	/**
	 * Cracks open all closed diagrams and marks the elements as deleted.
	 */
	public void markPresentationTargetsAsDeleted( ETList<IVersionableElement> pElements );

	/**
	 * Sends a broadcast to all open diagrams.
	 */
	public void broadcastToAllOpenDiagrams( IBroadcastAction pAction );

	/**
	 * Returns true if any of the open diagrams for this project 
	 * are dirty.  If sProjectName is empty then all diagrams are looked at
	 */
	public boolean areAnyOpenDiagramsDirty( IProject pProject );

	/**
	 * This routine will go through the workspace and verify that all the
	 * diagrams exist and are valid - it will remove invalid ones if 
	 * bRemoveDeadOnes is true
	 */
	public int cleanWorkspaceOfDeadDiagrams( boolean bRemoveDeadOnes );

	/**
	 * Cleans the DiagramBackup folder within the project base directory 
	 * of all files.
	 */
	public void cleanDiagramBackupFolder( String sProjectName );

	/**
	 * Returns a list of diagrams this element is associated with.
	 */
	public ETList<IProxyDiagram> getAssociatedDiagramsForElement( String sElementXMIID );

	/**
	 * Returns a list of diagrams this element is associated with.
	 */
	public ETList<IProxyDiagram> getAssociatedDiagramsForElement( IElement pElement );

	/**
	 * Returns a list of diagrams this diagram is associated with.
	 */
	public ETList<IProxyDiagram> getAssociatedDiagramsForDiagram( String sDiagramXMIID );

	/**
	 * Returns a list of diagrams this diagram is associated with.
	 */
	public ETList<IProxyDiagram> getAssociatedDiagramsForDiagram( IProxyDiagram pProxyDiagram );

	/**
	 * Creates a diagram filename for use when creating a new diagram (stub or not).
	*/
	public String createDiagramFilename( INamespace pDiagramNamespace, String sDiagramName );

	/**
	 * Returns all the diagram proxies in the namespace.  If bDeepSearch is true, all diagrams found through all the namespace's children will also be found.
	*/
	public ETList<IProxyDiagram> getDiagramsInNamespace( String sToplevelXMIID, String sXMIID, boolean bDeepSearch );

	/**
	 * Returns all the diagram proxies in the namespace.  If bDeepSearch is true, all diagrams found through all the namespace's children will also be found.
	*/
	public ETList<IProxyDiagram> getDiagramsInNamespace( INamespace pSpace, boolean bDeepSearch );

	public ETPairT<Boolean, String> isValidDiagramName(String diaName);

}
