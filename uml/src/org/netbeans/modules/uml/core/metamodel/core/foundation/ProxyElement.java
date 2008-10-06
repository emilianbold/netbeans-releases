/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * ProxyElement.java
 *
 * Created on May 4, 2004, 10:23 AM
 */

package org.netbeans.modules.uml.core.metamodel.core.foundation;

import org.netbeans.modules.uml.core.IApplication;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ElementLocator;
import org.netbeans.modules.uml.core.support.umlutils.IElementLocator;
import org.dom4j.Document;
import org.dom4j.Node;

/**
 *
 * @author  Trey Spiva
 */
public class ProxyElement implements IProxyElement
{
   /** The XMIID of the toplevelID for the above model element. */
   private String m_ModelElementTopLevelXMIID = "";
   
   /** The XMIID of the model element. */
   private String m_ModelElementXMIID = "";
   
   /** Creates a new instance of ProxyElement */
   public ProxyElement()
   {
   }
   
   /**
    * Returns the element this instance is assigned to 
    *
    * @return The element to add to our list
    */
   public IElement getElement()
   {
      IElement retVal = null;
      
      if((m_ModelElementXMIID.length() > 0) && 
         (m_ModelElementTopLevelXMIID.length() > 0))
      {
         IProject project = getProject();
         if(project != null)
         {
            IElementLocator locator = new ElementLocator();
            retVal = locator.findElementByID(project, m_ModelElementXMIID);
         }
      }
      
      return retVal;
   }
   
   /**
    * Returns the ID of the model element associated with the proxy.
    *
    * @return The elements ID.
    */
   public String getElementID()
   {
      return m_ModelElementXMIID;
   }
   
   /**
    * Returns the ID of the top level id associated with the proxy.
    *
    * @return The top level ID.
    */
   public String getElementTopLevelID()
   {
      return m_ModelElementTopLevelXMIID;
   }
   
   /**
    * Retrieves the name of the actual element, such as 'Class'. 
    *
    * @return The element type.
    */
   public String getElementType()
   {
      String retVal = "";
      
      if((m_ModelElementXMIID.length() > 0) && 
         (m_ModelElementTopLevelXMIID.length() > 0))
      {
         Document domDocument = null;
         
         IProject project = getProject();         
         if(project != null)
         {
            Node documentNode = project.getNode();
            if(documentNode != null)
            {
               domDocument = documentNode.getDocument();
            }
         }
         
         if(domDocument != null)
         {
            org.dom4j.Element tempNode = domDocument.elementByID(m_ModelElementXMIID);
            if(tempNode != null)
            {
               retVal = XMLManip.retrieveSimpleName(tempNode);
            }
         }
      }
      
      return retVal;
   }
   
   public boolean isSame(IElement pElement)
   {
      boolean retVal = false;
      
      if(pElement != null)
      {
         if((m_ModelElementTopLevelXMIID.length() > 0) && 
            (m_ModelElementXMIID.length() > 0) && 
            (m_ModelElementTopLevelXMIID.equals(pElement.getTopLevelId()) == true) && 
            (m_ModelElementXMIID.equals(pElement.getXMIID()) ))
         {
            retVal = true;
         }
      }
      
      return retVal;
   }
   
   /**
    * Determines whether or not this element encapsulates the same data as the 
    * passed in element. 
    *
    * @param pElement The element see if it's the same as this element
    * @return <code>true</code> if the pElement is the same as this element.
    */
   public boolean isSame(IProxyElement pElement)
   {
      boolean retVal = false;
      
      if(pElement != null)
      {
         if((m_ModelElementTopLevelXMIID.length() > 0) && 
            (m_ModelElementXMIID.length() > 0) && 
            (m_ModelElementTopLevelXMIID.equals(pElement.getElementTopLevelID()) == true) && 
            (m_ModelElementXMIID.equals(pElement.getElementID()) == true))
         {
            retVal = true;
         }
      }
      
      return retVal;
   }
   
   /**
    * Determines whether or not this element encapsulates the same data as the 
    * passed in element. 
    *
    * @param sElementXMIID [ The element's xmiid see if it's the same as this element
    * @return <code>true</code> if the pElement is the same as this element.
    */
   public boolean isSame(String sElementXMIID)
   {
      boolean retVal = false;
      
      if(sElementXMIID.length() > 0)
      {
         if(m_ModelElementXMIID.equals(sElementXMIID) == true )
         {
            retVal = true;
         }
      }
      
      return retVal;
   }
   
   /**
    * Sets the element this instance is assigned to 
    *
    * @param pElement The element to add to our list
    */
   public void setElement(IElement pElement)
   {
      m_ModelElementTopLevelXMIID = "";
      m_ModelElementXMIID = "";
      
      if(pElement != null)
      {
         m_ModelElementTopLevelXMIID = pElement.getTopLevelId();
         if(m_ModelElementTopLevelXMIID == null)
         {
             m_ModelElementTopLevelXMIID = "";
         }
         
         m_ModelElementXMIID = pElement.getXMIID();
         if(m_ModelElementXMIID == null)
         {
             m_ModelElementXMIID = "";
         }
      }
   }
   
   /**
    * Sets the element this instance is assigned to 
    *
    * @param sTopLevelXMIID The element's toplevel xmiid
    * @param sElementXMIID The element's xmiid
    */
   public void setElement(String sTopLevelXMIID, String sElementXMIID)
   {
      m_ModelElementTopLevelXMIID = "";
      m_ModelElementXMIID = "";
      
      if((sTopLevelXMIID.length() > 0) && (sElementXMIID.length() > 0))
      {
         m_ModelElementTopLevelXMIID = sTopLevelXMIID;
         m_ModelElementXMIID = sElementXMIID;
      }
   }
   
   /**
    * Retrieves the project assoicated with the proxy.  The project associated
    * with the proxy is specified by the Top Level id.
    *
    * @return The project.
    */
   protected IProject getProject()
   {
      IProject retVal = null;
      
      if(m_ModelElementTopLevelXMIID.length() > 0)
      {
         ICoreProduct product = ProductRetriever.retrieveProduct();
         if(product != null)
         {
            IApplication app = product.getApplication();
            if(app != null)
            {
               retVal = app.getProjectByID(m_ModelElementTopLevelXMIID);
            }
         }
      }
      
      return retVal;
   }
   
}
