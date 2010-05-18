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


/*
 *
 * Created on Jul 3, 2003
 * @author Trey Spiva
 */
package org.netbeans.modules.uml.ui.swing.projecttree;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * This class preserves the state of the tree when a refresh is done.  As much as
 * possible we try to restore the tree back to it's original state.
 *
 * <b>Note:</b> It would be <i>very</i> bad if multiple perservations of the
 * same tree to be distroy the previous preservation.  So the owther preservation
 * will take precidence.  PreserveTreeState must be a member of the tree control
 * order to perserve multiple states correctly.
 * 
 * @author Trey Spiva
 */
public class PreserveTreeState
{
   private JTree      m_ProjectTree   = null;
   private int        m_NumInstances  = 0;
   private ArrayList  m_ExpandNodes   = new ArrayList();
   private TreePath[] m_SelectedItems = null; 
   
   public PreserveTreeState(JTree tree)
   {
      setProjectTree(tree);
   }
   
   public void perserveState()
   {
      increment();  
      
      if((isAbleToPerserve() == true) && (getProjectTree() != null))
      { 
         int rowCount = getProjectTree().getRowCount();
         for(int index = 0; index < rowCount; index++)
         {
            addToExpandedNodes(index);            
         }
         m_SelectedItems = getProjectTree().getSelectionPaths();
      }
   }

   /**
    * @param curNode
    */
   protected void addToExpandedNodes(int curNode)
   {
      if(getProjectTree().isExpanded(curNode) == true)
      {
         m_ExpandNodes.add(getProjectTree().getPathForRow(curNode));
      }
      
   }

   public void restoreState()
   {  
      if((isAbleToPerserve() == true) && (getProjectTree() != null))
      { 
         expand();
      }
      
      decrement();  
   }
   
   /**
    * Goes through te m_ExpandedNodes and finds the same item in the new tree.  
    * When found the item is expanded.
    */
   protected void expand()
   {
      if(isAbleToPerserve() == true)
      {
         for (Iterator iter = m_ExpandNodes.iterator(); iter.hasNext();)
         {
            TreePath path = (TreePath)iter.next();
            expandThisItem(path);  
         }
      }
   }

   /**
    * Expands a specific tree item.
    * 
    * @param path The path to the item to expand.
    */
   protected void expandThisItem(TreePath path)
   {
		TreeModel model = getProjectTree().getModel();
		Object[] pathList = path.getPath();
      
		Object foundObject = null;
		TreePath parentPath = null;
		for (int index = 0; index < pathList.length; index++)
		{
			try
			{
			   foundObject = findChildItem(foundObject, pathList[index], model);
			   if(foundObject == null)
			   {
				  break;         
			   }
	         
			   if(parentPath == null)
			   {
				  parentPath = new TreePath(foundObject);
			   }
			   else
			   {
				  parentPath = parentPath.pathByAddingChild(foundObject);
			   }
			   getProjectTree().expandPath(parentPath);
			}
		   catch(Exception e)
		   {
		   }
		}
   }

   /**
    * @param foundObject
    * @param object
    * @param model
    * @return
    */
   protected Object findChildItem(Object parent, Object toFind, TreeModel model)
   {
      Object retVal = null;
      
      if(parent != null)
      {
         int childCount = model.getChildCount(parent);
         for(int index = 0; (index < childCount) && (retVal == null); index++)
         {
            Object curObject = model.getChild(parent, index);
            if(toFind.equals(curObject) == true)
            {
               retVal = curObject;
            }
         }
      }
      else
      {
         // check if the object to find is the root object. 
         if(toFind.equals(model.getRoot()) == true)
         {
            retVal = model.getRoot();
         }
      }
      
      return retVal;
   }

   /**
    * Retrieves the tree control that has its state perserved.
    * 
    * @return The tree control.
    */
   public JTree getProjectTree()
   {
      return m_ProjectTree;
   }

   /**
    * Sets the tree control that is to have its state perserved.
    * 
    * @param tree The tree control.
    */
   public void setProjectTree(JTree tree)
   {
      m_ProjectTree = tree;
   }

   /**
    * Retreives the number of instances of the tree perserver.  
    * @return
    */
   public boolean isAbleToPerserve()
   {
      return m_NumInstances == 1;
   }

   /**
    * @param i
    */
   public void increment()
   {
      m_NumInstances++;
   }

   /**
    * @param i
    */
   public void decrement()
   {
      m_NumInstances--;
   }
}
