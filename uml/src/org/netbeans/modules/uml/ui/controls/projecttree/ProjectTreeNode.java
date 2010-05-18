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
 * Created on Jun 11, 2003
 * @author Trey Spiva
 */
package org.netbeans.modules.uml.ui.controls.projecttree;

import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeElement;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeRelElement;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ProjectTreeComparable;

/**
 * 
 * @author Trey Spiva
 */
public class ProjectTreeNode extends DefaultMutableTreeNode implements ITreeItem
{
   private IProjectTreeItem m_DataItem     = null;
   private boolean          m_IsInitalized = false;
   protected String           m_Name         = "";
   protected String				m_PathAsString = "";
   protected ITreeItem			m_ParentTreeItem = null;
   
   
   /**
    * 
    */
   public ProjectTreeNode()
   {   
      this(new ProjectTreeItemImpl());      
   }
   
   /**
    * Initalizes the Project Tree Node.
    * 
    * @param item The project tree item data.
    * @throws NullPointerException Thrown if item is set to null.
    */
   public ProjectTreeNode(IProjectTreeItem item)
      throws NullPointerException
   {
      if(item == null)
      {
         throw new NullPointerException();
      }
      
      setDataItem(item);
      setIsInitalized(false);
   }  
   
   /**
    * @return
    */
   public IProjectTreeItem getDataItem()
   {
      return m_DataItem;
   }

   /**
    * @param item
    */
   public void setDataItem(IProjectTreeItem item)
   {
      m_DataItem = item;
      m_DataItem.setProjectTreeSupportTreeItem(this);
   } 

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem#setName(java.lang.String)
    */
   public void setName(String value)
   {   
      m_Name = value;   
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem#getName()
    */
   public String getName()
   {
      return m_Name;
   }

   public String getDisplayedName()
   {
      String retVal = "";
      if(getDataItem() != null)
      {
         retVal = getDataItem().getItemText();
      }

      if((retVal == null) || (retVal.length() <= 0))
      {
         retVal = getName();
      }
      return retVal;
   }

   public void setDisplayedName(String value)
   {
      if(getDataItem() != null)
      {
         getDataItem().setItemText(value);
      }
   }
   
   public void setDisplayedName(String value, boolean buildProperties)
   {
      setDisplayedName(value);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem#isSame(org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem)
    */
   public boolean isSame(ITreeItem queryItem)
   {
      return equals(queryItem);
   }



   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem#getTopParent()
    */
   public ITreeItem getTopParentItem()
   {
   		ITreeItem pTop = null;
   		ITreeItem pItem = getParentItem();
   		while (pItem != null)
   		{
   			pTop = pItem;
   			ITreeItem pTemp = pTop.getParentItem();
   			pItem = pTemp;
   		}
      	return pTop;
   }



   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem#getOwningTreeElement()
    */
   public ITreeElement getOwningTreeElement()
   {
   		ITreeElement pOutEle = null;
   		ITreeItem pLast = null;
		ITreeItem pItem = getParentItem();
		while (pItem != null)
		{
			pLast = pItem;
			if (pItem instanceof ITreeRelElement)
			{
				ITreeItem pTemp = pLast.getParentItem();
				pItem = pTemp;
			}
			else if (pItem instanceof ITreeElement)
			{
				pItem = null;
			}
			else
			{
				ITreeItem pTemp = pLast.getParentItem();
				pItem = pTemp;
			}
		}
		if (pLast != null)
		{
			if (pLast instanceof ITreeElement)
			{
				pOutEle = (ITreeElement)pLast;
			}
		}
   		return pOutEle;
   }



   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem#getType()
    */
   public String getType()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem#getParentItem()
    */
   public ITreeItem getParentItem()
   {
   	/*
      ITreeItem retVal = null;
      
      if (getParent() instanceof ITreeItem)
      {
         retVal = (ITreeItem)getParent();
      }
      */
      
      return m_ParentTreeItem;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem#setParentItem(org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem)
    */
   public void setParentItem(ITreeItem parent)
   {
   		m_ParentTreeItem = parent;
   		
      DefaultMutableTreeNode mutableNode = (DefaultMutableTreeNode)parent;
      setParent(mutableNode);
      
      if (parent instanceof MutableTreeNode)
      {  
         // Now we now all the information need to set the path of the 
         // tree item.  
         if(getData() != null)
         {
            Object[] parentPath = mutableNode.getPath();
            if(parentPath != null)
            {
               ITreeItem[] myPath = new ITreeItem[parentPath.length + 1];
               for (int index = 0; index < parentPath.length; index++)
               {
                  if(parentPath[index] instanceof ITreeItem)
                  {
                     myPath[index] = (ITreeItem)parentPath[index];
                  }
               }
               myPath[parentPath.length] = this;
               getData().setPath(myPath);
            }
         }
      }
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem#addChild(org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem)
    */
   public void addChild(ITreeItem item)
   {     
      add((MutableTreeNode) item);
   }

   /**
    * Adds a new child to the node at the specified location in the child list.
    * If the specified index is greater than the number of children then it
    * is appened to the end of the child list.
    * 
    * @param item The new node to add.
    * @param index The index to insert the node into.
    */
   public void insertAt(ITreeItem item, int index)
   {
      insert((MutableTreeNode)item, index);
   }
   
   /**
    * Retrieve a collection that contains all of the children contained by the
    * node.  
    * <br>
    * <i>Note:</i> This collection should be treated as <b>read-only</b>.  If a
    * node is added to the returned collection any associated view will not be
    * updated until a refresh is performed.
    * 
    * @return The collection of children.
    */
   public Enumeration < ITreeItem > getNodeChildren()
   {
      return children();
   }
      
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem#removeChild(org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem)
    */
   public void removeChild(ITreeItem item)
   {
      if(item instanceof MutableTreeNode)  
      {
         MutableTreeNode node = (MutableTreeNode)item;
         if(isNodeChild(node) == true)
         {
            this.remove((MutableTreeNode) item);
         }
      }      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem#getChild(int)
    */
   public ITreeItem getChild(int index)
   {
      ITreeItem retVal = null;
      if(index < getChildCount())
      {
         retVal = (ITreeItem)getChildAt(index);
      }
      else
      {
         throw new ArrayIndexOutOfBoundsException();
      }
      return retVal;
   }

   /**
    * Sorts the children of the tree item.  The children will be sorted occuring
    * to the default sort order.
    */
   public void sortChildren()
   {
      sortChildren(new ProjectTreeComparable());
   }

   /**
    * Sorts the children of the tree item.  The children will be sorted occuring
    * to the Comparable interface.
    * 
    * @param parent The parent who children are to be sorted.
    * @param compare The comparable interface used to sort the children.
    * @see Comparable
    */
   public void sortChildren(Comparator compare)
   {
   	if (children != null)
   	{
			Collections.sort(children, compare);
   	}
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem#getData()
    */
   public IProjectTreeItem getData()
   {
      return getDataItem();
   }
   
   /**
    * Specifies whether or not the node has ever been expanded.  If the
    * node has been expanded before then its children has already been
    * set.  Otherwise, the node has not been completely initialized yet.
    * 
    * @return <b>true</b> if the node has been initalized, <b>false</b>
    *         if the node has not been initialized.
    * @hidden
    */
   public boolean isInitalized()
   {
      return m_IsInitalized;
   }

   /**
    * Specifies whether or not the node has ever been expanded.  If the
    * node has been expanded before then its children has already been
    * set.  Otherwise, the node has not been completely initialized yet.
    * 
    * @param value <b>true</b> if the node has been initalized, <b>false</b>
    *              if the node has not been initialized.
    * @hidden
    */
   public void setIsInitalized(boolean value)
   {
      m_IsInitalized = value;
   }

   /* (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   public boolean equals(Object obj)
   {
      boolean retVal = false;
      
      IProjectTreeItem myItem = getData();
      
      if(obj instanceof IElement)
      {
         IElement myElement = myItem.getModelElement();
         if(myElement != null)
         {
            retVal = myElement.isSame((IElement)obj);
         } 
      }
      else if (obj instanceof ProjectTreeNode)
      {
         ProjectTreeNode node = (ProjectTreeNode)obj;
         
         IProjectTreeItem testItem = node.getData();                        
                     
         String testText = node.getDisplayedName();
         String myText   = getDisplayedName();

         if((testText != null) && (myText != null))
         {
            retVal = testText.equals(myText); 
         } 
         else if((testText == null) && (myText == null))
         {
            retVal =  super.equals(obj);
         }
         
         if(retVal == true)
         {
            String testXMIID = testItem.getModelElementXMIID();
            String myXMIID = myItem.getModelElementXMIID();
            if((testXMIID != null) && 
               (myXMIID != null) &&
               (testXMIID.length() > 0) &&
               (myXMIID.length() > 0))
            {
               retVal = testXMIID.equals(myXMIID);
            }
            else
            {
               retVal = false;
               String testDesc = testItem.getDescription();
               String myDesc = myItem.getDescription();
               if((testDesc != null) && 
                  (myDesc != null) &&
                  (testDesc.length() > 0) &&
                  (myDesc.length() > 0))
               {
                  retVal = testDesc.equals(myDesc);
               }
            }
         }
      }      
      else
      {  // Generic equals method.  This can be used to test
         // to ITreeItem(s).
         retVal =  super.equals(obj);
      }
      
      return retVal;
   }

   public int hashCode()
   {
      int retVal = super.hashCode();
      
      IProjectTreeItem data = getData();
      if(data.getModelElementXMIID().length() > 0)
      {
         String xmlId = data.getModelElementXMIID();
         retVal = xmlId.hashCode();
      }
      else if(getDisplayedName() != null)
      {
         String value = getDisplayedName();
         retVal = value.hashCode();
      }
      
      return retVal;
   }
   
   public long getSortPriority()
   {
      return m_DataItem.getSortPriority();
   }
   
   public void setSortPriority(long value)
   {
      m_DataItem.setSortPriority(value);
   }
   
   public void setExpanded(boolean value)
   {
   		//setExpanded(value);
   }
   public void setSelected(boolean value)
   {
		//setSelected(value);
   }
   
   public String getPathAsString()
   {
		return m_PathAsString;
   }
   public void setPathAsString(String value)
   {
		m_PathAsString = value;
   }   
   
   public String toString()
   {
      String retVal = getDisplayedName();
      
      if((retVal == null) || (retVal.length() <= 0))
      {
         retVal = getName();
      }
      
      return retVal;
   }
   public void vcsFeatureExecuted(/* SCMFeatureKind */ int kind)
   {
   
   }
}
