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

package org.netbeans.modules.uml.project.ui;

import org.netbeans.modules.uml.project.ui.nodes.UMLRelationshipNode;
import java.lang.ref.WeakReference;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.WeakHashMap;

import org.netbeans.api.project.Project;
import org.openide.util.Lookup;

import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.ui.products.ad.projecttreedefaultengine.FilteredItemManager;
import org.netbeans.modules.uml.ui.support.diagramsupport.ProxyDiagramManager;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeDiagram;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeFolder;
import org.netbeans.modules.uml.ui.swing.drawingarea.DrawingAreaEventsAdapter;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.scm.ISCMDiagramItem;
import org.netbeans.modules.uml.core.scm.ISCMElementItem;
import org.netbeans.modules.uml.core.scm.ISCMEventsSink;
import org.netbeans.modules.uml.core.scm.ISCMItem;
import org.netbeans.modules.uml.core.scm.ISCMItemGroup;
import org.netbeans.modules.uml.core.scm.ISCMOptions;
import org.netbeans.modules.uml.core.scm.SCMFeatureKind;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventDispatcher;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeExpandingContext;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.netbeans.modules.uml.ui.controls.projecttree.ProjectTreeExpandingContextImpl;
import org.netbeans.modules.uml.ui.controls.projecttree.ProjectTreeItemImpl;
import org.netbeans.modules.uml.ui.controls.projecttree.ProjectTreeModelAdapter;
import org.netbeans.modules.uml.ui.controls.projecttree.ProjectTreeNode;
import org.netbeans.modules.uml.ui.controls.projecttree.ProjectTreeNodeFactory;
import org.netbeans.modules.uml.ui.support.DispatchHelper;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem;

import org.netbeans.modules.uml.project.ui.nodes.UMLDiagramsRootNode;
import org.netbeans.modules.uml.project.ui.nodes.UMLModelRootNode;
import org.netbeans.modules.uml.project.UMLProjectHelper;
import org.netbeans.modules.uml.project.ui.nodes.UMLDiagramNode;
import org.netbeans.modules.uml.project.ui.nodes.NBNodeFactory;
import java.util.Enumeration;

/**
 *
 * @author Trey Spiva
 */
public class NetBeansUMLProjectTreeModel extends ProjectTreeModelAdapter
	implements ISCMEventsSink
{
	private ITreeItemExpandContext mContext = null;
	private DispatchHelper m_DispatcherHelper = new DispatchHelper();
	private ProjectTreeNodeFactory mFactory = new NBNodeFactory();
	
	// All the nodes under the Model root node:
	// key=<diagramName>#<diagramType>, example: MyDiagram#ClassDiagram_CLOSED;
	// value=<weak reference to node>, example: #360
	private HashMap<String, ETList<WeakReference<ITreeItem>>> mNodeMap =
		new HashMap<String, ETList<WeakReference<ITreeItem>>>();
	
	// key=Absolute path/file name of the diagrams .etld file;
	// value=<diagramName>#<diagramType>, example: MyDiagram#ClassDiagram_CLOSED
	private HashMap<String, String> mDiagramNodeMap =
		new HashMap<String, String>();
	
	// Just one entry; the ModelRootNode's xmi id is the key and the value
	//  is the instance reference # of the Diagrams Root Node,
	//  example: "DCE.96692F3F-E206-862D-D95E-79BF5ABF480B"=>#252
	private WeakHashMap<String, UMLDiagramsRootNode> mDiagramsNodeMap =
		new WeakHashMap<String, UMLDiagramsRootNode>();
	
	private FilteredItemManager diagramOnlyFilter = new FilteredItemManager();
	private DrawingAreaSink mDrawingAreaSink = new DrawingAreaSink();
	private IProject mProject = null;
	private Object lock = new Object();
	
	/**
	 *
	 */
	public NetBeansUMLProjectTreeModel()
	{
		super();
		diagramOnlyFilter.turnOffModelElements();
		m_DispatcherHelper.registerForSCMEvents(this);
		
	}
	
	public ProjectTreeNodeFactory getNodeFactory()
	{
		return mFactory;
	}
	
	public IDrawingAreaEventsSink getDrawingAreaListener()
	{
		return mDrawingAreaSink;
	}
	
	public void fireDiagramsExpanding(UMLDiagramsRootNode node, IProject project)
	{
		try
		{
			IProjectTreeEventDispatcher disp = 
				m_DispatcherHelper.getProjectTreeDispatcher();
			
			if (disp != null)
			{
				IEventPayload payload = 
					disp.createPayload("ProjectTreeItemExpanding"); // NOI18N
				IProjectTreeExpandingContext expandingContext = 
					new ProjectTreeExpandingContextImpl(node);
				
				disp.fireItemExpandingWithFilter(
					null, expandingContext, diagramOnlyFilter, payload);
			}
			
			mDiagramsNodeMap.put(project.getXMIID(), node);
		}
		
		catch (Exception e)
		{
		}
	}
	
	public void fireItemExpanding(ITreeItem item, ITreeItemExpandContext context)
	{
		try
		{
			mContext = context;
			
			IProjectTreeEventDispatcher disp = 
				m_DispatcherHelper.getProjectTreeDispatcher();
			
			if (disp != null)
			{
				// Debug.out.println("In fireItemExpanding " + disp);
				IEventPayload payload = 
					disp.createPayload("ProjectTreeItemExpanding"); // NOI18N
				
				IProjectTreeExpandingContext expandingContext = 
					new ProjectTreeExpandingContextImpl(item);
				
				disp.fireItemExpanding(null, expandingContext, payload);
			}
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		finally
		{
			mContext = null;
		}
	}

	public void fireItemExpanding(ITreeItem item)
	{
            fireItemExpanding(item, null);
        }
		
        
   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeModel#addItem(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem, java.lang.String, java.lang.String, long, org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, java.lang.Object, java.lang.String)
	*/
	public IProjectTreeItem addItem(
		IProjectTreeItem parent,
		String name,
		String text,
		long sortPriority,
		IElement element,
		Object supportTreeItem,
		String description)
	{
		IProjectTreeItem retVal = createProjectTreeItem(
			text, sortPriority, element, supportTreeItem, description);
		
		// notifyContextOfAdd(retVal);
		//Debug.out.println("addItem 1: [name="+name+", text="+text+"]");
		
		return retVal;
	}
	
   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeModel#addItem(org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem, org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem)
	*/
	public void addItem(ITreeItem parent, ITreeItem node)
	{
		// since folder node is always created with a new UMLFolderNode
		// we have to remove the old folder node before adding the new one #6283081
		if (node instanceof ITreeFolder)
		{
			Enumeration<ITreeItem> kids = parent.getNodeChildren();
			ITreeItem curChild = null;
        
			while (kids.hasMoreElements())
			{
				curChild = kids.nextElement();
				if ((curChild instanceof ITreeFolder) && 
					curChild.getType().equals(node.getType()))
					parent.removeChild(curChild);
			}
		}
        if (!alreadyHasChild(parent, node) && 
            !(parent instanceof UMLRelationshipNode))
        {
    		parent.addChild(node);
		
        	if (!(node instanceof ITreeFolder))
             	addNode(node);
        }
	}
	
    
    public boolean alreadyHasChild(ITreeItem parent, ITreeItem child)
        throws IllegalArgumentException
    {
        if (parent == null || child == null)
            throw new IllegalArgumentException(
                "Neither parent nor child argument can be null"); // NOI18N
        
        if (parent.getChildCount() == 0)
            return false;
        
        Enumeration<ITreeItem> kids = parent.getNodeChildren();
        ITreeItem curChild = null;
        
        while (kids.hasMoreElements())
        {
            curChild = kids.nextElement();
            if (curChild == child || curChild.equals(child))
                return true;
        }
        
        return false;
    }
    
    
   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeModel#addItem(org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem, java.lang.String, java.lang.String, long, org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, java.lang.Object, java.lang.String)
	*/
	public IProjectTreeItem addItem(
		ITreeItem parent,
		String name,
		String text,
		long sortPriority,
		IElement element,
		Object supportTreeItem,
		String description)
	{
		IProjectTreeItem retVal = createProjectTreeItem(
			text, sortPriority, element, supportTreeItem, description);
		
		// Debug.out.println("addItem 1: [name="+name+", text=" + text + "]");
		// notifyContextOfAdd(retVal);
		
		return retVal;
	}
	
	/**
	 * Locate the node that represents the model element.
	 *
	 * @param element The model element to locate.
	 * @return The tree Node.  <code>null</code> is returned if the node
	 *         is not found.
	 */
	public ETList<ITreeItem> findNodes(final IElement element)
	{
		ETList<ITreeItem> retVal = new ETArrayList<ITreeItem>();
		
		ETList<WeakReference<ITreeItem>> list = 
			mNodeMap.get(element.getXMIID());
		
		if (list != null)
		{
			for (WeakReference<ITreeItem> ref : list)
			{
				if (ref != null)
				{
					ITreeItem item = ref.get();
					
					if (item != null)
						retVal.add(item);
				}
			}
		}
		
		return retVal;
	}
	
	/**
	 * Locates the nodes that represents the model element.
	 *
	 * @param element The model element to locate.
	 * @return The tree Node.  <code>null</code> is returned if the node
	 *         is not found.
	 */
	public ETList<ITreeItem> findNodes(Comparator<ITreeItem> comparator )
	{
		ETList<ITreeItem> retVal = new ETArrayList<ITreeItem>();
		
		// findNodes(getRootItem(), comparator, retVal);
		
		return retVal;
	}
	
	/**
	 * Locate the node that represents the model element.
	 *
	 * @param filename The name of the file that specifies the diagram.
	 * @return The tree Nodes.  <code>null</code> is returned if the node
	 *         is not found.
	 */
	public ETList<ITreeItem> findDiagramNodes(final String filename)
	{
		//      Debug.out.println("Got findDiagramNodes in NetBeansUMLProjectTreeModel");
		//      TopComponent tc = WindowManager.getDefault().findTopComponent("project");
		//      if (tc != null)
		//      {
		//         Debug.out.println("got the tc");
		//      }
		//
		//      return findNodes(new Comparator < ITreeItem >()
		//      {
		//         public int compare(ITreeItem o1, ITreeItem o2)
		//         {
		//            return 0;
		//         }
		//
		//         public boolean equals(Object obj)
		//         {
		//            return obj.equals(filename);
		//         }
		//      });
		
		String key = mDiagramNodeMap.get(filename);
		ETList<ITreeItem> retVal = new ETArrayList<ITreeItem>();
		
		if (key != null)
		{
			// Now we got the key we can get the list from the node list.
			ETList<WeakReference<ITreeItem>> list = mNodeMap.get(key);
			
			if (list != null)
			{
				for (WeakReference<ITreeItem> ref : list)
				{
					if (ref != null)
					{
						ITreeItem diagRef = ref.get();
						
						if (diagRef != null)
							retVal.add(diagRef);
					}
				}
			}
		}
		
		return retVal;
	}
	
	/**
	 * Removes a node from its parent.  The parent of the nodes is determined
	 * by calling getParent on the passed in node.
	 *
	 * @param node The node to be removed.
	 */
	public void removeNodeFromParent(ITreeItem node)
	{
		ITreeItem parent = node.getParentItem();
		
		if (parent != null)
		{
			removeInstanceFromModel(node);
			parent.removeChild(node);
			
//                                // cvc - CR 6272973
//			// refresh the Diagrams root node
//			if (node instanceof ITreeDiagram)
//			{
//				UMLModelRootNode modelRootNode = getModelRootNode(parent);
//				UMLDiagramsRootNode diagramsNode =
//					mDiagramsNodeMap.get(
//						modelRootNode.getModelElement().getXMIID());
//
//				diagramsNode.recalculateChildren();
//			}
		}
	}
	
	public UMLModelRootNode getModelRootNode(ITreeItem node)
	{
//		if (node instanceof IProjectTreeItem)
//		{
//			IProjectTreeItem prjTreeItem = (IProjectTreeItem)node;
//			IProject project = prjTreeItem.getProject();
//			mNodeMap.get(project.getXMIID());
//		}
		
		if (node == null)
			return null;
		
		else if (node instanceof UMLModelRootNode)
			return (UMLModelRootNode)node;
		
		ITreeItem parent = node.getParentItem();
		
		if (parent == null)
			return null;
		
		else if (parent instanceof UMLModelRootNode)
			return (UMLModelRootNode)parent;
		
		else if (parent instanceof IProject)
		{
			ITreeItem treeItem = (ITreeItem)mNodeMap
				.get(((IProject)parent).getXMIID());
			
			if (treeItem instanceof UMLModelRootNode)
				return (UMLModelRootNode)treeItem;
		}
		
		return getModelRootNode(parent);
	}
	
	
	/**
	 * Remove all instances of the model element from the model.
	 */
	public void removeAll(IElement element)
	{
		ETList<ITreeItem> nodes = findNodes(element);
		
		if (nodes != null)
		{
			for (ITreeItem curNode : nodes)
			{
				removeNodeFromParent(curNode);
			}
		}
	}
	
	public void addModelRootNode(UMLModelRootNode node, Project project)
	{
		Lookup lookup = project.getLookup();
		
		// cvc - as per comments by MCF in UMLProject, the IProject is not
		//  added as a "lookup" anymore, but the ProjectHelper is and we can
		//  get the Project from that
		// IProject projectElement = (IProject)lookup.lookup(IProject.class);
		IProject projectElement = ((UMLProjectHelper)lookup
			.lookup(UMLProjectHelper.class)).getProject();
		
		if (projectElement != null)
		{
			// The project can only be displayed on the list once.  Therefore,
			// if the project is already present do not allow it to be added
			// again.
			ETList<WeakReference<ITreeItem>> list =
				mNodeMap.get(projectElement.getXMIID());
			
			if ((list == null) || (list.size() <= 0))
				addNode(projectElement.getXMIID(), node);
		}
	}
	
	////////////////////////////////////////////////////////////////////////////
	// Helper Methods
	////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Notify that listener that something tree item has been updated.
	 */
	protected void notifyContextOfAdd(IProjectTreeItem retVal)
	{
		if (mContext != null)
		{
			ProjectTreeNode node = new ProjectTreeNode(retVal);
			mContext.itemAdded(node);
		}
	}
	
	/**
	 * Create and a new tree item.
	 *
	 * @parem text The display text
	 * @param sortPriority The index into the sort algorithm
	 * @param element The associated model element
	 * @param supportedTreeItem Additional data to assoicated with the tree item.
	 * @param description A description of the tree node.
	 */
	protected IProjectTreeItem createProjectTreeItem(
		String text,
		long sortPriority,
		IElement element,
		Object supportTreeItem,
		String description)
	{
		// Debug.out.println("Creating a project tree item for " + text);
		
		IProjectTreeItem retVal = new ProjectTreeItemImpl();
		retVal.setItemText(text);
		retVal.setModelElement(element);
		retVal.setDescription(description);
		retVal.setSortPriority((int)sortPriority);
		
		if (supportTreeItem instanceof ITreeItem)
			retVal.setProjectTreeSupportTreeItem((ITreeItem)supportTreeItem);
		
		return retVal;
	}
	
	/**
	 * Searches the model for the node that visualizes the model tree item.
	 *
	 * @param element
	 * @param item
	 * @param items
	 */
	protected void findNodes(
		ITreeItem parentItem,
		Comparator<ITreeItem> comparator,
		ETList<ITreeItem> items)
	{
		if ((parentItem != null) && (items != null))
		{
			// if(parentItem.equals(element) == true)
			
			if(comparator.equals(parentItem) == true)
				items.add(parentItem);
			
			int childCnt = parentItem.getChildCount();
			
			for (int index = 0; index < childCnt; index++)
			{
				ITreeItem child = parentItem.getChild(index);
				findNodes(child, comparator, items);
			}
		}
	}
	

	/**
	 * Removes the reference to the node from node map in our model.
	 *
	 * @param node The node that needs to be removed.
	 */
	public void removeInstanceFromHash(ITreeItem node)
	{
		if (node != null)
		{       
			String key = getNodeKey(node);
			ETList<WeakReference<ITreeItem>> nodeList = mNodeMap.get(key);
			
			// cvc - 6272973
			// if the diagram is closed, then its name now has "_CLOSED"
			// appended to it but the mNodeMap has a key for the diagram
			// that doesn't have "_CLOSED". Also, try appending "_CLOSED"
			// if it wasn't ther already just to cover all the bases
			if (nodeList == null && node instanceof UMLDiagramNode)
			{
				if (key.endsWith("_CLOSED"))
					key = key.substring(0, key.indexOf("_CLOSED"));
//				
//				else
//					key = key+"_CLOSED";
				
				nodeList = mNodeMap.get(key);
			}
			
			if (nodeList != null)
			{
				// I need to remove all of the items from the tree that are the
				// same instance of the specified node.  I can not just call remove
				// becaue the remove will use the equals method to determine if
				// the list item is the same as the specified node.  Since all items
				// have the same key theorectially the equals method will say all
				// list items are the same.
				//
				// This time I am not interested in the logicaly equality, I am
				// interested if they are the same instances.
				
				for (int index = nodeList.size() - 1; index >= 0; index--)
				{
					WeakReference<ITreeItem> curNode = nodeList.get(index);
					
					if (curNode.get() == node)
						nodeList.remove(index);
				}
				
				if (nodeList.size() == 0)
					mNodeMap.remove(key);
			}
		} // if node !null
	}
	

	/**
	 * Removes the reference to the node in our model.
	 *
	 * @param node The node that needs to be removed.
	 */
	protected void removeInstanceFromModel(ITreeItem node)
	{
		if (node != null)
		{
		        removeInstanceFromHash(node);
			
			if (node instanceof ITreeDiagram)
			{
				ITreeDiagram diagramNode = (ITreeDiagram)node;
				IProxyDiagram diagram = diagramNode.getDiagram();
				
				if (diagram != null)
				{
					String filename = diagram.getFilename();
					mDiagramNodeMap.remove(filename);
					
					// cvc - start 6272973 fix
					// IProject project = diagram.getProject();
					// if(project != null)
					// {
					//     UMLDiagramsRootNode diagramsNode =
					//             mDiagramsNodeMap.get(project.getXMIID());
					//     if (diagramsNode != null)
					//         diagramsNode.removeChild(diagramNode);
					// }
					
					// cvc - 6272973
					// the above method for getting the Diagrams root node was
					// failing because the project was always null
					removeFromDiagramsRootNode(diagramNode);
				} // if diagram !null
			} // if node instanceof ITreeDiagram
		} // if node !null
	}
	
	private void removeFromDiagramsRootNode(ITreeItem treeItem)
	{
		UMLModelRootNode modelRootNode = getModelRootNode(treeItem);
		
		if (modelRootNode != null)
		{
			UMLDiagramsRootNode diagramsNode =
				mDiagramsNodeMap.get(
				modelRootNode.getModelElement().getXMIID());
			
			if (diagramsNode != null)
				diagramsNode.removeChild(treeItem);
		}
		
		else
		{
			Iterator it = mDiagramsNodeMap.keySet().iterator();
			
			while (it.hasNext())
			{
				UMLDiagramsRootNode diagramsNode =
					mDiagramsNodeMap.get(it.next());
				
				diagramsNode.removeChild((ITreeItem)treeItem);
			} // while
		} // else
	}
	
	/**
	 * Generates a key for the specified node.  If the node is associated with
	 * an IElement then the key will be the model elements id.
	 *
	 * @param node The node that needs a key.
	 * @return The generated key.
	 */
	protected String getNodeKey(ITreeItem node)
        {
            String retVal = "";
            IElement element = null;
            
            if (node != null)
            {
                if (node.getData() != null)
                {
                    element = node.getData().getModelElement();
                    
                    if (element != null)
                        retVal = element.getXMIID();
                    
                    else
                    {
                        if (node instanceof UMLDiagramNode)
                        {
                            UMLDiagramNode aNode = (UMLDiagramNode)node;
                            element = aNode.getModelElement();
                            if (element != null)
                            {
                                retVal = element.getXMIID();
                            }
                            else
                            {
                                IProxyDiagram proxyDiagram = aNode.getDiagram();
                                if ( proxyDiagram != null )
                                {
                                    retVal = proxyDiagram.getXMIID();
                                }
                                
                                if (retVal == null || retVal.length() == 0)
                                {
                                    String key = proxyDiagram.getFilename();
                                    retVal = mDiagramNodeMap.get(key);
                                }
                            }
                        }
                    }
                }
            }
            
            return ((retVal == null || retVal.length() == 0) ? 
                (node.getName() + '#' + node.getType()) : retVal);
        }
	
	/**
         * Add a new node to the models data structure.
         *
         * @param node The node to manage.
         */
        public void addNode(ITreeItem node)
        {
            if (node != null)
            {
                String key = getNodeKey(node);
                addNode(key, node);
            }
        }
	
	/**
	 * Add a new node to the models data structure.  The node will be
	 * associated with the specified key.
	 *
	 * @param node The node to manage.
	 * @param key The key to associate with the node.
	 */
	protected void addNode(String key, ITreeItem node)
        {
            if ((node != null) && (key != null))
            {
                if (node instanceof ITreeDiagram)
                {
                    ITreeDiagram diagramNode = (ITreeDiagram)node;
                    IProxyDiagram diagram = diagramNode.getDiagram();
                    
                    if (diagram != null)
                    {
                        String filename = diagram.getFilename();
                        mDiagramNodeMap.put(filename, key);
                        
                        // cvc - 6294480 (start)
                        // if Diagrams node is expanded before Model node, things
                        // get out of whack and diagram nodes don't get removed
                        // when deleted.
                        UMLModelRootNode modelRootNode = getModelRootNode(node);
                        
                        if (modelRootNode != null && mDiagramsNodeMap != null)
                        {
                            UMLDiagramsRootNode diagramsNode =
                                    mDiagramsNodeMap.get(
                                    modelRootNode.getModelElement().getXMIID());
                            
                            if (diagramsNode != null)
                            {
                                ITreeItem nodeCopy =
                                        mFactory.createDiagramNode(diagram);
                                
                                diagramsNode.removeChild(node);
                                diagramsNode.addChild(nodeCopy);
                            }
                        }
                        // cvc - 6294480 (end)
                    }
                }
                ETList<WeakReference<ITreeItem>> nodeList = mNodeMap.get(key);
                
                if(nodeList == null)
                {
                    nodeList = new ETArrayList<WeakReference<ITreeItem>>();
                    mNodeMap.put(key, nodeList);
                }
                nodeList.add(new WeakReference<ITreeItem>(node));
            }
        }
	
	public class DrawingAreaSink extends DrawingAreaEventsAdapter
        {
            public void onDrawingAreaPostCreated(
                    IDrawingAreaControl pDiagramControl,
                    IResultCell cell)
            {
                if (pDiagramControl != null)
                {
                    final IDrawingAreaControl control = pDiagramControl;
                    IDiagram diagram = control.getDiagram();
                    
                    if (diagram != null)
                    {
                        IProject project = diagram.getProject();
                        if (project != null)
                        {
                            UMLDiagramsRootNode diagramsNode =
                                    mDiagramsNodeMap.get(project.getXMIID());
                            
                            if (diagramsNode != null)
                            {
                                NBNodeFactory factory = new NBNodeFactory();
                                
                                IProxyDiagram proxy = ProxyDiagramManager
                                        .instance().getDiagram(diagram);
                                
                                ITreeDiagram node =
                                        factory.createDiagramNode(proxy);
                                
                                diagramsNode.addChild(node);
                            }
                        }
                    }
                }
            }
        }
        
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.scm.ISCMEventsSink#onPreFeatureExecuted(int, org.netbeans.modules.uml.core.scm.ISCMItemGroup, org.netbeans.modules.uml.core.scm.ISCMOptions, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
	public void onPreFeatureExecuted(
		int kind, ISCMItemGroup Group, ISCMOptions pOptions, IResultCell cell){}
	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.scm.ISCMEventsSink#onFeatureExecuted(int, org.netbeans.modules.uml.core.scm.ISCMItemGroup, org.netbeans.modules.uml.core.scm.ISCMOptions, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onFeatureExecuted(
		int kind, ISCMItemGroup group, ISCMOptions pOptions, IResultCell cell)
	{
		ETList<ITreeItem> treeElements = new ETArrayList<ITreeItem>();
		
		if (group != null)
		{
			for (int i=0; i < group.getCount(); i++)
			{
				ISCMItem item = group.item(i);
				
				if (item instanceof ISCMElementItem)
				{
					ISCMElementItem  elementItem = (ISCMElementItem)item;
					treeElements = findNodes(elementItem.getElement());
					Iterator<ITreeItem> iter = treeElements.iterator();
					
					while (iter.hasNext() == true)
					{
						ITreeItem treeElement = iter.next();
						treeElement.vcsFeatureExecuted(kind);
					}
				}
				
				if (item instanceof ISCMDiagramItem)
				{
					ISCMDiagramItem  diagramItem = (ISCMDiagramItem)item;
					treeElements = findDiagramNodes(diagramItem.getFileName());
					Iterator<ITreeItem> iter = treeElements.iterator();
					
					while (iter.hasNext() == true)
					{
						ITreeItem treeElement = iter.next();
						if (kind == SCMFeatureKind.FK_REMOVE_FROM_SOURCE_CONTROL)
						{
							ITreeItem parentItem = treeElement.getParentItem();
							parentItem.removeChild(treeElement);
						}
						
						else
							treeElement.vcsFeatureExecuted(kind);
					}
				}
			}
		}
	}
}
