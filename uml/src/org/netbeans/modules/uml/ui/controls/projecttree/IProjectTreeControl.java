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


package org.netbeans.modules.uml.ui.controls.projecttree;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.workspacemanagement.IWSProject;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem;

public interface IProjectTreeControl
{
	/** The description of that specifies a Workspace object. */
	public final static String WORKSPACE_DESCRIPTION = "Workspace";
	
	/** The description of that specifies a Project object. */
	public final static String PROJECT_DESCRIPTION = "Workspace Project";
	
	/**
	 * Returns the root nodes for the project tree.
	 */
	public ETList<IProjectTreeItem> getRootNodes();

	/**
	 * Returns the name of the configuration manager for this tree.
	*/
	public String getConfigMgrName();

	/**
	 * Returns a standard description type based on the enumeration
	 */
	public String getStandardDescription( /* ProjectTreeDescriptionKind */ int nKind );

	/**
	 * Has the user called ShowWorkspaceNode
	 */
	public boolean getUserCalledShowWorkspaceNode();

	/**
	 * Shows or hides the workspace node.  If the workspace is closed then its shown if open then this flag takes affect
	 */
	public boolean getShowWorkspaceNode();

	/**
	 * Shows or hides the workspace node.  If the workspace is closed then its shown if open then this flag takes affect
	 */
	public void setShowWorkspaceNode( boolean value );

	/**
	 * Adds the workspace as the primary node in the tree
	 */
	public long addWorkspace();

	/**
	 * If you want to only show certain objects in the tree then add to this list the project names to be shown.  If the list is empty then all projects are shown.
	 */
	public IStrings getUnfilteredProjects();

	/**
	 * If you want to only show certain objects in the tree then add to this list the project names to be shown.  If the list is empty then all projects are shown.
	 */
	public void setUnfilteredProjects( IStrings value );

	/**
	 * Refresh the project tree
	 */
	public void refresh( boolean bPostEvent );

	/**
	 * Close the project tree
	 */
	public void close();

	/**
	 * Sort the child items of this tree item
	 */
	public void sortChildNodes( IProjectTreeItem pParent );

	/**
	 * Sort this node within the tree
	 */
	public void sortThisNode( IProjectTreeItem pNode );

//	/**
//	 * Sets an image for a particular node.
//	 */
//	public void setImage( IProjectTreeItem pItem, String sIconLibrary, int nIconID );
//
//	/**
//	 * Sets an image for a particular node.
//	 */
//	public void setImage2( IProjectTreeItem pItem, int hIcon );
    
    /**
     * Sets the name of the node that is associated with the project tree item.
     * The name of the node can be used to determine the icon that is displayed.
     *
     * @param pItem The tree item to update.
     * @param name The name of the node.
     */
    public void setNodeName(IProjectTreeItem pItem, String name);

	/**
	 * Sets a description for a particular node.
	 */
	public void setDescription( IProjectTreeItem pItem, String sDesc );

	/**
	 * Sets a secondary description for a particular node.
	 */
	public void setSecondaryDescription( IProjectTreeItem pItem, String sDesc );

	/**
	 * Sets a the dispatch pointer for a particular node.
	 */
	public void setDispatch( IProjectTreeItem pItem, Object pDisp );

	/**
	 * Sets the model element for a particular node.
	 */
	public void setModelElement( IProjectTreeItem pItem, IElement pEle );

	/**
	 * Adds a node to the tree
	 */
	public IProjectTreeItem addItem( IProjectTreeItem pParent,
                                    String programName, 
	                                 String sText, 
	                                 int nSortPriority, 
	                                 IElement pElement);

	/**
	 * Adds a node to the tree with a description
	 */
	public IProjectTreeItem addItem( IProjectTreeItem pParent,
                                    String programName, 
	                                 String sText, 
	                                 int nSortPriority, 
	                                 IElement pElement, 
	                                 String sDescription );

	/**
	 * Adds a node to the tree with a description and a ITreeItem found in ProjectTreeSupport
	 */
	public IProjectTreeItem addItem( IProjectTreeItem pParent,
                                    String programName,
	                                 String sText,
                                    int nSortPriority, 
                                    IElement pElement, 
                                    Object pProjectTreeSupportTreeItem, 
                                    String sDescription );

	/**
	 * Start Editing the first selected item
	 */
	public void beginEditFirstSelected();

	/**
	 * Start Editing this item
	 */
	public void beginEditThisItem( IProjectTreeItem pItem );

	/**
	 * Get the selected items
	 */
	public IProjectTreeItem[] getSelected();

	/**
	 * Deselects all the items
	 */
	public void deselectAll();

	/**
	 * End the editing of the project tree node
	 */
	public void endEditing( boolean bSaveChanges );

	/**
	 * Are we editing a node?
	 */
	public boolean isEditing();

	/**
	 * Delete the selected items
	 */
	public void deleteSelectedItems();

	/**
	 * Set the text for an item
	 */
	public void setText( IProjectTreeItem pItem, String sText );

	/**
	 * Get the parent for an item
	 */
	public IProjectTreeItem getParent( IProjectTreeItem pItem );

	/**
	 * Get the parent IProject for an item
	 */
	public IProject retrieveProjectFromItem( IProjectTreeItem pProjItem );

	/**
	 * Get the current projects in the tree
	 */
	public ETList<IProjectTreeItem> getProjects();

	/**
	 * Returns the workspace node in the tree.  NULL if the workspace node is not shown.
	 */
	public IProjectTreeItem getWorkspaceTreeItem();

	/**
	 * Get the immediate children for a node
	 */
	public ETList<IProjectTreeItem> getChildren( IProjectTreeItem pItem );

	/**
	 * Get the immediate diagram children for a node
	 */
	public IProjectTreeItem[] getChildDiagrams( IProjectTreeItem pItem );

	/**
	 * Find a particular node in the tree.  May exist in several places.
	 */
	public ETList<IProjectTreeItem> findNode(ITreeItem parent, String sTopLevelXMIID, String itemXMIID );

	/**
	 * Find a particular node in the tree.  May exist in several places.
	 */
	public ETList<IProjectTreeItem> findNode2( IElement pElement );

	/**
	 * Returns the direct children of pParent that have the description.
	 */
	public ETList<IProjectTreeItem> findNodeWithDescription( IProjectTreeItem pParent, 
	                                                   String sDescription,
	                                                   boolean bCallRecursively );

	/**
	 * Returns the direct children of pParent that have the metatype sMetaType
	 */
	public ETList<IProjectTreeItem> findNodesRepresentingMetaType( IProjectTreeItem pParent, 
	                                                         String sMetaType );

	/**
	 * Find a particular diagram node in the tree.  May exist in several places.
	 */
	public ETList<IProjectTreeItem> findDiagramNode(ITreeItem parent, String sTOMFilename );

	/**
	 * Removes a particular diagram node in the tree.  May exist in several places.
	 */
	public void removeDiagramNode( String sTOMFilename, boolean bPostEvent );

	/**
	 * Removes the HTREEITEMS represented by the IProjectTreeItems.
	 */
	public void removeFromTree( IProjectTreeItem[] pRemovedItems );

	/**
	 * Removes the HTREEITEMS represented by the IElement.
	 */
	public void removeFromTree2( IElement pElementToRemove );

	/**
	 * Selects this project in the tree.
	 */
	public void selectProject( IProject pProject );

	/**
	 * Locks or Unlocks the window update for quicker response.
	 */
	public void lockWindowUpdate( boolean bLock );

	/**
	 * Get the current drop target
	 */
	public IProjectTreeItem getDropTargetItem();

	/**
	 * Close this project
	 */
	public void closeProject( IProject pProject );

	/**
	 * Open this project
	 */
	public void openProject( IProject pProject );

	/**
	 * Returns true if only these kinds of tree items are selected.  The input is a long made up of ProjectTreeItemKind's
	 */
	public boolean areJustTheseSelected( int items );

	/**
	 * Returns true if at least 1 of these kinds of tree items are selected.  The input is a long made up of ProjectTreeItemKind's
	 */
	public boolean atLeastOneOfTheseSelected( int items );

	/**
	 * Get the first selected model element
	 */
	public IElement getFirstSelectedModelElement(  );

	/**
	 * Get the first selected model element
	 */
	public IProjectTreeItem getFirstSelectedModelElementItem(  );

	/**
	 * Get the first selected diagram
	 */
	public String getFirstSelectedDiagram(  );

	/**
	 * Get the first selected opened diagram
	 */
	public String getFirstSelectedOpenDiagram(  );

	/**
	 * Get the first selected closed diagram
	 */
	public String getFirstSelectedClosedDiagram( );

	/**
	 * Get the first selected opened project
	 */
	public String getFirstSelectedOpenProject(  );

	/**
	 * Get the first selected closed project
	 */
	public String getFirstSelectedClosedProject( );

	/**
	 * Returns the HWND to the drawing area.
	 */
	public int getWindowHandle();

	/**
	 * Searches for this element in the tree, selects it and makes it visible.
	 */
	public void findAndSelectInTree( IElement pModelElement );

	/**
	 * Has this item ever been expanded?
	 */
	public boolean getHasBeenExpanded( IProjectTreeItem pItem );

	/**
	 * Is this element expanded.
	 */
	public boolean getIsExpanded( IProjectTreeItem pItem );

	/**
	 * Is this element expanded.
	 */
	public void setIsExpanded( IProjectTreeItem pItem, boolean value );

	/**
	 * Is this element selected.
	 */
	public boolean getIsSelected( IProjectTreeItem pItem );

	/**
	 * Is this element selected.
	 */
	public void setIsSelected( IProjectTreeItem pItem, boolean value );

	/**
	 * Remembers the current tree state.
	 */
	public void rememberTreeState();

	/**
	 * Restores the last remembered tree state.
	 */
	public void restoreTreeState();

	/**
	 * Opens the selected diagrams.
	 */
	public ETList<IProxyDiagram> openSelectedDiagrams();

	/**
	 * Closes the selected diagrams.
	 */
	public ETList<IProxyDiagram> closeSelectedDiagrams();

	/**
	 * Saves the selected diagrams.
	 */
	public ETList<IProxyDiagram> saveSelectedDiagrams();

	/**
	 * Opens the selected projects.
	 */
	public void openSelectedProjects(IWorkspace space);

	/**
	 * Closes the selected projects.
	 */
	public void closeSelectedProjects(IWorkspace space);

	/**
	 * Saves the selected projects.
	 */
	public void saveSelectedProjects(IWorkspace space);

	/**
	 * Called when a workspace points at a project that could not be located
	 */
	public void handleLostProject( IWSProject wsProject );

	/**
	 * Ask the user what to do about a name collision
	 */
	public void questionUserAboutNameCollision( INamedElement pElement, 
	                                            String sProposedName, 
	                                            ETList<INamedElement> pCollidingElements, 
	                                            IResultCell pCell );

	/**
	 * Gets the number of open projects in the tree
	 */
	public int getNumOpenProjects();

	/**
	 * Tells the tree to delete a model element when it next processes the queue
	 */
	public void postDeleteModelElement( String sTopLevelXMIID, String sXMIID );
	
	public void beginEditContext();

}
