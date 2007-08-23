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
 * Created on May 27, 2003
 *
 */
package org.netbeans.modules.uml.ui.controls.projecttree;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.Comparator;

import javax.swing.SwingUtilities;

import org.netbeans.modules.uml.core.IApplication;
import org.netbeans.modules.uml.core.coreapplication.CoreProductInitEventsAdapter;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.metamodel.structure.ProjectEventsAdapter;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlutils.InvalidArguments;
import org.netbeans.modules.uml.core.workspacemanagement.IWSProject;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;
import org.netbeans.modules.uml.core.workspacemanagement.WSProjectEventsAdapter;
import org.netbeans.modules.uml.core.workspacemanagement.WorkspaceEventsAdapter;
import org.netbeans.modules.uml.ui.products.ad.projecttreedefaultengine.ADProjectTreeEngine;
import org.netbeans.modules.uml.ui.support.DispatchHelper;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeElement;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ProjectTreeComparable;
import org.netbeans.modules.uml.core.support.umlutils.DataFormatter;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.IDataFormatter;

/**
 * The bussiness logic that is need to maintain the project tree information.
 * The ProductProjectTreeModel uses implementaiton of
 * <code>IProjectTreeEngine</code> to customize how project tree data is
 * gathered.  The data of a project tree nodes are represented by in ITreeItem
 * implementations.
 *
 * @see IProjectTreeEngine
 * @see ITreeItem
 * @author Trey Spiva
 */
public abstract class ProductProjectTreeModel implements IProjectTreeModel
{
   /** The product to use that specifies the tree structure.*/
   private ICoreProduct m_Product = null;
   
   /** A table of the projects that are open. */
   //private HashMap < String, IProject > m_ProjectList = new HashMap < String, IProject >();
   private ProjectTreeNode m_TreeRoot = null;
   
   /** The collection of project tree engines that are used by the model. */
   private ArrayList < IProjectTreeEngine > m_TreeEngines = new ArrayList < IProjectTreeEngine >();
   
   /** The collection of project tree engines that are used by the model. */
   private ArrayList < IProjectTreeModelListener > m_Listeners = new ArrayList < IProjectTreeModelListener >();
   
   private DispatchHelper                m_DispatcherHelper = new DispatchHelper();
   private WorkspaceEventHandler         m_WorkspaceHandler = new WorkspaceEventHandler();
   private WSProjectEventsHandler        m_WSProjectHandler = new WSProjectEventsHandler();
   private ProjectEventHandler           m_ProjectHandler   = new ProjectEventHandler();
   
   private ProjectTreeNodeFactory m_Factory = new DefaultNodeFactory();
   
   public ProductProjectTreeModel()
   {
      
   }
   
   public ProductProjectTreeModel(ICoreProduct product)
   {
      setProduct(product);
      initialize();
   }
   
   
   /**
    * Retreives the project tree model name.  The model name can be used to 
    * determine the type of project tree that is being displayed.
    */
   public String getModelName()
   {
      return "ProjectTree"; //$NON-NLS-1$
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
   
	public IProjectTreeEngine getFirstEngine()
	{
		return m_TreeEngines.get(0);
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
         
      }
      catch (InvalidArguments e)
      {
         // TODO: Figure out what to do about error handling.
      }
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
   
   /**
    * Clears the content of the model and reload the workspace.  This basically
    * a refresh.
    */
   public void clear()
   {
      clear(true);
   }
   
   /**
    * Clears the content of the model.
    *
    * @param reload <b>true</b> if the workspace is to be reloaded.
    *               <b>false</b> if the model is to be left empty.
    */
   public void clear(boolean reload)
   {
      if(m_TreeRoot != null)
      {
         m_TreeRoot.removeAllChildren();
         m_TreeRoot = null;
      }
      
      if(reload == true)
      {
         setWorkspace(getWorkspace());
      }
   }
   
   //**************************************************
   // Getter And Setter Methods
   //**************************************************
   
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
      
      if(getWorkspace() != null)
      {
         setWorkspace(getWorkspace());
      }
   }
   
   public IWorkspace getWorkspace()
   {
      IWorkspace retVal = null;
      if(getProduct() != null)
      {
         retVal = getProduct().getCurrentWorkspace();
      }
      
      return retVal;
   }
   
   /**
    * @param workspace
    */
   public void setWorkspace(IWorkspace workspace)
   {
      if (workspace != null)
      {
         IProjectTreeItem item = new ProjectTreeItemImpl();
         item.setData(workspace);
         item.setDescription(IProjectTreeControl.WORKSPACE_DESCRIPTION);
         item.setItemText(workspace.getName());
         
         m_TreeRoot = new ProjectTreeNode(item);
         
         // The C++ version relies on the expansion of the workspace to
         // gather the project information.  Since the Java version has
         // done away with the configuration manager (the model is the
         // configuration manager).  I will gather the project information
         // here.
         addProjects(workspace);
      }
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
         if(isProjectTree() == true)
         {
            IApplication app = ProductHelper.getApplication();
            ETArrayList < String > projectsToShow = getUnfilteredProjects();
            
            ETList<IWSProject> wsProjects = workspace.getWSProjects();
            for (int index = 0; index < wsProjects.size(); index++)
            {
               IWSProject wsProject = wsProjects.get(index);
               
               String name = wsProject.getNameWithAlias();
               
               if(name.length() > 0)
               {
                  if(projectsToShow == null)
                  {
                     addProject(name, getRootItem(), workspace, app);
                  }
                  else
                  {
                     if(projectsToShow.contains(name) == true)
                     {
                        addProject(name,  getRootItem(), workspace, app);
                     }
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
   
   protected ITreeElement addProject(String name,
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
      parent.insertAt(node, findLocation(parent, node));
      //parent.addChild(node);
      node.setName(name);
      
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
            
//            int max = count;
//            int[] updated = new int[max - location];
//            ITreeItem[] updatedItems =  new ITreeItem[updated.length];
//            for(int index = 0; index < updated.length; index++)
//            {
//               updated[index] = location + index;
//               updatedItems[index] = getChildItem(parent, updated[index]);
//            }
//            notifyOfNodesChanged(parent, updated, updatedItems);
         }
      }      
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
      
      return retVal;
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
    * @return
    */
   public ETArrayList < String > getUnfilteredProjects()
   {
      // TODO Auto-generated method stub
      return null;
   }
   
   public boolean isProjectTree()
   {
      return false;
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
         throw new IllegalArgumentException("node does not have a parent.");
      }
      
      int[] childIndex =
      { getIndexOfChild(parent, node) };
      ITreeItem[] removedArray =
      { node };
      
      parent.removeChild(node);
      notifyOfRemovedChildren(parent, childIndex, removedArray);
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
//      if(parent != null)
//      {
//         if(parent.getChildCount() > 1)
//         {
//            parent.sortChildren();
//            
//            ETList < ITreeItem > items = new ETArrayList < ITreeItem >();
//            items.add(parent);
//            notifyOfStructureChange(items);
//            parent.setIsInitalized(true);
//         }
//      }
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
         //         if(parentItem.equals(element) == true)
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
   
   //   /**
   //    * Locate the node that represents the model element.
   //    *
   //    * @param filename The name of the file that specifies the diagram.
   //    * @return The tree Nodes.  <code>null</code> is returned if the node
   //    *         is not found.
   //    */
   //   public void findDiagramNodes(String                    filename,
   //                                ITreeItem                 parentItem,
   //                                ETList < ITreeItem >        items)
   //   {
   //      if((filename != null) && (parentItem != null) && (items != null))
   //      {
   //         if(parentItem instanceof ITreeDiagram)
   //         {
   //            if(parentItem.equals(filename) == true)
   //            {
   //               items.add(parentItem);
   //            }
   //         }
   //
   //         int childCnt = parentItem.getChildCount();
   //         for(int index = 0; index < childCnt; index++)
   //         {
   //            ITreeItem child = parentItem.getChild(index);
   //            findDiagramNodes(filename, child, items);
   //         }
   //      }
   //   }
   //**************************************************
   // Event Methods
   //**************************************************
   
   
   
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
   
   //**************************************************
   // Helper Methods
   //**************************************************
   
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
      
      ITreeItem rootItem = getRootItem();
      if(rootItem != null)
      {
         int numChildren = rootItem.getChildCount();
         for(int index = 0; (index < numChildren) && (foundProject == null); index++)
         {
            ITreeItem curItem = rootItem.getChild(index);
            IProjectTreeItem data = curItem.getData();
            if(data.isProject() == true)
            {
               if(data.getItemText().equals(projName) == true)
               {
                  if (curItem instanceof ITreeElement)
                  {
                     foundProject = (ITreeElement)curItem;
                  }
               }
            }
         }
      }
      
      return foundProject;
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
    * Initializes and attaches the engines required by the model.
    */
   protected void attachEngines()
   {
      ADProjectTreeEngine adEngine = new ADProjectTreeEngine();
      adEngine.initialize(this);
      
      addEngine(adEngine);
   }
   
   /**
    * Attaches all of the required listeners.
    */
   protected void attachSinks()
   throws InvalidArguments
   {
      m_DispatcherHelper.registerForWorkspaceEvents(m_WorkspaceHandler);
      m_DispatcherHelper.registerForWSProjectEvents(m_WSProjectHandler);
      m_DispatcherHelper.registerForProjectEvents(m_ProjectHandler);
   }
   
   protected void detachSinks()
   throws InvalidArguments
   {
      m_DispatcherHelper.revokeWorkspaceSink(m_WorkspaceHandler);
      m_DispatcherHelper.revokeWSProjectSink(m_WSProjectHandler);
      m_DispatcherHelper.revokeProjectSink(m_ProjectHandler);
   }
   
   //**************************************************
   // Sink Implementations
   //**************************************************
   
   protected class CoreProductInitHandler extends CoreProductInitEventsAdapter
   {
      /* (non-Javadoc)
       * @see com.embarcadero.describe.coreapplication.ICoreProductInitEventsSink#onCoreProductPreQuit(com.embarcadero.describe.coreapplication.ICoreProduct, com.embarcadero.describe.umlsupport.IResultCell)
       */
      public void onCoreProductPreQuit(ICoreProduct arg0, IResultCell arg1)
      {
         try
         {
            detachSinks();
            clear(false);
         }
         catch (InvalidArguments e)
         {
            
         }
      }
   }
   
   protected class WorkspaceEventHandler extends WorkspaceEventsAdapter
   {
      public void onWorkspaceOpened(IWorkspace space, IResultCell cell)
      {
         SwingUtilities.invokeLater(new Runnable()
         {
            public void run()
            {
               clear();
            }
         });
      }
      
      public void onWorkspaceClosed(IWorkspace space, IResultCell cell)
      {
         clear(false);
      }
   }
   
   public void handleWSProjectRemoved(IWSProject project, IResultCell cell)
   {
   }
   public void handleWSProjectInserted(IWSProject project, IResultCell cell)
   {
   }
   
   protected class WSProjectEventsHandler extends WSProjectEventsAdapter
   {
      public void onWSProjectRemoved(IWSProject project, IResultCell cell)
      {
         handleWSProjectRemoved(project, cell);
      }
      
      public void onWSProjectInserted(IWSProject project, IResultCell cell)
      {
         handleWSProjectInserted(project, cell);
      }
   }
   
   public void handleProjectCreated(IProject project, IResultCell cell)
   {
   }
   public void handleProjectOpened(IProject project, IResultCell cell)
   {
   }
   public void handleProjectClosed(IProject project, IResultCell cell)
   {
   }
   public void handleProjectRenamed(IProject project, String oldName, IResultCell cell)
   {
   }
   
   protected class ProjectEventHandler extends ProjectEventsAdapter
   {
      public void onModeModified(IProject pProject, IResultCell cell)
      {
      }
      
      public void onDefaultLanguageModified(IProject pProject, IResultCell cell)
      {
      }
      
      public void onProjectCreated(IProject project, IResultCell cell)
      {
         handleProjectCreated(project, cell);
      }
      
      public void onProjectOpened(IProject project, IResultCell cell)
      {
         handleProjectOpened(project, cell);
      }
      
      public void onProjectRenamed(IProject project,
      String oldName,
      IResultCell cell)
      {
         handleProjectRenamed(project, oldName, cell);
      }
      
      public void onProjectClosed(IProject project, IResultCell cell)
      {
         handleProjectClosed(project, cell);
      }
      
      public void onProjectSaved(IProject Project, IResultCell cell)
      {
      }
   }
}

