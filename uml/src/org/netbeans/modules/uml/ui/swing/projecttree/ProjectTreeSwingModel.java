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
import java.util.Iterator;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;

import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.netbeans.modules.uml.ui.controls.projecttree.ProductProjectTreeModel;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProxyUserInterface;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeElement;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlsupport.Log;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.IDataFormatter;
import org.netbeans.modules.uml.core.workspacemanagement.IWSProject;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;

/**
 * The Swing implementation of the project tree model.  The 
 * <code>ProductProjectTreeModel</code> contains the bussiness logic required
 * to maintain the project tree data.  The <code>ProjectTreeSwingModel</code>
 * implements the Swing TreeModel interface.
 * 
 * @see ProjectTreeSwingModel
 * @author Trey Spiva
 */
public class ProjectTreeSwingModel extends ProductProjectTreeModel 
  implements ISwingProjectTreeModel

{
	/** The list of tree listeners to notify when an event occurs. */
	private ArrayList < TreeModelListener >  m_ModelListeners = new ArrayList < TreeModelListener >();
   
   
	
	/** The data that populates the project tree. */
	//private IProjectTreeModel m_ProjectTreeData = null;
	
	public ProjectTreeSwingModel(ICoreProduct product)
	{
		//m_ProjectTreeData = new ProductProjectTreeModel(product);
      super(product);
	}
	
	public ProjectTreeSwingModel()
	{
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
      super.clear();
      
      if(getRoot() != null)
      {
      	fireTreeStructureChanged(getRoot(), new TreePath(getRoot()));
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
      return super.getRootItem();
   }

   /* (non-Javadoc)
    * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
    */
   public Object getChild(Object parent, int index)
   {
      return super.getChildItem(parent, index);
   }

   /* (non-Javadoc)
    * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
    */
   public int getChildCount(Object parent)
   {
		return super.getChildCount(parent);
   }

   /* (non-Javadoc)
    * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
    */
   public boolean isLeaf(Object node)
   {
		return super.isLeaf(node);
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
         retVal = super.getIndexOfChild((ITreeItem)parent,(ITreeItem) child);
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
//      if((nodes != null) && (childIndices != null)) 
//      {
////         fireTreeNodesChanged(this, getPathToRoot(parent), childIndices, 
////                               nodes);
//
//         if(parent != null) 
//         {
//            if (childIndices != null)
//            {
//               int cCount = childIndices.length;
//               if(cCount > 0) 
//               {
//                  childIndices = getAffectedChildren(parent, childIndices);
//                  if(childIndices != null)
//                  {
//                     parent.sortChildren();
//                     ITreeItem[] affectedNodes = new ITreeItem[childIndices.length];
//                     for(int index = 0; index < childIndices.length; index++)
//                     {
//                        affectedNodes[index] = parent.getChild(index);
//                        fireTreeNodesChanged(this, getPathToRoot(parent),
//                                             childIndices, affectedNodes);
//                     }
//                  }
//               }
//            }
//            else if (parent == getRoot()) 
//            {
//               fireTreeNodesChanged(this, getPathToRoot(parent), null, null);
//            }
//         }
//      }
      
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

   protected int[] getAffectedChildren(ITreeItem parent, int[] children)
   {
      int[] retVal = null;      
      
      if((parent != null) && (children != null))
      {
         if(children.length == 1)
         {
            int location = findLocation(parent, parent.getChild(children[0]));
            if(children[0] < location)
            {
               retVal = getIndexBetween(children[0], parent.getChildCount()- 1);
            }
            else if(children[0] > location)
            {
               retVal = getIndexBetween(location, parent.getChildCount() - 1 );
            }
            else
            {
               retVal = new int[1];
               retVal[0] = children[0];
            }
         }
         else
         {
            int startLocation = parent.getChildCount() - 1;
            
            for(int index = 0; index < children.length; index++)
            {
               ITreeItem child = parent.getChild(index);
               if(child != null)
               {
                  int location = findLocation(parent, parent.getChild(children[index]));
                  if(location < startLocation)
                  {
                     startLocation = location;
                  }
               }
            }
            
            for(int cIndex = 0; cIndex < children.length; cIndex++)
            {
               if(children[cIndex] < startLocation)
               {
                  startLocation = children[cIndex];
               }
            }
            
            retVal = getIndexBetween(startLocation, parent.getChildCount() - 1);
         }
      }
      
      return retVal;
   }
   
   protected int[] getIndexBetween(int start, int end)
   {
      int[] retVal = new int[end - start];
      
      for(int index = 0, value = start; index < retVal.length; index++, value++)
      {
         retVal[index] = value;
      }
      
      return retVal;
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
      				Log.stackTrace(exc);
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
				Log.stackTrace(exc);
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
				Log.stackTrace(exc);
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
				Log.stackTrace(exc);
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
				Log.stackTrace(exp);
				//just ignore it.
			}
		}	
	}

	public String getProjectTreeName()
	{
		return ProjectTreeResources.getString("ProjectTreeSwingModel.ProjectTree_Name"); //$NON-NLS-1$
	}

	public void setProjectTree(JTree tree) {
            // Initialize based on perferences.
            //kris richards - ShowWorkspaceNode pref expunged. set to false.
            tree.setRootVisible(false ); //$NON-NLS-1$
            tree.setShowsRootHandles(true); //$NON-NLS-1$
        }

	public IProjectTreeItem addWorkspace(ITreeItem pParent, IWorkspace space)
	{
		setWorkspace(space);
		return null;
	}

   public void handleProjectCreated(IProject project, IResultCell cell)
   {
      if (isProjectTree())
      {
         if(project != null)
         {
            ITreeItem prjNode = projectOpened(project);
         }
      }
   }

   public void handleProjectOpened(IProject project, IResultCell cell)
   {
      if (isProjectTree())
      {
         if(project != null)
         {
            ITreeElement prjNode = getProjectNode(project);
            
            if(prjNode != null)
            {
               prjNode.setElement(project);
               
               int[]       childIndices = { super.getIndexOfChild(getRootItem(), prjNode) };
               ITreeItem[] children     = { prjNode };
               notifyOfNodesChanged(getRootItem(), childIndices, children);
               fireProjectOpened(prjNode, project);
            }
         }
      }
   }

   public void handleProjectClosed(IProject project, IResultCell cell)
   {
      if (isProjectTree())
      {
         if(project != null)
         {
            ITreeElement prjNode = getProjectNode(project);
            
            if(prjNode != null)
            {
               fireProjectClosed(prjNode, project);
               
               ITreeItem[] rootChildren  =
               { prjNode };
               int[]       projectIndice =
               { super.getIndexOfChild(getRootItem(), prjNode) };
               
               prjNode.setElement(null);
               prjNode.setIsInitalized(false);
               notifyOfNodesChanged(getRootItem(), projectIndice, rootChildren);
               
               ITreeItem[] children     = new ITreeItem[prjNode.getChildCount()];
               int[]       childIndices = new int[prjNode.getChildCount()];
               
               for(int index = 0; index < children.length; index++)
               {
                  children[index] = prjNode.getChild(index);
                  childIndices[index] = index;
               }
               
               prjNode.removeAllChildren();
               notifyOfRemovedChildren(prjNode, childIndices, children);
            }
         }
      }
   }

   public void handleProjectRenamed(IProject project, String oldName, IResultCell cell)
   {
      if (isProjectTree())
      {
         if(project != null)
         {
            ITreeElement prjNode = getProjectNode(oldName);
            if(prjNode != null)
            {
               IDataFormatter formatter = ProductHelper.getDataFormatter();
               String formatName = formatter.formatElement(project);
               prjNode.setDisplayedName(formatName);
               prjNode.setName(formatName);
               
               int[]       childIndices =
               { super.getIndexOfChild(getRootItem(), prjNode) };
               ITreeItem[] children     =
               { prjNode };
               notifyOfNodesChanged(getRootItem(), childIndices, children);
            }
         }
      }
   }

   public void handleWSProjectRemoved(IWSProject project, IResultCell cell)
   {
//      if (isProjectTree())
//      {
//         String projName = project.getNameWithAlias();
//         
//         ITreeItem foundProject = getProjectNode(projName);
//         
//         if(foundProject != null)
//         {
//            //rootItem.removeChild(foundProject);
//            removeNodeFromParent(foundProject);
//         }
//      }
   }

   public void handleWSProjectInserted(IWSProject project, IResultCell cell)
   {
//      if (isProjectTree())
//      {
//         if(getRootItem() != null)
//         {
//            ITreeItem root = getRootItem();
//            
//            // The design center shouldn't be adding projects.  The design center engine
//            // derives from this engine so make sure we're controlling the real project tree
//            // and not the design center tree before adding projects
//            if(isProjectTree() == true)
//            {
//               IApplication app = ProductHelper.getApplication();
//               IWorkspace   ws  = ProductHelper.getWorkspace();
//               ETArrayList < String > projectsToShow = getUnfilteredProjects();
//               
//               String name = project.getNameWithAlias();
//               
//               ITreeItem node = null;
//               if(name.length() > 0)
//               {
//                  if(projectsToShow == null)
//                  {
//                     node = addProject(name, root, ws, app);
//                  }
//                  else
//                  {
//                     if(projectsToShow.contains(name) == true)
//                     {
//                        node = addProject(name,  root, ws, app);
//                     }
//                  }
//               }
//               
//               if (node != null)
//               {
//                  int[] childIndex =
//                  { root.getChildCount() - 1 };
//                  //ITreeItem[] addeddArray = { node };
//                  
//                  //notifyOfAddedChildren(root, childIndex, addeddArray);
//                  notifyOfAddedChildren(root, childIndex);
//               }
//            }
//         }
//      }
   }

   public boolean isProjectTree()
   {
      return true;
   }
   
   public void closeProject(IProject proj)
   {
      IProxyUserInterface pUI = ProductHelper.getProxyUserInterface();
      pUI.closeProject(proj);
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

    public void fireItemExpanding(ITreeItem item) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
