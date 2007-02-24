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
 * Created on Jun 13, 2003
 * @author Trey Spiva
 */
package org.netbeans.modules.uml.ui.controls.projecttree;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeFolder;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem;

/**
 *
 * @author Trey Spiva
 */
public class ProjectTreeFolderNode extends ProjectTreeNode
   implements ITreeFolder
{
   private String m_GetMethodName  = "";
   private String m_Name           = "";
   private String m_ID             = "";
   
   public ProjectTreeFolderNode()
   {
      this(new ProjectTreeItemImpl());
   }
   
   /**
    * @param item
    */
   public ProjectTreeFolderNode(IProjectTreeItem item)
   {
      super(item);
   }

   public void setID(String id)
   {
      m_ID = id;
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeFolder#getID()
    */
   public String getID()
   {
      return m_ID;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeFolder#setName(java.lang.String)
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
      
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeFolder#setDisplayName(java.lang.String)
    */
   public void setDisplayName(String value)
   {
      IProjectTreeItem data = getData();
      if(data != null)
      {
         data.setItemText(value);
      }
   }
   
   public void setDisplayName(String value, boolean buildProperties)
   {
      setDisplayName(value);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeFolder#getDisplayName()
    */
   public String getDisplayName()
   {
      String retVal = getName();
      
      IProjectTreeItem data = getData();
      if(data != null)
      {
         retVal = data.getItemText();
      }
      return retVal;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeFolder#setGetMethod(java.lang.String)
    */
   public void setGetMethod(String name)
   {
      m_GetMethodName = name;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeFolder#getGetMethod()
    */
   public String getGetMethod()
   {
      return m_GetMethodName;
   }

   /**
    * Sets the element that this tree folder represents.
    *
    * @param newVal The element that this folder represents
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeFolder#setElement(com.embarcadero.describe.foundation.IElement)
    */
   public void setElement(IElement newVal)
   {
      IProjectTreeItem data = getData();
      if(data != null)
      {
         data.setModelElement(newVal); 
         data.setModelElementMetaType("");
      }
   }

   /**
    * Returns the element that this tree folder represents.
    *
    * @return The element that this folder represents, or NULL
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeFolder#getElement()
    */
   public IElement getElement()
   {
      IElement retVal = null;
      
      IProjectTreeItem data = getData();
      if(data != null)
      {
         retVal = data.getModelElement();
      }
      
      return retVal;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeFolder#setPath(java.lang.String)
    */
   public void setPath(ITreeItem[] defPath)
   {
      IProjectTreeItem data = getData();
      if(data != null)
      {
         data.setPath(defPath);
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeFolder#setIsGetMethodAnImport(boolean)
    */
   public void setIsGetMethodAnImport(boolean value)
   {
      

   }

   /**
    * Is the get method the one used to get imported packages or elements?
    *
    * retVal <b>true</b> if the get method is one used to get package or element
    *        imports.
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeFolder#isGetMethodAnImport()
    */
   public boolean isGetMethodAnImport()
   {
      boolean retVal = false;
      if((getGetMethod().equals("ImportedElements") == true) || 
         (getGetMethod().equals("ImportedPackages") == true))
      {
         retVal = true;
      }
      
      return retVal;
   }
   
   /**
    * Is the get method the one used to get imported elements?
    * 
    * @return <b>true</b> if the get method is an import, <b>false</b>
    *         if the get method is not an import.
    */
   public boolean isGetMethodAnImportElement()
   {
      boolean retVal = false;
      if(getGetMethod().equals("ImportedElements") == true) 
      {
         retVal = true;
      }

      return retVal;
   }

   /**
    * Is the get method the one used to get imported packages?
    * 
    * @return <b>true</b> if the get method is an import, <b>false</b>
    *         if the get method is not an import.
    */
   public boolean isGetMethodAnImportPackage()
   {
      boolean retVal = false;
     if(getGetMethod().equals("ImportedPackages") == true)
     {
        retVal = true;
     }

     return retVal;
   }
   
   public String getType()
   {
   		return getName();
   }
   /* (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   public boolean equals(Object obj)
   {
      boolean retVal = false;
      
      if (obj instanceof ITreeFolder)
      {
         ITreeFolder folder = (ITreeFolder)obj;
         
         if((getDisplayName() != null) && (folder.getDisplayName() != null))
         {
            if(getDisplayName().equals(folder.getDisplayName()) == true)
            {
               if((getID() == null) && (folder.getID() == null))
               {
                  retVal = true;
               }
               else if((getID() != null) && (folder.getID() != null))
               {
                  retVal = getID().equals(folder.getID());
               }
            }
         }
      }
      
      return retVal;
   }

}
