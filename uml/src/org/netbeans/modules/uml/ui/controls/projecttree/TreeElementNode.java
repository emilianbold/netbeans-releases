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

