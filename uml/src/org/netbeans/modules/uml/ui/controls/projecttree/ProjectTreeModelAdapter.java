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

import java.util.ArrayList;
import java.util.Comparator;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem;

/**
 *
 * @author Trey Spiva
 */
public class ProjectTreeModelAdapter implements IProjectTreeModel
{
   /** The collection of project tree engines that are used by the model. */
      private ArrayList < IProjectTreeModelListener > m_Listeners = new ArrayList < IProjectTreeModelListener >();
  
   /**
    * 
    */
   public ProjectTreeModelAdapter()
   {
      super();
      // TODO Auto-generated constructor stub
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeModel#addProjectTreeModelListener(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeModelListener)
    */
   public void addProjectTreeModelListener(IProjectTreeModelListener listener)
   {
      m_Listeners.add(listener);

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeModel#removeProjectTreeModelListener(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeModelListener)
    */
   public void removeProjectTreeModelListener(IProjectTreeModelListener listener)
   {
      m_Listeners.remove(listener);

   }

   protected ArrayList < IProjectTreeModelListener > getListeners()
   {
      return m_Listeners;
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeModel#getChildItem(java.lang.Object, int)
    */
   public ITreeItem getChildItem(Object parent, int index)
   {
      return null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeModel#getChildCount(java.lang.Object)
    */
   public int getChildCount(Object parent)
   {
      return 0;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeModel#isLeaf(java.lang.Object)
    */
   public boolean isLeaf(Object node)
   {
      return false;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeModel#getWorkspace()
    */
   public IWorkspace getWorkspace()
   {
      return null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeModel#getRootItem()
    */
   public ITreeItem getRootItem()
   {
      return null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeModel#addItem(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem, java.lang.String, java.lang.String, long, org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, java.lang.Object, java.lang.String)
    */
   public IProjectTreeItem addItem(IProjectTreeItem parent, String name, String text, long sortPriority, IElement element, Object supportTreeItem, String description)
   {
      return null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeModel#addItem(org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem, java.lang.String, java.lang.String, long, org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, java.lang.Object, java.lang.String)
    */
   public IProjectTreeItem addItem(ITreeItem parent, String name, String text, long sortPriority, IElement element, Object supportTreeItem, String description)
   {
      return null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeModel#addItem(org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem, org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem)
    */
   public void addItem(ITreeItem parent, ITreeItem node)
   {
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeModel#removeAll(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
    */
   public void removeAll(IElement element)
   {
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeModel#removeNodeFromParent(org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem)
    */
   public void removeNodeFromParent(ITreeItem node)
   {
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeModel#projectOpened(org.netbeans.modules.uml.core.metamodel.structure.IProject)
    */
   public ITreeItem projectOpened(IProject pProject)
   {
      return null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeModel#clear()
    */
   public void clear()
   {
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeModel#getIndexOfChild(org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem, org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem)
    */
   public int getIndexOfChild(ITreeItem parent, ITreeItem child)
   {
      return 0;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeModel#sortChildren(org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem)
    */
   public void sortChildren(ITreeItem parent)
   {
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeModel#sortChildren(org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem, java.util.Comparator)
    */
   public void sortChildren(ITreeItem parent, Comparator compare)
   {
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeModel#findNodes(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
    */
   public ETList < ITreeItem > findNodes(IElement element)
   {      
      return null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeModel#findDiagramNodes(java.lang.String)
    */
   public ETList < ITreeItem > findDiagramNodes(String filename)
   {
      return null;  
   }

   public ETList < ITreeItem > findNodes(Comparator < ITreeItem > comparator )
   {
      return null;
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeModel#notifyOfStructureChange()
    */
   public void notifyOfStructureChange()
   {
      
   }
   
   public void notifyOfStructureChange(ETList < ITreeItem > items)
   {
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeModel#notifyOfRemovedChildren(org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem, int[], org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem[])
    */
   public void notifyOfRemovedChildren(ITreeItem parent, int[] childIndices, ITreeItem[] children)
   {
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeModel#notifyOfAddedChildren(org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem, int[])
    */
   public void notifyOfAddedChildren(ITreeItem parent, int[] childIndices)
   {
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeModel#notifyOfNodesChanged(org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem, int[], org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem[])
    */
   public void notifyOfNodesChanged(ITreeItem parent, int[] childIndices, ITreeItem[] nodes)
   {
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeModel#canDelete(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem)
    */
   public boolean canDelete(IProjectTreeItem item)
   {
      return false;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeModel#canEdit(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem)
    */
   public boolean canEdit(IProjectTreeItem item)
   {
      return false;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeModel#getNodeFactory()
    */
   public ProjectTreeNodeFactory getNodeFactory()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * Retreives the name of the model.
    */
   public String getModelName()
   {
      return "ProjectTree";
   }
   
   public void fireItemExpanding(ITreeItem item)
   {
       
   }

    public ITreeItem getDiagramsRootNode(IProject project)
    {
        return null;
    }

    public ITreeItem getModelRootNode(ITreeItem node)
    {
        return null;
    }
}
