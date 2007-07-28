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
 * Created on Jun 11, 2003
 * @author Trey Spiva
 */
package org.netbeans.modules.uml.ui.controls.projecttree;

import javax.swing.Action;
import javax.swing.tree.MutableTreeNode;

import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.support.umlsupport.FileExtensions;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.ui.support.diagramsupport.IProxyDiagramManager;
import org.netbeans.modules.uml.ui.support.diagramsupport.ProxyDiagramManager;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementImport;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackageImport;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IProxyElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ProxyElement;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.Debug;

/**
 * 
 * @author Trey Spiva
 */
public class ProjectTreeItemImpl implements IProjectTreeItem, FileExtensions
{
   private ITreeItem[]   m_Path                 = null;
   private IProxyElement m_ModelElement         = null;
   //private IElement      m_ModelElement         = null;
   private String        m_ElementMetaType      = "";
   private Object        m_DataObject           = null;
   private String        m_Description          = "";
   private String        m_SecondaryDescription = "";
   private long          m_SortPriority         = 0L;
   private String        m_ItemText             = "";
   private ITreeItem     m_SupportItem          = null;
   private boolean 	 m_IsAddinNode 		= false;
   private Action[]      m_Actions              = null;
   
   //improve performance
   private IElement cachedElement = null;
   
   public ProjectTreeItemImpl()
   {     
   }
   
   public ProjectTreeItemImpl(ITreeItem[] path)
   {
      this();
      setPath(path);
   }

   public ProjectTreeItemImpl(ITreeItem[] path, IElement element)
   {
      this(path);
      setModelElement(element);
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem#setPath(IProjectTreeItem[])
    */
   public void setPath(ITreeItem[] path)
   {
      m_Path = path;
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem#getPath()
    */
   public ITreeItem[] getPath()
   {
      //return m_Path;
      ITreeItem[] retVal = m_Path;

      if((retVal == null) && (getProjectTreeSupportTreeItem() != null))
      {
         ITreeItem item = getProjectTreeSupportTreeItem();
         if(item instanceof ProjectTreeNode)
         {
            ProjectTreeNode node = (ProjectTreeNode)item;            
            Object[] parentPath = node.getPath();
            if(parentPath != null)
            {
               retVal = new ITreeItem[parentPath.length + 1];
               for (int index = 0; index < parentPath.length; index++)
               {
                  if(parentPath[index] instanceof ITreeItem)
                  {
                     retVal[index] = (ITreeItem)parentPath[index];
                  }
               }
               retVal[parentPath.length] = item;
            }
         }
      }

      return retVal;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem#setModelElement()
    */
   public void setModelElement(IElement element)
   {      
      m_ModelElement = null;
      if(element != null)
      {  
         m_ModelElement  = new ProxyElement();
         m_ModelElement.setElement(element);
         
         m_ElementMetaType = element.getElementType();
         
         // TODO: This is temporary.  I need to find a better solution.
         if(element instanceof INamedElement)
         {
            INamedElement namedE = (INamedElement)element;
            setItemText(namedE.getName());
         }
      }
      else
      {
         m_ElementMetaType = "";
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem#getModelElement()
    */
   public IElement getModelElement()
   {
      IElement retVal = null;
      if(m_ModelElement != null)
      {
         retVal = m_ModelElement.getElement();
      }
      return retVal;
	   
	   //to improve performance cache the element
//		if(cachedElement==null){
//			if(m_ModelElement != null)
//			{
//				cachedElement=m_ModelElement.getElement();
//			}
//		}
//		return cachedElement;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem#getModelElementXMIID()
    */
   public String getModelElementXMIID()
   {
      String retVal = "";
      
      if(m_ModelElement != null)
      {
         retVal = m_ModelElement.getElementID();
//         retVal = m_ModelElement.getXMIID();
      }
      
      return retVal;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem#getTopLevelXMIID()
    */
   public String getTopLevelXMIID()
   {
      String retVal = "";
      
      if(m_ModelElement != null)
      {
         retVal = m_ModelElement.getElementTopLevelID();
//         retVal = m_ModelElement.getTopLevelId();
      }

      return retVal;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem#getIsModelElement()
    */
   public boolean isModelElement()
   {
      return (m_ModelElement != null);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem#isSameModelElement(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
    */
   public boolean isSameModelElement(IElement pQueryItem)
   {
      boolean retVal = false;
      if(isModelElement() == true)
      {
         retVal = m_ModelElement.isSame(pQueryItem);
//         retVal = pQueryItem.isSame(m_ModelElement);
      }
      
      return retVal;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem#isImportedModelElement()
    */
   public boolean isImportedModelElement()
   {
      boolean retVal = false;
      
      
      
      return retVal;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem#getImportedModelElement()
    */
   public IElementImport getImportedModelElement()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem#isImportedPackage()
    */
   public boolean isImportedPackage()
   {
      // TODO Auto-generated method stub
      return false;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem#getImportedPackage()
    */
   public IPackageImport getImportedPackage()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem#getModelElementMetaType()
    */
   public String getModelElementMetaType()
   {
      return m_ElementMetaType;
   }

   public void setModelElementMetaType(String value)
   {
      m_ElementMetaType = value;
   }
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem#SetData(java.lang.Object)
    */
   public void setData(Object data)
   {
      m_DataObject = data;
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem#getElement()
    */
   public Object getData()
   {
      return m_DataObject;
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem#getDescription()
    */
   public void setDescription(String value)
   {
      if(value == null)
      {
         Debug.out.println("I am here");
      }
      m_Description = value;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem#getDescription()
    */
   public String getDescription()
   {
      return m_Description;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem#getSecondaryDescription()
    */
   public void setSecondaryDescription(String value)
   {
      m_SecondaryDescription = value;
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem#getSecondaryDescription()
    */
   public String getSecondaryDescription()
   {
      return m_SecondaryDescription;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem#isSame(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem)
    */
   public boolean isSame(IProjectTreeItem pQueryItem)
   {
      boolean retVal = false;
      
      if(pQueryItem != null)
      {
         Object[] thisPath  = getPath();
         Object[] queryPath = pQueryItem.getPath();
         
         if(thisPath.length == queryPath.length)
         {
            retVal = true;
            for (int i = 0; (i < queryPath.length) && (retVal != false); i++)
            {
               retVal = thisPath[i].equals(queryPath[i]);
            }
         }
      }
      
      return retVal;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem#getSortPriority()
    */
   public long getSortPriority()
   {
      return m_SortPriority;
   }

   /**
    * (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem#setSortPriority(int)
    */
   public void setSortPriority( long value )
   {
      m_SortPriority = value;
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem#isDiagram()
    */
   public boolean isDiagram()
   {
      boolean retVal = false;
      
      if(getDescription().length() > 0)
      {
         retVal = StringUtilities.hasExtension(getDescription(), DIAGRAM_LAYOUT_EXT);
      }
      
      return retVal;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem#getDiagram()
    */
   public IProxyDiagram getDiagram()
   {
      IProxyDiagram retVal = null;
      
      if(isDiagram() == true)
      {
         IProxyDiagramManager manager = ProxyDiagramManager.instance();
         retVal = manager.getDiagram(getDescription());
      }
      
      return retVal;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem#isProject()
    */
   public boolean isProject()
   {
      boolean retVal = false;
      if(getDescription() != null)
      {
         if(getDescription().equals(IProjectTreeControl.PROJECT_DESCRIPTION) == true)
         {
            retVal = true;
         }
      }
      return retVal;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem#getProject()
    */
   public IProject getProject()
   {
        IElement element = getModelElement();
      if (element != null)
          return element.getProject();
      else
      {
          IProxyDiagram dia = getDiagram();
          if (dia != null)
              return dia.getProject();
      }
      return null;
//      IProject retVal = null;
//      
//      
//     
//      
//      if(isProject() == true)
//      {
//         IElement element = getModelElement();
//         if(element instanceof IProject)
//         {
//            retVal = (IProject)element;
//         }
//      }
//      
//      
//      return retVal;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem#isWorkspace()
    */
   public boolean isWorkspace()
   {
      boolean retVal = false;
      if(getDescription().equals(IProjectTreeControl.WORKSPACE_DESCRIPTION) == true)
      {
         retVal = true;
      }
      return retVal;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem#getItemText()
    */
   public String getItemText()
   {      
      return m_ItemText;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem#setItemText(java.lang.String)
    */
   public void setItemText(String value)
   {
      m_ItemText = value;
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem#getProjectTreeSupportTreeItem()
    */
   public ITreeItem getProjectTreeSupportTreeItem()
   {      
      return m_SupportItem;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem#setProjectTreeSupportTreeItem(java.lang.Object)
    */
   public void setProjectTreeSupportTreeItem(ITreeItem value)
   {
      m_SupportItem = value;
   }

   public void setAsAddinNode(boolean val)
   {
      m_IsAddinNode = val;
   }
   
   public boolean isAddinNode()
   {
      return m_IsAddinNode;
   }
   
   public void setActions(Action[] actions)
   {
       m_Actions = actions;
   }
   
   public Action[] getActions()
   {
      return m_Actions;   
   }
   
   public IProxyElement getProxyElement()
   {
      return m_ModelElement;
   }
   
}