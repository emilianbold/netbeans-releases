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

import org.dom4j.Node;

import org.netbeans.modules.uml.core.configstringframework.ConfigStringTranslator;
import org.netbeans.modules.uml.core.configstringframework.IConfigStringTranslator;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeElement;

/**
 *
 * @author Trey Spiva
 */
public class TreeElementNode extends ProjectTreeNode
   implements ITreeElement
{
   private String m_ExpandedElementType = "";
   private boolean m_bTranslateName = false;

   public TreeElementNode()
   {
      super();
   }

   /**
    * @param item
    * @throws NullPointerException
    */
   public TreeElementNode(IProjectTreeItem item) throws NullPointerException
   {
      super(item);
   }

   public void setElement(IElement element)
   {
      //m_ExpandedElementType = "";
      if(element != null)
      {
         m_ExpandedElementType = element.getExpandedElementType();         
      }
      
      if(getData() != null)
      {
         getData().setModelElement(element);
      }
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeElement#getElement()
    */
   public IElement getElement()
   {
      IElement retVal = null;
      
      if(getData() != null)
      {
         retVal = getData().getModelElement();
      }
      return retVal;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeElement#getXMIID()
    */
   public String getXMIID()
   {
      String retVal = null;
      
      if(getData() != null)
      {
         retVal = getData().getModelElementXMIID();
      }
      return retVal;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeElement#getElementType()
    */
   public String getElementType()
   {
      String retVal = null;
      
     if(getData() != null)
     {
        retVal = getData().getModelElementMetaType();
     }
     return retVal;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeElement#getExpandedElementType()
    */
   public String getExpandedElementType()
   {
      return m_ExpandedElementType;
   }
   
   /* (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   public boolean equals(Object obj)
   {
      boolean retVal = false;
   
      if(obj instanceof ITreeElement)
      {         
         IElement myElement = getData().getModelElement();
         ITreeElement objElement = (ITreeElement)obj;
         
         if((myElement != null) && (objElement.getElement() != null))
         {
            retVal = myElement.isSame(objElement.getElement());
         } 
         else if((myElement == null) && (objElement.getElement() == null))
         {
            retVal =  super.equals(obj);
            
            if(retVal == false)
            {
               retVal = getDisplayedName().equals(objElement.getDisplayedName());
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
   
   public String getDisplayedName()
   {
      String retVal = super.getDisplayedName();
      if(retVal.length() <= 0)
      {
         retVal = getElementType();
      }
      return retVal;
   }
   
   public String getType()
   {
	  String retVal = null;
      
	 if(getData() != null)
	 {
		retVal = getData().getModelElementMetaType();
	 }
	 return retVal;
   }
   public Node getXMLNode()
   {
   		Node pVal = null;
		IElement pElement = getElement();
		if (pElement != null)
		{
			pVal = pElement.getNode();
		}
   		return pVal;
   }
   
   public boolean getTranslateName()
   {
   		return m_bTranslateName;
   }
   
   public void setTranslateName(boolean val)
   {
   		m_bTranslateName = val;
   }
  
  	public String getDisplayName()
  	{
  		String pVal = "";
		if (!m_bTranslateName)
		{
		   pVal = getName();
		}
		else
		{
		   // Try translating the name first
		   IConfigStringTranslator pTranslator = new ConfigStringTranslator();
		   if (pTranslator != null)
		   {
			  pVal = pTranslator.translateWord(m_Name);
		   }
		}
  		return pVal;
  	}

}

