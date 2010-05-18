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
