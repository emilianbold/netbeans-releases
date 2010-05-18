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
