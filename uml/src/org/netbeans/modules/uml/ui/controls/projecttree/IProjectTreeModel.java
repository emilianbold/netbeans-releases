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


/*
 *
 * Created on Jun 2, 2003
 * @author Trey Spiva
 */
package org.netbeans.modules.uml.ui.controls.projecttree;

import java.util.Comparator;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;

/**
 * 
 * @author Trey Spiva
 */
public interface IProjectTreeModel
{
   /**
    * Adds a listener for the TreeModelEvent posted after the tree changes.
    * 
    * @param listener The lisener to add.
    */
   public void addProjectTreeModelListener(IProjectTreeModelListener listener);
   
   /**
    * Removes a listener previously added with <code>addProjectTreeModelListener</code>
    * 
    * @param listener The listener to remove.
    */
   public void removeProjectTreeModelListener(IProjectTreeModelListener listener);
   
   /**
    * Returns the child of parent at a given index in the parent's child  
    * array.  parent must be a node previously obtained from this data source.  
    * This  should not return null if index is a valid index for parent (that is 
    * index >= 0 && index < getChildCount(parent)). 
    * 
    * @param parent A ITreeItem in the tree, obtained from this data source 
    * @param index The child to retrieve.
    * @return
    */
	public ITreeItem getChildItem(Object parent, int index);
	
	/**
    * Returns the number of children of parent. Returns 0 if the node is a leaf 
    * or if it has no children. parent must be a ITreeItem previously obtained 
    * from this data source. 
    * 
	 * @param parent A ITreeItem in the tree, obtained from this data source 
	 * @return
	 */
	public int getChildCount(Object parent);
	
	/**
    * Returns <code>true</code> if node is a leaf. It is possible for this 
    * method to return <code>false</code> even if node has no children. A 
    * directory in a filesystem, for example, may contain no files; the node 
    * representing the directory is not a leaf, but it also has no children. 
    * 
	 * @param node A ITreeItem in the tree, obtained from this data source 
	 * @return
	 */
	public boolean isLeaf(Object node);
	
	/**
	 * Retrieves the workspace that the project tree is associated
	 * with.
	 * 
	 * @return The workspace.
	 */
	public IWorkspace getWorkspace();

   /**
    * Retrieve the tree's root item.
    * 
    * @return The root item.
    */
   public ITreeItem getRootItem();
   
   public IProjectTreeItem addItem(IProjectTreeItem parent,
                                   String           name,
                                   String           text,
                                   long             sortPriority,
                                   IElement         element,
                                   Object           supportTreeItem,
                                   String           description);
   
   public IProjectTreeItem addItem(ITreeItem parent, 
                                   String    name,
                                   String    text, 
                                   long      sortPriority, 
                                   IElement  element, 
                                   Object    supportTreeItem,
                                   String    description);
   
   /**
    * Apends the node to the end of the parents child list.
    * 
    * @param parent The parent to recieve the new node.
    * @param node The node to be added.
    */
   public void addItem(ITreeItem parent, ITreeItem node);
   
   /**
    * Inserts the new node into the parent child list at a specified location.
    * The notifyOfAddedChildren will be sent after the node is added.
    * 
    * @param parent The parent to recieve the new node.
    * @param node The node to be added.
    * @param index The index to insert the node.  If the index is greater than
    *              the number of children the node will be appended to the
    *              end of the child list.
    */
//   public void insertItem(ITreeItem parent, ITreeItem node, int index);
   
   
   /**
    * Remove all instances of the model element from the tree.
    * 
    * @param element The element to remove.
    */
   public void removeAll(IElement element);
   
   /** 
    * Remove the specified node from its parent node.  All model listeners will
    * be notified of the change.
    * 
    * @param node The node to remove.
    */
   public void removeNodeFromParent(ITreeItem node);
      
   /**
    * The IProject will be associated to the node that represents the IWSProject
    * element.
    * 
    * @param pProject The project that has been opened.
    * @return The ITreeItem that is the project node.
    */
   public ITreeItem projectOpened(IProject pProject);
   
   /**
    * Clears the content of the model.  This basically a refresh.
    */
   public void clear();
   
   /**
    * Retrieves the index of a child node.  
    * 
    * @param parent The parent of the child node.
    * @param child The child node to find.
    * @return The index value.  -1 if the second parameter is not a child of the 
    *         parent node.  The method <code>equals</code> is used to find the
    *         child node.
    */
   public int getIndexOfChild(ITreeItem parent, ITreeItem child);
   
   /**
    * Sorts the children of a node.  The children will be sorted occuring to
    * the default sort order.
    * 
    * @param parent The parent who children are to be sorted.
    */
   public void sortChildren(ITreeItem parent);
   
   /**
    * Sorts the children of a node.  The children will be sorted occuring to
    * the Comparable interface.
    * 
    * @param parent The parent who children are to be sorted.
    * @param compare The comparable interface used to sort the children.
    * @see Comparable
    */
   public void sortChildren(ITreeItem parent, Comparator compare);
   
   /**
    * Locates the nodes that represents the model element.
    * 
    * @param element The model element to locate.
    * @return The tree Node.  <code>null</code> is returned if the node 
    *         is not found.
    */
   public ETList < ITreeItem > findNodes(IElement element);
   
   /**
    * Locates the nodes that represents the diagram.
    * 
    * @param filename The name of the file that specifies the diagram.
    * @return The tree Nodes.  <code>null</code> is returned if the node 
    *         is not found.
    */
   public ETList < ITreeItem > findDiagramNodes(String filename);
   
   /**
    * Locates the nodes that represents the model element.
    * 
    * @param element The model element to locate.
    * @return The tree Node.  <code>null</code> is returned if the node 
    *         is not found.
    */
   public ETList < ITreeItem > findNodes(Comparator < ITreeItem > comparator );
   
   /**
    * Notifies all listeners that some of the nodes structure has changed.  
    * The mannor that the controls are notified is specific to the platform.
    * <br>
    * <b>Example:</b> For a Swing control the registered TreeModelListener will
    * recieve the treeStructureChanged event.
    * 
    * @parms items The tree items that has been changed. 
    */
   public void notifyOfStructureChange(ETList < ITreeItem > items);
   
   /**
    * Notifies all listeners that a child was removed from its parent.  
    * The mannor that the controls are notified is specific to the platform.
    * <br>
    * <b>Example:</b> For a Swing control the registered TreeModelListener will
    * recieve the treeNodesRemoved event.
    * 
    * @parms parent The parent tree item that is affected.
    * @param childIndices The index of the child nodes that was removed.
    * @param children The children nodes that was removed.
    */
   public void notifyOfRemovedChildren(ITreeItem   parent, 
                                       int[]       childIndices, 
                                       ITreeItem[] children);
                                       
   /**
    * Notifies all listeners that a child was added to a parent.  
    * The mannor that the controls are notified is specific to the platform.
    * <br>
    * <b>Example:</b> For a Swing control the registered TreeModelListener will
    * recieve the treeNodesInserted event.
    * 
    * @parms parent The parent tree item that is affected.
    * @param childIndices The index of the child nodes that was added.
    * @param children The children nodes that was added.
    */
   public void notifyOfAddedChildren(ITreeItem   parent, 
                                     int[]       childIndices/*, 
                                     ITreeItem[] children*/);
                                     
   /**
    * Notifies all listeners that the content of some nodes has changed.  
    * The mannor that the controls are notified is specific to the platform.
    * <br>
    * <b>Example:</b> For a Swing control the registered TreeModelListener will
    * recieve the treeNodesChanged event.
    * 
    * @parms parent The parent tree item that is affected.
    * @param childIndices The index of the child nodes that was added.
    * @param children The children nodes that was added.
    */
   public void notifyOfNodesChanged(ITreeItem   parent, 
                                    int[]       childIndices, 
                                    ITreeItem[] nodes);
   
   /**
    * Test if it is OK to delete a tree item.
    * 
    * @param item The item to test.
    * @return <b>true</b> if it is OK to delete the tree item, <b>false</b> if 
    *         it is not OK to delete the tree item.
    */
   public boolean canDelete(IProjectTreeItem item);
   
   /**
	* Test if it is OK to edit a tree item.
	* 
	* @param item The item to test.
	* @return <b>true</b> if it is OK to delete the tree item, <b>false</b> if 
	*         it is not OK to delete the tree item.
	*/
   public boolean canEdit(IProjectTreeItem item);

   /**
    * Retrieves the node factory to use when creating nodes for the model.
    * 
    * @return The factory to use.
    */
   public ProjectTreeNodeFactory getNodeFactory();
   
   
   /**
    * Retreives the name of the model.
    */
   public String getModelName();
   
   /**
    * Fires an event when a given tree node is expanded
    * 
    * @param item The item to be expanded.
    * 
    */
   public void fireItemExpanding(ITreeItem item);
   
}
