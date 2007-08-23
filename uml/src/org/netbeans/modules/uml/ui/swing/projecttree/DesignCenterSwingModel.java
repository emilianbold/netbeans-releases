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
 * Created on Jun 10, 2003
 * @author Trey Spiva
 */
package org.netbeans.modules.uml.ui.swing.projecttree;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.netbeans.modules.uml.ui.controls.projecttree.DefaultNodeFactory;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEngine;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeModelListener;
import org.netbeans.modules.uml.ui.controls.projecttree.ProjectTreeItemImpl;
import org.netbeans.modules.uml.ui.controls.projecttree.ProjectTreeModelEvent;
import org.netbeans.modules.uml.ui.controls.projecttree.ProjectTreeNode;
import org.netbeans.modules.uml.ui.controls.projecttree.ProjectTreeNodeFactory;
import org.netbeans.modules.uml.ui.controls.projecttree.TreeElementNode;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeElement;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ProjectTreeComparable;
import org.netbeans.modules.uml.core.IApplication;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlutils.DataFormatter;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.IDataFormatter;
import org.netbeans.modules.uml.core.support.umlutils.InvalidArguments;
import org.netbeans.modules.uml.core.workspacemanagement.IWSProject;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;
import javax.swing.tree.TreeNode;
import org.netbeans.modules.uml.ui.controls.projecttree.ProjectTreeModelAdapter;

/**
 * The Swing implementation of the project tree model.  The
 * <code>ProductProjectTreeModel</code> contains the bussiness logic required
 * to maintain the project tree data.  The <code>ProjectTreeSwingModel</code>
 * implements the Swing TreeModel interface.
 *
 * @see ProjectTreeSwingModel
 * @author Trey Spiva
 */
public class DesignCenterSwingModel extends ProjectTreeModelAdapter 
        implements ISwingProjectTreeModel
{
	/** The list of tree listeners to notify when an event occurs. */
	private ArrayList < TreeModelListener >  m_ModelListeners = new ArrayList < TreeModelListener >();
	public static String DESIGN_CENTER_NAME = ProjectTreeResources.getString("DesignCenterSwingModel.Design_Center_DisplayName"); //$NON-NLS-1$
	public static String DESIGN_CENTER_DESCRIPTION = ProjectTreeResources.getString("DesignCenterSwingModel.Design_Center_Description"); //$NON-NLS-1$

	/** The product to use that specifies the tree structure.*/
	private ICoreProduct m_Product = null;

	/** A table of the projects that are open. */
	//private HashMap < String, IProject > m_ProjectList = new HashMap < String, IProject >();
	protected ProjectTreeNode m_TreeRoot = null;

	/** The collection of project tree engines that are used by the model. */
	private ArrayList < IProjectTreeEngine > m_TreeEngines = new ArrayList < IProjectTreeEngine >();

	/** The collection of project tree engines that are used by the model. */
	private ArrayList < IProjectTreeModelListener > m_Listeners = new ArrayList < IProjectTreeModelListener >();

	/** The data that populates the project tree. */
	//private IProjectTreeModel m_ProjectTreeData = null;

        private ProjectTreeNodeFactory m_Factory = new DefaultNodeFactory();

	public DesignCenterSwingModel(ICoreProduct product)
	{
            initialize();
            setProduct(product);
	}

	public DesignCenterSwingModel()
	{
	}

    /**
     * Retreives the project tree model name.  The model name can be used to
     * determine the type of project tree that is being displayed.
     */
    public String getModelName()
    {
       return "DesignCenter"; //$NON-NLS-1$
    }

   /**
    * Retrieves the node factory to use when creating nodes for the model.
    *
    * @return The factory to use.
    */
   public ProjectTreeNodeFactory getNodeFactory()
   {
      return m_Factory;
   }

	/**
	 * Retrieves the collection of TreeModelListeners that are reqistered
	 * with the model.
	 *
	 * @return The tree modeled listeners.
	 */
	//protected ArrayList getModelListeners()
	protected ArrayList < TreeModelListener > getModelListeners()
	{
		return m_ModelListeners;
	}
	public IProjectTreeEngine getFirstEngine()
	{
		return m_TreeEngines.get(0);
	}
	//**************************************************
	// ISwingProjectTreeModel Implmentation
	//**************************************************

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.projecttree.ISwingProjectTreeModel#getTreeItem(javax.swing.tree.TreePath)
	 */
	public ITreeItem getTreeItem(TreePath path)
	{
      ITreeItem retVal = null;

		if(path.getLastPathComponent() != null)
		{
			if(path.getLastPathComponent() instanceof ITreeItem)
			{
            retVal = (ITreeItem)path.getLastPathComponent();
			}
		}

		return retVal;
	}

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeModel#clear()
    */
   public void clear()
   {
      clear(true);

      if(getRoot() != null)
      {
      	try
      	{
			fireTreeStructureChanged(getRoot(), new TreePath(getRoot()));
      	}
      	catch (Exception e)
      	{
      		//we get null pointer sometimes, just ignore it.
      	}
      }
   }

	//**************************************************
   // The TreeModel methods.
   //**************************************************

   /* (non-Javadoc)
    * @see javax.swing.tree.TreeModel#getRoot()
    */
   public Object getRoot()
   {
      return getRootItem();
   }

   /* (non-Javadoc)
	* @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
	*/
   public ITreeItem getChildItem(Object parent, int index)
   {
	  ITreeItem retVal = null;

	  if(parent instanceof ITreeItem)
	  {
		 ITreeItem parItem = (ITreeItem)parent;
		 if (parItem.getChildCount() > 0)
		 {
			retVal = ((ITreeItem)parent).getChild(index);
		 }
	  }

	  return retVal;
   }

   /* (non-Javadoc)
	* @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
	*/
   public int getChildCount(Object parent)
   {
	  int retVal = 0;

	  if(parent instanceof ITreeItem)
	  {
		 ITreeItem item = (ITreeItem)parent;
		 retVal = item.getChildCount();

		 if((retVal <= 0) && (item.isInitalized() == false))
		 {
			retVal = 1;
		 }
	  }

	  return retVal;
   }

   public ITreeItem getRootItem()
   {
	  return m_TreeRoot;
   }

   /* (non-Javadoc)
	* @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
	*/
   public boolean isLeaf(Object node)
   {
	  int children = getChildCount(node);
	  return (children == 0);
   }

   /**
	* Locates the nodes that represents the model element.
	*
	* @param element The model element to locate.
	* @return The tree Node.  <code>null</code> is returned if the node
	*         is not found.
	*/
   public ETList < ITreeItem > findNodes(Comparator < ITreeItem > comparator )
   {
	  ETList < ITreeItem > retVal = new ETArrayList < ITreeItem >();

	  findNodes(getRootItem(), comparator, retVal);

	  return retVal;
   }

   /**
	* Locate the node that represents the model element.
	*
	* @param element The model element to locate.
	* @return The tree Node.  <code>null</code> is returned if the node
	*         is not found.
	*/
   public ETList < ITreeItem > findNodes(final IElement element)
   {

	  return findNodes(new Comparator < ITreeItem >()
	  {
		 public int compare(ITreeItem o1,
							ITreeItem o2)
		 {
			return 0;
		 }

		 public boolean equals(Object obj)
		 {
			return obj.equals(element);
		 }
	  });
   }

   /**
	* Retrieves the index of a child node.
	*
	* @param parent The parent of the child node.
	* @param child The child node to find.
	* @return The index value.  -1 if the second parameter is not a child of the
	*         parent node.  The method <code>equals</code> is used to find the
	*         child node.
	*/
   public int getIndexOfChild(ITreeItem parent, ITreeItem child)
   {
	  int retVal = -1;

	  if((parent != null) && (child != null))
	  {
		 for(int index = 0; (index < parent.getChildCount()) && (retVal < 0); index++)
		 {
			if(child.equals(parent.getChild(index)) == true)
			{
			   retVal = index;
			}
		 }
	  }

	  return retVal;
   }

   /**
	* The IProject will be associated to the node that represents the IWSProject
	* element.
	*
	* @param pProject The project that has been opened.
	* @return The ITreeItem that is the project node.
	*/
   public ITreeItem projectOpened(IProject pProject)
   {
	  ITreeElement retVal = null;
	  if(pProject != null)
	  {
		 retVal = getProjectNode(pProject.getNameWithAlias());
		 if (retVal != null)
		 {
			retVal.setElement(pProject);
		 }
	  }

	  return retVal;
   }

   /**
	* Adds a listener for the TreeModelEvent posted after the tree changes.
	*
	* @param listener The lisener to add.
	*/
   public void addProjectTreeModelListener(IProjectTreeModelListener listener)
   {
	  m_Listeners.add(listener);
   }

   /**
	* Removes a listener previously added with <code>addProjectTreeModelListener</code>
	*
	* @param listener The listener to remove.
	*/
   public void removeProjectTreeModelListener(IProjectTreeModelListener listener)
   {
	  m_Listeners.remove(listener);
   }

   /**
	* @param element
	* @param item
	* @param items
	*/
   protected void findNodes(ITreeItem                parentItem,
							Comparator < ITreeItem > comparator,
							ETList < ITreeItem >     items)
   {
	  if((parentItem != null) && (items != null))
	  {
//		   if(parentItem.equals(element) == true)
		 if(comparator.equals(parentItem) == true)
		 {
			items.add(parentItem);
		 }

		 int childCnt = parentItem.getChildCount();
		 for(int index = 0; index < childCnt; index++)
		 {
			ITreeItem child = parentItem.getChild(index);
			findNodes(child, comparator, items);
		 }
	  }
   }

   /**
	* Locate the node that represents the model element.
	*
	* @param filename The name of the file that specifies the diagram.
	* @return The tree Nodes.  <code>null</code> is returned if the node
	*         is not found.
	*/
   public ETList < ITreeItem > findDiagramNodes(final String filename)
   {
	  return findNodes(new Comparator < ITreeItem >()
	  {
		 public int compare(ITreeItem o1, ITreeItem o2)
		 {
			return 0;
		 }

		 public boolean equals(Object obj)
		 {
			return obj.equals(filename);
		 }
	  });
   }

   /**
	* Sorts the children of a node.  The children will be sorted occuring to
	* the default sort order.
	*
	* @param parent The parent who children are to be sorted.
	*/
   public void sortChildren(ITreeItem parent)
   {
	  sortChildren(parent, new ProjectTreeComparable());
   }

   /**
	* Sorts the children of a node.  The children will be sorted occuring to
	* the Comparable interface.
	*
	* @param parent The parent who children are to be sorted.
	* @param compare The comparable interface used to sort the children.
	* @see Comparable
	*/
   public void sortChildren(ITreeItem parent, Comparator compare)
   {
	  if(parent != null)
	  {
		 parent.sortChildren();

		 ETList < ITreeItem > items = new ETArrayList < ITreeItem >();
		 items.add(parent);
		 notifyOfStructureChange(items);
		 parent.setIsInitalized(true);
	  }
   }

   /**
	* Test if it is OK to delete a tree item.  The registered IProjectTreeEngine
	* instances are first given chance to deny the delete operation.
	*
	* @param item The item to test.
	* @return <b>true</b> if it is OK to delete the tree item, <b>false</b> if
	*         it is not OK to delete the tree item.
	*/
   public boolean canDelete(IProjectTreeItem item)
   {
	  boolean retVal = true;

	  for (Iterator < IProjectTreeEngine > iter = m_TreeEngines.iterator(); iter.hasNext();)
	  {
		 IProjectTreeEngine engine = iter.next();
		 if(engine.canDelete(item) == false)
		 {
			retVal = false;
			break;
		 }
	  }

	  return retVal;
   }

   /**
	* Test if it is OK to edit a tree item.  The registered IProjectTreeEngine
	* instances are first given chance to deny the edit operation.
	*
	* @param item The item to test.
	* @return <b>true</b> if it is OK to delete the tree item, <b>false</b> if
	*         it is not OK to delete the tree item.
	*/
   public boolean canEdit(IProjectTreeItem item)
   {
	  boolean retVal = true;

	  for (Iterator < IProjectTreeEngine > iter = m_TreeEngines.iterator(); iter.hasNext();)
	  {
		 IProjectTreeEngine engine = iter.next();
		 if(engine.canEdit(item) == false)
		 {
			retVal = false;
			break;
		 }
	  }

	  return retVal;
   }

   /* (non-Javadoc)
    * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
    */
   public Object getChild(Object parent, int index)
   {
      return getChildItem(parent, index);
   }

   /* (non-Javadoc)
    * @see javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath, java.lang.Object)
    */
   public void valueForPathChanged(TreePath path, Object newValue)
   {
      // TODO Auto-generated method stub

   }

   /* (non-Javadoc)
    * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object, java.lang.Object)
    */
   public int getIndexOfChild(Object parent, Object child)
   {
		int retVal = 0;
		boolean parentIsItem = parent instanceof ITreeItem;
		boolean childIsItem  = child instanceof ITreeItem;

		if((parentIsItem == true) && (childIsItem == true))
		{
		   retVal = getIndexOfChild((ITreeItem)parent,(ITreeItem) child);
		}

		return retVal;
   }

   /* (non-Javadoc)
    * @see javax.swing.tree.TreeModel#addTreeModelListener(javax.swing.event.TreeModelListener)
    */
   public void addTreeModelListener(TreeModelListener l)
   {
		m_ModelListeners.add(l);

   }

   /* (non-Javadoc)
    * @see javax.swing.tree.TreeModel#removeTreeModelListener(javax.swing.event.TreeModelListener)
    */
   public void removeTreeModelListener(TreeModelListener l)
   {
		m_ModelListeners.remove(l);
   }

   /**
    * Sends the treeStructureChanged event to all registered TreeModelListeners.
    *
    * @parms items The tree items that has been changed.
    */
   public void notifyOfStructureChange(ETList < ITreeItem > items)
   {
      for (Iterator < ITreeItem > iter = items.iterator(); iter.hasNext();)
      {
         ITreeItem item = iter.next();

         if(item != null)
         {
            item.setIsInitalized(false);
            //item.removeAllChildren();

            fireTreeStructureChanged(this, new TreePath(item.getPath()));
         }
      }
   }

   /**
    * Notifies all listeners that a child was removed from its parent.
    * The mannor that the controls are notified is specific to the platform.
    * <br>
    * <b>Example:</b> For a Swing control the registered TreeModelListener will
    * recieve the treeNodesRemoved event.
    *
    * @parms parent The parent tree item that is affected.
    * @param childIndex The index of the child that was removed.
    * @param node The child node that was removed.
    */
   public void notifyOfRemovedChildren(ITreeItem   parent,
                                       int[]       childIndices,
                                       ITreeItem[] removedChildren)
   {
      if((removedChildren != null) && (childIndices != null))
      {
//         fireTreeNodesRemoved(this, getPathToRoot(parent), childIndices,
//                              removedChildren);
         fireTreeNodesRemoved(this, parent.getPath(), childIndices,
                                       removedChildren);
      }
   }

   /**
    * Notifies all listeners that a child was added to a parent by sending
    * the TreeModelListener event treeNodesInserted.
    *
    * @parms parent The parent tree item that is affected.
    * @param childIndices The index of the child nodes that was added.
    * @param children The children nodes that was added.
    */
   public void notifyOfAddedChildren(ITreeItem   parent,
                                     int[]       childIndices/*,
                                     ITreeItem[] children*/)
   {
      if(/*(children != null) && (*/(childIndices != null))
      {
         ITreeItem temp = getChildItem(parent, childIndices[0]);
         ITreeItem[] children = new ITreeItem[childIndices.length];
         for (int index = 0; index < childIndices.length; index++)
         {
            children[index] = parent.getChild(childIndices[index]);
         }

         fireTreeNodesInserted(this, getPathToRoot(parent), childIndices,
                               children);
      }
   }

   /**
    * Notifies all listeners that the content of some nodes has changed by
    * sending the TreeModelListener event treeNodesChanged.
    *
    * @parms parent The parent tree item that is affected.
    * @param childIndices The index of the child nodes that was added.
    * @param children The children nodes that was added.
    */
   public void notifyOfNodesChanged(ITreeItem   parent,
                                    int[]       childIndices,
                                    ITreeItem[] nodes)
   {
      if((nodes != null) && (childIndices != null))
      {
//         fireTreeNodesChanged(this, getPathToRoot(parent), childIndices,
//                               nodes);

         if(parent != null)
         {
            if (childIndices != null)
            {
               int cCount = childIndices.length;
               if(cCount > 0)
               {
                  Object[] cChildren = new Object[cCount];
                  for(int counter = 0; counter < cCount; counter++)
                  {
                     cChildren[counter] = parent.getChild(childIndices[counter]);
                  }

                  fireTreeNodesChanged(this, getPathToRoot(parent),
                                       childIndices, cChildren);
               }
            }
            else if (parent == getRoot())
            {
               fireTreeNodesChanged(this, getPathToRoot(parent), null, null);
            }
         }
      }
   }

   /**
    * Builds the parents of node up to and including the root node,
    * where the original node is the last element in the returned array.
    * The length of the returned array gives the node's depth in the
    * tree.
    *
    * @param aNode the TreeNode to get the path for
    */
   public Object[] getPathToRoot(ITreeItem node)
   {
      return getPathToRoot(node, 0);
   }

   /**
    * Builds the parents of node up to and including the root node,
    * where the original node is the last element in the returned array.
    * The length of the returned array gives the node's depth in the
    * tree.
    *
    * @param aNode  the TreeNode to get the path for
    * @param depth  an int giving the number of steps already taken towards
    *        the root (on recursive calls), used to size the returned array
    * @return an array of TreeNodes giving the path from the root to the
    *         specified node
    */
   protected ITreeItem[] getPathToRoot(ITreeItem aNode, int depth)
   {
      ITreeItem[] retNodes = null;

      // This method recurses, traversing towards the root in order
      // size the array. On the way back, it fills in the nodes,
      // starting from the root and working back to the original node.

      // Check for null, in case someone passed in a null node, or
      // they passed in an element that isn't rooted at root.
      if(aNode == null)
      {
         if(depth > 0)
         {
            retNodes = new ITreeItem[depth];
         }
      }
      else
      {
         depth++;
         if(aNode == getRoot())
         {
             retNodes = new ITreeItem[depth];
         }
         else
         {
             retNodes = getPathToRoot(aNode.getParentItem(), depth);
         }

         retNodes[retNodes.length - depth] = aNode;
      }

      return retNodes;
   }

   /**
	* Fires the projectOpened event to every registered IProjectTreeListener
	*
	* @param node The node that is affected.
	* @param project The project that is closed.
	*/
   public void fireProjectOpened(ITreeElement node, IProject project)
   {
	  ProjectTreeModelEvent event = new ProjectTreeModelEvent(this,
															  node,
															  project,
															  false);

	  for (Iterator < IProjectTreeModelListener >iter = m_Listeners.iterator(); iter.hasNext();)
	  {
		 IProjectTreeModelListener curListener = iter.next();
		 curListener.projectOpened(event);
	  }
   }

   /**
	* Fires the projectClosed event to every registered IProjectTreeListener
	*
	* @param node The node that is affected.
	* @param project The project that is closed.
	*/
   public void fireProjectClosed(ITreeElement node, IProject project)
   {
	  ProjectTreeModelEvent event = new ProjectTreeModelEvent(this,
															  node,
															  project,
															  true);

	  for (Iterator < IProjectTreeModelListener >iter = m_Listeners.iterator(); iter.hasNext();)
	  {
		 IProjectTreeModelListener curListener = iter.next();
		 curListener.projectClosed(event);
	  }
   }

   // **************************************************
   // Event Helper Methods
   //**************************************************

	/**
	 * Notifies all listeners that have registered interest for
	 * notification on this event type.  The event instance
	 * is lazily created using the parameters passed into
	 * the fire method.
	 *
	 * @param source the node being changed
	 * @param path the path to the root node
	 * @param childIndicies the indices of the changed elements
	 * @param children the changed elements
	 * @see EventListenerList
	 */
	protected void fireTreeNodesChanged(final Object source,
                                       final Object[] path,
	 											   final int[] childIndices,
												   final Object[] children)
	{
      final ArrayList < TreeModelListener > listeners = m_ModelListeners;

      SwingUtilities.invokeLater( new Runnable()
      {
         public void run()
         {
            TreeModelEvent e = null;

      		// Process the listeners last to first, notifying
      		// those that are interested in this event
      		for (int i = listeners.size() - 1; i >= 0; i--)
      		{
      			try
      			{
					// Lazily create the event:
					if (e == null)
					{
						e = new TreeModelEvent(source, path,
													  childIndices, children);
					}
					TreeModelListener listener = listeners.get(i);
					listener.treeNodesChanged(e);
      			}
      			catch (Exception exc)
      			{
      				exc.printStackTrace();
      				//just ignore this exception for the time being.
      			}
      		}
         }
      });
	}

	/**
	 * Notifies all listeners that have registered interest for
	 * notification on this event type.  The event instance
	 * is lazily created using the parameters passed into
	 * the fire method.
	 *
	 * @param source the node where new elements are being inserted
	 * @param path the path to the root node
	 * @param childIndicies the indices of the new elements
	 * @param children the new elements
	 * @see EventListenerList
	 */
	protected void fireTreeNodesInserted(final Object   source,
                                        final Object[] path,
													 final int[]    childIndices,
													 final Object[] children)
	{
      TreeModelEvent e = null;

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = m_ModelListeners.size() - 1; i >= 0; i--)
		{
			try
			{
				// Lazily create the event:
				if (e == null)
				 {
				 TreePath treePath = new TempTreePath(path);
				 TreePath parentPath = treePath.getParentPath();
					e = new TreeModelEvent(source, treePath,
												  childIndices, children);
				 }
				 TreeModelListener listener = m_ModelListeners.get(i);
				 listener.treeNodesInserted(e);
			}
			catch (Exception exc)
			{
				exc.printStackTrace();
				//just catch it for the time being.
			}
		}
	}

	/**
	 * Notifies all listeners that have registered interest for
	 * notification on this event type.  The event instance
	 * is lazily created using the parameters passed into
	 * the fire method.
	 *
	 * @param source the node where elements are being removed
	 * @param path the path to the root node from the childrens parent.
	 * @param childIndicies the indices of the removed elements
	 * @param children the removed elements
	 * @see EventListenerList
	 */
	protected void fireTreeNodesRemoved(Object source, Object[] path,
													int[] childIndices,
													Object[] children)
	{
		TreeModelEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = m_ModelListeners.size() - 1; i >= 0; i--)
		{
			try
			{
				// Lazily create the event:
				if (e == null)
				{
					e = new TreeModelEvent(source, path,
												  childIndices, children);
				}
				TreeModelListener listener = m_ModelListeners.get(i);
				listener.treeNodesRemoved(e);
			}
			catch (Exception exc)
			{
				exc.printStackTrace();
				//just ignore it for the time being.
			}
		}
	}

	/**
	 * Notifies all listeners that have registered interest for
	 * notification on this event type.  The event instance
	 * is lazily created using the parameters passed into
	 * the fire method.
	 *
	 * @param source the node where the tree model has changed
	 * @param path the path to the root node
	 * @param childIndicies the indices of the affected elements
	 * @param children the affected elements
	 * @see EventListenerList
	 */
	protected void fireTreeStructureChanged(Object   source,
                                           Object[] path,
											 int[]    childIndices,
											Object[] children)
	{
		TreeModelEvent e = null;

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = m_ModelListeners.size() - 1; i >= 0; i--)
		{
			try
			{
				// Lazily create the event:
				if (e == null)
				{
					e = new TreeModelEvent(source, path,
												  childIndices, children);
				}
				TreeModelListener listener = m_ModelListeners.get(i);
				listener.treeStructureChanged(e);
			}
			catch (Exception exc)
			{
				exc.printStackTrace();
				//just ignore it for the time being.
			}
		}
	}

	/**
	 * Notifies all listeners that have registered interest for
	 * notification on this event type.  The event instance
	 * is lazily created using the parameters passed into
	 * the fire method.
	 *
	 * @param source the node where the tree model has changed
	 * @param path the path to the root node
	 * @see EventListenerList
	 */
	protected void fireTreeStructureChanged(Object source, TreePath path)
	{
		TreeModelEvent e = null;

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = m_ModelListeners.size() - 1; i >= 0; i--)
		{
			try
			{
				// Lazily create the event:
				if (e == null)
				{
					e = new TreeModelEvent(source, path);
				}
				TreeModelListener listener = m_ModelListeners.get(i);
				listener.treeStructureChanged(e);
			}
			catch(Exception exp)
			{
				exp.printStackTrace();
				//just ignore it.
			}
		}
	}

	/**
	 * Initialize the model to the project.  This is a good
	 * place to initialize all sinks and project tree engines.
	 */
	public void initialize()
	{
	   try
	   {
			attachSinks();
			attachEngines();

			IProjectTreeItem item = new ProjectTreeItemImpl();
			item.setData(null);
			item.setDescription(DESIGN_CENTER_DESCRIPTION);
			item.setItemText(DESIGN_CENTER_NAME);

			m_TreeRoot = new ProjectTreeNode(item);
			m_TreeRoot.setExpanded(false);
			m_TreeRoot.setAllowsChildren(true);
			m_TreeRoot.setIsInitalized(false);
			m_TreeRoot.setName(DESIGN_CENTER_DESCRIPTION);

			//create a dummy node
//			IProjectTreeItem dummyItem = new ProjectTreeItemImpl();
//			dummyItem.setData(null);
//			dummyItem.setDescription(DESIGN_CENTER_DESCRIPTION);
//			dummyItem.setItemText(DESIGN_CENTER_DESCRIPTION);
//			ProjectTreeNode node = new ProjectTreeNode(dummyItem);
//			m_TreeRoot.addChild(node);

	   }
	   catch (InvalidArguments e)
	   {
		 // TODO: Figure out what to do about error handling.
	   }
	}

	public String getProjectTreeName()
	{
		return ProjectTreeResources.getString("DesignCenterSwingModel.Design_Center_Description"); //$NON-NLS-1$
	}

	public IProjectTreeItem addWorkspace(ITreeItem pParent, IWorkspace space)
	{
		IProjectTreeItem item = new ProjectTreeItemImpl();
		item.setData(space);
		item.setDescription(IProjectTreeControl.WORKSPACE_DESCRIPTION);
		item.setItemText(space.getName());
		item.setAsAddinNode(true);

		ITreeItem projectNode = new ProjectTreeNode(item);
		projectNode.setName(ProjectTreeResources.getString("DesignCenterSwingModel.Design_Center_Description")); //$NON-NLS-1$
		projectNode.setExpanded(false);
		projectNode.setIsInitalized(false);

		addItem(pParent, projectNode);

		//addProjects(space);

		if(getRoot() != null)
		{
		  try
		  {
			  fireTreeStructureChanged(getRoot(), new TreePath(getRoot()));
		  }
		  catch (Exception e)
		  {
			  //we get null pointer sometimes, just ignore it.
		  }
		}
		return item;
	}

	/**
	 * Initializes and attaches the engines required by the model.
	 */
	protected void attachEngines()
	{
		//This has to be uncommented after resolving cyclic dependency issues
	   //ADDesignCenterEngine adEngine = new ADDesignCenterEngine();
	   //adEngine.initialize(this);

	   //addEngine(adEngine);
	}

	public void addEngine(IProjectTreeEngine engine)
	{
	   m_TreeEngines.add(engine);
	}

	public void removeEngine(IProjectTreeEngine engine)
	{
	   m_TreeEngines.remove(engine);
	}


	/**
	 * Attaches all of the required listeners.
	 */
	protected void attachSinks()
	   throws InvalidArguments
	{
	}

	protected void detachSinks()
	  throws InvalidArguments
	{
	}


	/**
	 * Clears the content of the model.
	 *
	 * @param reload <b>true</b> if the workspace is to be reloaded.
	 *               <b>false</b> if the model is to be left empty.
	 */
	public void clear(boolean reload)
	{
      //there could be multiple workspaces in design center, so I need to find the right workspace
		//and then expand that.
      if (m_TreeRoot != null)
      {
//         ITreeItem item = getWorkspaceNode(space, m_TreeRoot);
//         m_TreeRoot.removeAllChildren();
//         m_TreeRoot = null;

         int childCount = m_TreeRoot.getChildCount();
         for(int index = 0; index < childCount; index++)
         {
            TreeNode node = m_TreeRoot.getChildAt(index);
            if(node instanceof ITreeItem)
            {
               ITreeItem item = (ITreeItem)node;
               item.removeAllChildren();
               item.setIsInitalized(false);
            }
            else if(node instanceof DefaultMutableTreeNode)
            {
               ITreeItem item = (ITreeItem)node;
               item.removeAllChildren();
            }
         }
      }

	   if(reload == true)
	   {
        //addProjects(space);
		  //setWorkspace(space);

//         IProjectTreeItem item = new ProjectTreeItemImpl();
//			item.setData(null);
//			item.setDescription(DESIGN_CENTER_DESCRIPTION);
//			item.setItemText(DESIGN_CENTER_NAME);
//
//			m_TreeRoot = new ProjectTreeNode(item);
//			m_TreeRoot.setExpanded(false);
//			m_TreeRoot.setAllowsChildren(true);
//			m_TreeRoot.setIsInitalized(false);
//			m_TreeRoot.setName(DESIGN_CENTER_DESCRIPTION);
	   }
	}

   public ITreeItem getWorkspaceNode(IWorkspace space, ITreeItem startNode)
   {
      ITreeItem retItem = null;
      if(m_TreeRoot != null && startNode == null)
      {
         //start from the root node.
         startNode = m_TreeRoot;
      }

      if (space != null)
      {
         int count = startNode.getChildCount();
         if (count > 0)
         {
            for (int i=0; i<count && retItem == null; i++)
            {
               ITreeItem item = startNode.getChild(i);
               String name = item.getDisplayedName();
               if (name != null && name.equals(space.getName()))
               {
                  Object obj = item.getData().getData();
                  if (obj instanceof IWorkspace)
                  {
                     retItem = item;
                  }
               }
               else
               {
                  retItem = getWorkspaceNode(space, item);
               }
            }
         }
      }
      return retItem;
   }

	/**
	 * Retrieves the product that specifies the tree structure.
	 *
	 * @return The product.
	 */
	public ICoreProduct getProduct()
	{
	   return m_Product;
	}

	/**
	 * Sets the product that specifies the tree structure.
	 *
	 * @param product The new product.
	 */
	public void setProduct(ICoreProduct product)
	{
	   DataFormatter formater;
	   m_Product = product;

      IWorkspace space = getWorkspace();
	   if(space != null)
	   {
		  setWorkspace(space);
	   }
	}

   public void closeProject(IProject proj)
   {
   	  //This has to be uncommented after resolving cyclic dependency issues
      //ICoreProduct pCore = ProductHelper.getCoreProduct();
     // if (pCore != null)
      //{
      //   IDesignCenterManager pManager = pCore.getDesignCenterManager();
      //   if (pManager != null)
      //   {
      //      if (pManager instanceof IADDesignCenterManager)
       //     {
      //         IADDesignCenterManager pADManager = (IADDesignCenterManager)pManager;
      //         IDesignPatternCatalog pCatalog = pADManager.getDesignPatternCatalog();
      //         if (pCatalog != null)
      //         {
      //
      //           pCatalog.closeProject(proj);
     //          }
     //       }
     //    }
     // }
   }

	public IWorkspace getWorkspace()
	{
	 // This has to be uncommented after resolving cyclic dependency issues
	 return null; //DesignPatternUtilities.getDesignPatternCatalogWorkspace();
	}

	/**
	 * @param workspace
	 */
	public void setWorkspace(IWorkspace workspace)
	{
//	   if (workspace != null)
//	   {
//		  IProjectTreeItem item = new ProjectTreeItemImpl();
//		  item.setData(workspace);
//		  item.setDescription(IProjectTreeControl.WORKSPACE_DESCRIPTION);
//		  item.setItemText(workspace.getName());
//
//		  ProjectTreeNode node = new ProjectTreeNode(item);
//
//		  // The C++ version relies on the expansion of the workspace to
//		  // gather the project information.  Since the Java version has
//		  // done away with the configuration manager (the model is the
//		  // configuration manager).  I will gather the project information
//		  // here.
//		  m_TreeRoot.addChild(node);
//		  addProjects(workspace);
//	   }
	}

	/**
	 * @return
	 */
	public ETArrayList < String > getUnfilteredProjects()
	{
	   // TODO Auto-generated method stub
	   return null;
	}

	/**
	  * Message this to remove node from its parent. This will message
	  * nodesWereRemoved to create the appropriate event. This is the
	  * preferred way to remove a node as it handles the event creation
	  * for you.
	  */
	 public void removeNodeFromParent(ITreeItem node)
	 {
		ITreeItem  parent = node.getParentItem();

		if(parent == null)
		{
			throw new IllegalArgumentException(ProjectTreeResources.getString("DesignCenterSwingModel.node_does_not_have_a_parent._4")); //$NON-NLS-1$
		}

		int[] childIndex = { getIndexOfChild(parent, node) };
		ITreeItem[] removedArray = { node };

		parent.removeChild(node);
		notifyOfRemovedChildren(parent, childIndex, removedArray);
	 }

	/**
	 * Retrieves the node that represents a project.  The project is located by
	 * name of the project.
	 *
	 * @param projName The name of the project to locate.
	 * @return The project node.
	 */
	protected ITreeElement getProjectNode(IProject project)
	{
	   ITreeElement retVal = null;

	   if(project != null)
	   {

		  IWorkspace ws = getWorkspace();
		  String name = project.getNameWithAlias();

		  boolean useFormatter = false;
		  if(ws != null)
		  {
			 IWSProject wsProject = ws.getWSProjectByName(project.getName());
			 if(wsProject != null)
			 {
				useFormatter = wsProject.isOpen();
			 }
		  }

		  if(useFormatter == true)
		  {
			 IDataFormatter formatter = ProductHelper.getDataFormatter();
			 retVal = getProjectNode(formatter.formatElement(project));
		  }
	   }

	   return retVal;
	}

	/**
	 * Retrieves the node that represents a project.  The project is located by
	 * name of the project.
	 *
	 * @param projName The name of the project to locate.
	 * @return The project node.
	 */
	protected ITreeElement getProjectNode(String projName)
	{
	   ITreeElement foundProject = null;

	   ITreeItem dpcItem = getCatalogNode();
	   if(dpcItem != null)
	   {
			int numChildren = dpcItem.getChildCount();
			for(int index = 0; (index < numChildren) && (foundProject == null); index++)
			{
			  ITreeItem curItem = dpcItem.getChild(index);
			  if (curItem != null)
			  {
				  IProjectTreeItem data = curItem.getData();
				  if(data.isProject() == true)
				  {
					 if(data.getItemText().equals(projName) == true)
					 {
						 if (curItem instanceof ITreeElement)
						 {
							foundProject = (ITreeElement)curItem;
							break;
						 }
					 }
				  }
			  }
			}
	   }
	   return foundProject;
	}
	private ITreeItem getCatalogNode()
	{
		ITreeItem dpcItem = null;
		ITreeItem rootItem = getRootItem();
		if(rootItem != null)
		{
			int numChildren = rootItem.getChildCount();
			if (numChildren == 1)
			{
				dpcItem = rootItem.getChild(0);
			}
		}
		return dpcItem;
	}
	/**
	 * @param workspace
	 */
	private void addProjects(IWorkspace workspace)
	{
		  try
		  {
			  // The design center shouldn't be adding projects.  The design center engine
			  // derives from this engine so make sure we're controlling the real project tree
			  // and not the design center tree before adding projects
			  if(!isProjectTree())
			  {
				  							// This has to be uncommented after resolving the cyclic dependency issues
			  	 IWorkspace pWork = null; //DesignPatternUtilities.getDesignPatternCatalogWorkspace();
			  	 if (pWork != null)
			  	 {
					 ETList<IWSProject> wsProjects = pWork.getWSProjects();
					 for (int index = 0; index < wsProjects.size(); index++)
					 {
					 IWSProject wsProject = wsProjects.get(index);

						String name = wsProject.getNameWithAlias();

						if(name.length() > 0)
						{
							  addProject(name, getCatalogNode(), null);
						}
					 }
				 }
			  }
		  }
		  catch (Exception e)
		  {
		  }
	}

	/**
	 * @param name
	 * @param workspace
	 * @param app
	 */
	protected ITreeElement addProject(String name,
									  ITreeItem parent,
									  IWorkspace workspace,
									  IApplication app)
	{
	   // TODO: I have to determine how to get the aliased Name
	   IProject project = app.getProjectByName(workspace, name);

	   ITreeElement projectNode = addProject(name, parent, project);
	   return projectNode;
	}

	public ITreeElement addProject(String name,
									ITreeItem parent,
									IProject project)
	{
	   ITreeElement projectNode = new TreeElementNode();
	   projectNode.setName(name);
	   projectNode.setElement(project);
	   projectNode.setDisplayedName(name);

	   if(projectNode.getData() != null)
	   {
		  projectNode.getData().setSortPriority(1);
		  projectNode.getData().setDescription(IProjectTreeControl.PROJECT_DESCRIPTION);
	   }

	   addItem(parent, projectNode);
	   return projectNode;
	}

	/**
	* Adds a node to the tree with a description and an alternate element.
	*
	* @param pParent The parent item for the one to create
	* @param sText The node text for this new item
	* @param nSortPriority The sort priority.  The lower the number the higher it will appear in the sibling list
	* @param pElement The element this item represents
	* @param pProjectTreeSupportTreeItem The ITreeItem from the project tree builder where we build the tree.
	* @param sDescription The description of this item.  Placed as part of the HTREEITEM's itemData
	* @return The created project tree item
	*/
	public IProjectTreeItem addItem(IProjectTreeItem  parent,
									String            name,
									String            text,
									long              sortPriority,
									IElement          element,
									Object            supportTreeItem,
									String            description)
	{
	   ITreeItem parentItem = null;
	   if(parent.getPath() != null)
	   {
		  ITreeItem[] path = parent.getPath();
		  parentItem = path[path.length - 1];
	   }

	   return addItem(parentItem,
					  name,
					  text,
					  sortPriority,
					  element,
					  supportTreeItem,
					  description);
	}

   protected int findLocation(ITreeItem parent, ITreeItem node)
   {
      int retVal = 0;

      //ProjectTreeComparable comparable = new ProjectTreeComparable();
      int max = parent.getChildCount();
      retVal = max;
      for(int index = 0; index < max; index++)
      {
         ITreeItem curChild = parent.getChild(index);
         if(curChild != null)
         {
            if(ProjectTreeComparable.compareTo(curChild, node) == ProjectTreeComparable.GREATER_THAN)
            {
               retVal = index;
               break;
            }
         }
      }

//      if(retVal >= max)
//      {
//         retVal = max - 1;
//      }

      return retVal;
   }

	/**
	 * Adds a node to the tree with a description and an alternate element.
	 *
	 * @param pParent The parent item for the one to create
	 * @param name The name of the node.  This is not the displayed text.
	 * @param sText The node text for this new item.  This is the displayed text.
	 * @param nSortPriority The sort priority.  The lower the number the higher it will appear in the sibling list
	 * @param pElement The element this item represents
	 * @param pProjectTreeSupportTreeItem The ITreeItem from the project tree builder where we build the tree.
	 * @param sDescription The description of this item.  Placed as part of the HTREEITEM's itemData
	 * @return The created project tree item
	 */
	public IProjectTreeItem addItem(ITreeItem parent,
									String    name,
									String    text,
									long      sortPriority,
									IElement  element,
									Object    supportTreeItem,
									String    description)
	{
	   IProjectTreeItem retVal = new ProjectTreeItemImpl();
	   retVal.setItemText(text);
	   retVal.setModelElement(element);
	   retVal.setDescription(description);
	   retVal.setSortPriority((int)sortPriority);

	   if(supportTreeItem instanceof ITreeItem)
	   {
		  retVal.setProjectTreeSupportTreeItem((ITreeItem)supportTreeItem);
	   }

	   ProjectTreeNode  node   = new ProjectTreeNode(retVal);
	   //parent.addChild(node);
	   node.setName(name);

      int location = findLocation(parent, node);
      parent.insertAt(node, location);
      node.setParentItem(parent);

	   Object[] path = node.getPath();
	   if (path != null)
	   {
		  ITreeItem[] treeItems = new ITreeItem[path.length];
		  for (int index = 0; index < path.length; index++)
		  {
			  if (path[index] instanceof ITreeItem)
			  {
				 treeItems[index] = (ITreeItem)path[index];

			  }
		  }
		  retVal.setPath(treeItems);
	   }

      int[] childIndices = { location };
      notifyOfAddedChildren(parent, childIndices);

//      ITreeItem[] nodes = { (ITreeItem)node };
//      notifyOfNodesChanged(parent,
//                           childIndices,
//                           nodes);
	   return retVal;
	}

	public void addItem(ITreeItem parent, ITreeItem folder)
   {
      // I have to call setParentItem and pass in null because the
      // call to addChild does not like it when a childs parent is
      // already set.  Since the process of building the tree can
      // cause the parent to be set I must first remove the parents.

      if (parent != null && folder != null)
      {
         folder.setParentItem(null);
         int count = parent.getChildCount();
         boolean found = false;

         for (int i=0; i<count; i++)
         {
            ITreeItem item = parent.getChild(i);
            if (item.equals(folder))
            {
               found = true;
               break;
            }
         }

         if (!found)
         {
            //parent.addChild(folder);
            int location = findLocation(parent, folder);
            parent.insertAt(folder, location);
            folder.setParentItem(parent);

            // For some reason when I notify about an insert a gap is created.
            // So, I am not going to notify about the gap and only notify the
            // nodes that they have changed.
            int[] childIndices = { location };
            notifyOfAddedChildren(parent, childIndices);

//            ITreeItem[] nodes = { folder };
//            notifyOfNodesChanged(parent,
//                                 childIndices,
//                                 nodes);
         }

      }
   }

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
	public void insertItem(ITreeItem parent, ITreeItem node, int index)
	{
	   parent.insertAt(node, index);

	   int[] childIndices = { index };
	   notifyOfAddedChildren(parent, childIndices);
	}

	/**
	 * Remove all instances of the model element from the tree.
	 *
	 * @param element The element to remove.
	 */
	public void removeAll(IElement element)
	{
	   ETList < ITreeItem > nodes = findNodes(element);
	   if((nodes != null) && (nodes.size() > 0))
	   {
		  for (Iterator < ITreeItem > iter = nodes.iterator(); iter.hasNext();)
		  {
			 ITreeItem node = iter.next();
			 removeNodeFromParent(node);
		  }
	   }
	}

	public boolean isProjectTree()
	{
	   return false;
	}

	public void setProjectTree(JTree tree)
	{
		tree.setShowsRootHandles(true);
		tree.setRootVisible(true);
		tree.collapseRow(0);
	}

   public class TempTreePath extends TreePath
   {
      /**
       * Tests two TreePaths for equality by checking each element of the
       * paths for equality. Two paths are considered equal if they are of
       * the same length, and contain
       * the same elements (<code>.equals</code>).
       *
       * @param o the Object to compare
       */
      public boolean equals(Object o)
      {
         if(o == this)
         {
            return true;
         }

         if(o instanceof TreePath)
         {
            TreePath            oTreePath = (TreePath)o;
            if(getPathCount() != oTreePath.getPathCount())
               return false;
            for(TreePath path = this; path != null; path = path.getParentPath())
            {
               if (!(path.getLastPathComponent().equals(oTreePath.getLastPathComponent())))
               {
                  return false;
               }
               oTreePath = oTreePath.getParentPath();
            }

            return true;
         }
         return false;
      }

      /**
       * @param path
       */
      public TempTreePath(Object[] path)
      {
         super(path);
      }
   }
}
