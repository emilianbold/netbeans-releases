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

package org.netbeans.modules.uml.requirements.xmlprovider;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.common.generics.ETPairT;
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
