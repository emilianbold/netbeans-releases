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


package org.netbeans.modules.uml.project.ui.nodes;

import java.util.TreeSet;

import org.openide.nodes.Children;
import org.openide.nodes.Node;

import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ProjectTreeComparable;
import org.netbeans.modules.uml.project.ui.ITreeItemExpandContext;
import org.netbeans.modules.uml.project.ui.NetBeansUMLProjectTreeModel;
// import org.netbeans.modules.uml.core.support.Debug;

/**
 *
 * @author Trey Spiva
 */
public class UMLChildren extends Children.Array
{
   private ITreeItem mItem = null;

   public UMLChildren()
   {
       super(new TreeSet());
   }
   
   /**
    * 
    */
   public UMLChildren(ITreeItem item)
   {
      this();

      setItem(item);
      //setComparator(new ProjectTreeComparable());
   }
    

   public boolean add(Node[] arr)
   {
       boolean retVal = super.add(arr);
//       refresh();
       
       return retVal;
   }


   /* (non-Javadoc)
    * @see org.openide.nodes.Children#addNotify()
    */
   protected void addNotify()
   {
      super.addNotify();
      
      sendNodeExpandEvent();
   }

   /* (non-Javadoc)s
    * @see org.openide.nodes.Children#removeNotify()
    */
   protected void removeNotify()
   {
      super.removeNotify();
   }
   
   public boolean areChildrenInitialized()
   {
      return isInitialized();
   }
   
   //**************************************************
   // Data Access Methods
   //**************************************************
   
   /**
    * Retrieves the project tree item used to describe the model element that 
    * contains the children.
    * 
    * @return The project tree item.
    */
   public ITreeItem getItem()
   {
      return mItem;
   }

   /**
    * Sets  the project tree item used to describe the model element that 
    * contains the children.
    * 
    * @param item The project tree item.
    */
   public void setItem(ITreeItem item)
   {
      mItem = item;
   }
   
   //**************************************************
   // Helper Methods
   //**************************************************
   
   /**
    * Notify listeners that a node is being expanded.  Listeners are able to add
    * nodes to the ProjectTreeItem.
    */
   protected void sendNodeExpandEvent()
   {
      final NetBeansUMLProjectTreeModel model = UMLModelRootNode.getProjectTreeModel(); 
      final ITreeItem item = getItem();
      
      // Debug.out.println("UMLChildren Firing node expand event");
      if((model != null) && (item != null))  
      {
         model.fireItemExpanding(item, new ChildrenNodeContext());    
         
         item.setIsInitalized(true);
      }      
   }
   

  /**
    * Test to see if we can redraw the children list on demand, would
   * help with the filter action impl if it works
    */
   public void recalculateChildren()
   {
      final NetBeansUMLProjectTreeModel model = UMLModelRootNode.getProjectTreeModel(); 
      final ITreeItem item = getItem();
      item.setIsInitalized(false);
      
      // Debug.out.println("UMLChildren.recalculateChildren");
      if((model != null) && (item != null))  
      {
         // MCF - we have to remove the existing nodes at some point,
         // otherwise new nodes are added in addition to the prior ones
         // Not sure if this is the most efficient thing to do or if this
         // is the right place to do it.
	 MUTEX.readAccess(new Runnable() 
	 {
	     public void run() 
	     {
		 if (nodes != null) 
		 {
		     remove(getNodes());
		     refresh();
		 }
	     }
	 });  
          
	 MUTEX.readAccess(new Runnable() 
	 {
	     public void run() 
	     {
		 model.fireItemExpanding(item, new ChildrenNodeContext());  
	     }
	 });  
         
         item.setIsInitalized(true);
      }      
   }

 
   /** 
    *  update the nodemap hash in the model to avoid "ghost" nodes in the hash
    */
   public boolean remove(Node[] nodes)
   {       
       NetBeansUMLProjectTreeModel model = UMLModelRootNode.getProjectTreeModel(); 
       if (model != null) 
       { 
	   for (int i = 0; i < nodes.length; i++) 
	   {
	       if (nodes[i] instanceof ITreeItem) 
	       {
		   model.removeInstanceFromHash((ITreeItem)nodes[i]);
	       }
	   }
       }
       return super.remove(nodes);
   }
   

   public class ChildrenNodeContext implements ITreeItemExpandContext
   {

      /* (non-Javadoc)
       * @see org.netbeans.modules.uml.netbeans.umlproject.ui.IProjectTreeExpandContext#itemAdded(org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem)
       */
      public void itemAdded(ITreeItem item)
      {
         Node[] nodes = {new UMLElementNode(item)};
         boolean isAdded = add(nodes);
      }
      
      public String toString()
      {
         return mItem.getDisplayedName();
      }
   }

}
