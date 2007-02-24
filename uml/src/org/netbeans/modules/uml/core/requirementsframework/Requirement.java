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

/**
 * Requirement.java
 *
 * Created on June 24, 2004, 1:55 PM
 */

package org.netbeans.modules.uml.core.requirementsframework;

import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 *
 * @author  Trey Spiva
 */
public class Requirement implements IRequirement
{
   // Satisfier of this Requirement.
   ETList < ISatisfier > m_cpSatisfiers = null;

   // Collection of sub-Requirements for this Requirement.
   ETList < IRequirement > m_cpRequirements = null;

   // A category is a grouping of requirements.
   boolean m_vbIsCategory = false;
   
   // The text of the requirement.
   String m_cbsName = "";
   String m_cbsID = "";
   String m_cbsType = "";
   String m_cbsModName = "";
   String m_cbsDescription = "";
   String m_cbsProviderID = "";
   String m_cbsSourceID = "";
   
   /** Creates a new instance of Requirement */
   public Requirement()
   {
   }
   
   public void addSatisfier(ISatisfier pSatisfier)
   {
      // This method is not implemented in C++
   }
   
   /**
    * Get the text of the requirement.
    *
    * @return The requirement text.
    */
   public String getDescription()
   {
      return m_cbsDescription;
   }
   
   /**
    * Retreives the requirements ID.
    */
   public String getID()
   {
      return m_cbsID;
   }
   
   public String getModName()
   {
      return m_cbsModName;
   }
   
   /**
    * Retrieves the name of the requirement.
    */
   public String getName()
   {
      return m_cbsName;
   }
   
   public String getProjectName()
   {
      // This method is not implemented in C++
      return "";
   }
   
   /**
    * Retrieves the ID of the provider.
    */
   public String getProviderID()
   {
      return m_cbsProviderID;
   }
   
   /**
    * Get the ID of the soruce in the provider.  The source is the database
    * that defines the requiremnt.
    */
   public String getSourceID()
   {
      return m_cbsSourceID;
   }
   
   /**
    * Retrieves the requirements type.
    */
   public String getType()
   {
      return m_cbsType;
   }
   
   /**
    * Sets if the requirment is a category.  A category has no requirement text,
    * but does contain a group of requirements
    *
    * @retrun The value of IsCategory Property.
    */
   public boolean isCategory()
   {
      return m_vbIsCategory;
   }
   
   public void removeSatisfier(ISatisfier pSatisfier)
   {
      // This method is not implemented in C++
   }
   
   /**
    *
    * Set the text of the requirement.
    *
    * @param newVal The requirement text.    */
   public void setDescription(String newVal)
   {
      m_cbsDescription = newVal;
   }
   
   /**
    * Sets the requirements ID.
    */
   public void setID(String newVal)
   {
      m_cbsID = newVal;
   }
   
   /**
    * Sets if the requirment is a category.  A category has no requirement text,
    * but does contain a group of requirements
    *
    * @param newVal The value of IsCategory Property.
    */
   public void setIsCategory(boolean newVal)
   {
      m_vbIsCategory = newVal;
   }
   
   public void setModName(String newVal)
   {
      m_cbsModName = newVal;
   }
   
   /**
    * Sets the name of the requriment.
    */
   public void setName(String newVal)
   {
      m_cbsName = newVal;
   }
   
   public void setProjectName(String newVal)
   {
      // This method is not implemented in C++
   }
   
   /**
    * Set the name of the provider that defines the requirements.
    */
   public void setProviderID(String newVal)
   {
      m_cbsProviderID = newVal;
   }
   
   /**
    * Gets if the requirments for a category.  A category has no requirement text,
    * but does contain a group of requirements
    *
    * @retrun The categories requirments.
    */
   public ETList < IRequirement > getRequirements()
   {
      return m_cpRequirements;
   }
   
   /**
    * Sets if the requirments for a category.  A category has no requirement text,
    * but does contain a group of requirements
    *
    * @retrun The categories requirments.
    */
   public void setRequirements(ETList < IRequirement > newVal)
   {
      m_cpRequirements = newVal;
   }
   
   /**
    * Retrieves the satisifiers of the requirement.
    */
   public ETList < ISatisfier > getSatisfiers()
   {
      return m_cpSatisfiers;
   }
   
   /**
    * Sets the satisifiers of the requirement.
    */
   public void setSatisfiers(ETList < ISatisfier > newVal)
   {
      m_cpSatisfiers = newVal;
   }
   
   /**
    * Set the ID of the soruce in the provider.  The source is the database
    * that defines the requiremnt.
    */
   public void setSourceID(String newVal)
   {
      m_cbsSourceID = newVal;
   }
   
   /**
    * Sets the requirments type.
    */
   public void setType(String newVal)
   {
      m_cbsType = newVal;
   }
   
   
   public ETList < IRequirement > getSubRequirements( IRequirementSource pRequirementSource )
   {
      // This method is not implemented in C++
      return new ETArrayList < IRequirement >();
   }
   
   /** Checks if the node can participate in a drag operation. */
   public boolean isAllowedToDrag()
   {
      return true;
   }
   
}
