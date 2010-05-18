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

package org.netbeans.modules.uml.requirements.xmlprovider;

import org.dom4j.Document;
import org.netbeans.modules.uml.core.requirementsframework.IRequirement;
import org.netbeans.modules.uml.core.requirementsframework.IRequirementSource;
import org.netbeans.modules.uml.core.requirementsframework.ISatisfier;
import org.netbeans.modules.uml.core.requirementsframework.RequirementUtility;
import org.netbeans.modules.uml.core.support.umlsupport.IComparableTreeData;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.dom4j.Element;

/**
 * @author josephg
 *
 */
public class ETRequirement implements IRequirement
{
   private ETList<ISatisfier> m_Satisfiers = new ETArrayList<ISatisfier>();
   private ETList<IRequirement> m_Requirements;
   private boolean m_IsCategory;
   
   private String m_Name;
   private String m_Id;
   private String m_Description;
   private String m_Type;
   private String m_ModName;
   private String m_ProjectName;
   private String m_ProviderID;
   private String m_SourceID;
   
   /**
    * @return
    */
   public String getDescription()
   {
      return m_Description;
   }

   /**
    * @return
    */
   public String getID()
   {
      return m_Id;
   }

   /**
    * @return
    */
   public boolean isCategory()
   {
      return m_IsCategory;
   }

   /**
    * @return
    */
   public String getModName()
   {
      return m_ModName;
   }

   /**
    * @return
    */
   public String getName()
   {
      return m_Name;
   }

   /**
    * @return
    */
   public String getProjectName()
   {
      return m_ProjectName;
   }

   /**
    * @return
    */
   public String getProviderID()
   {
      return m_ProviderID;
   }

   /**
    * @return
    */
   public ETList<IRequirement> getRequirements()
   {
      return m_Requirements;
   }

   /**
    * @return
    */
   public ETList<ISatisfier> getSatisfiers()
   {
      return m_Satisfiers;
   }

   /**
    * @return
    */
   public String getSourceID()
   {
      return m_SourceID;
   }

   /**
    * @return
    */
   public String getType()
   {
      return m_Type;
   }

   /**
    * @param string
    */
   public void setDescription(String string)
   {
      m_Description = string;
   }

   /**
    * @param string
    */
   public void setID(String string)
   {
      m_Id = string;
   }

   /**
    * @param b
    */
   public void setIsCategory(boolean b)
   {
      m_IsCategory = b;
   }

   /**
    * @param string
    */
   public void setModName(String string)
   {
      m_ModName = string;
   }

   /**
    * @param string
    */
   public void setName(String string)
   {
      m_Name = string;
   }

   /**
    * @param string
    */
   public void setProjectName(String string)
   {
      m_ProjectName = string;
   }

   /**
    * @param string
    */
   public void setProviderID(String string)
   {
      m_ProviderID = string;
   }

   /**
    * @param requirements
    */
   public void setRequirements(ETList<IRequirement> requirements)
   {
      m_Requirements = requirements;
   }

   /**
    * @param satisfiers
    */
   public void setSatisfiers(ETList<ISatisfier> satisfiers)
   {
      m_Satisfiers = satisfiers;
   }

   /**
    * @param string
    */
   public void setSourceID(String string)
   {
      m_SourceID = string;
   }

   /**
    * @param string
    */
   public void setType(String string)
   {
      m_Type = string;
   }

   public boolean isSame(IComparableTreeData otherData)
   {
      return true;
   }
   
   public void addSatisfier(ISatisfier satisfier)
   {
      m_Satisfiers.add(satisfier);
   }
   
   public void removeSatisfier(ISatisfier satisfier)
   {
      // C++ is empty, go figure
   }
   
   public ETList<IRequirement> getSubRequirements(IRequirementSource requirementSource)
   {
      ETList<IRequirement> retVal = null;
      Document doc = ReqUtils.getXMLDoc(requirementSource);
      
      if(doc != null)
      {
//         StringBuffer pattern = new StringBuffer("//Requirment");
//         pattern.append("[@id='");
//         pattern.append(m_Id);
//         pattern.append("']");
//         
//         Node xmlNode = doc.selectSingleNode(pattern.toString());
         Element xmlNode = doc.elementByID(m_Id);
         
         if(xmlNode != null)
         {
            retVal = RequirementUtility.processChildElements(xmlNode,ETRequirement.class,null);
            
            if(retVal != null)
            {
               int count = retVal.getCount();
               
               for(int index = 0;index < count; index++)
               {
                  IRequirement requirement = retVal.item(index);
                  ReqUtils.addSatisfierChildElements(requirement, requirementSource);
               }
            }
         }
      }
      return retVal;
   }
   
   /** Checks if the node can participate in a drag operation. */
   public boolean isAllowedToDrag()
   {
      return true;
   }
}
