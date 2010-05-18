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

package org.netbeans.modules.uml.core.requirementsframework;

import org.netbeans.modules.uml.core.support.umlsupport.IComparableTreeData;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author brettb
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class RequirementSource implements IRequirementSource, IComparableTreeData
{
   private boolean m_bRequiresLogin; // does source require login
   private String m_strId = ""; // XML id of the requirement
   private String m_strDispName = ""; // name to display in the tree
   private String m_strProvider = ""; // registered name of provider
   private String m_strLocation = ""; // location of the requirements file
   private String m_strProxyFile = ""; // location of the proxy file

   /// Collection of Requirements for this Requirement Source.
   ETList < IRequirement > m_Requirements = null;

   /**
    *
    * Get the path/file location of the proxy file for this
    * requirements source.
    *
    * @param *pVal[out]  Proxy file location.
    *
    * @return void
    *
    */
   public String getProxyFile()
   {
      return m_strProxyFile;
   }

   /**
    *
    * Set the path/file location of the proxy file for this
    * requirements source.
    *
    * @param newVal[in]  Proxy file location.
    *
    * @return void
    *
    */
   public void setProxyFile(String newVal)
   {
      m_strProxyFile = newVal;
   }

   /**
    *
    * Get the path/file location of the requirements file for this
    * requirements source.
    *
    * @param *pVal[out]  Requirements file location.
    *
    * @return void
    *
    */
   public String getLocation()
   {
      return m_strLocation;
   }

   /**
    *
    * Set the path/file location of the requirements file for this
    * requirements source.
    *
    * @param newVal[in]  Requirements file location.
    *
    * @return void
    *
    */
   public void setLocation(String newVal)
   {
      m_strLocation = newVal;
   }

   /**
    *
    * Get the registered name of the Requirements Provider addin
    * that manages this requirements source.
    *
    * @param *pVal[out]  The prog ID.
    *
    * @return void
    *
    */
   public String getProvider()
   {
      return m_strProvider;
   }

   /**
    *
    * Set the registered name of the Requirements Provider addin
    * that manages this requirements source.
    *
    * @param newVal[in]  The prog ID.
    *
    * @return void
    *
    */
   public void setProvider(String newVal)
   {
      m_strProvider = newVal;
   }

   /**
    *
    * Get the name displayed for this requirement source in the
    * Design Center tree.
    *
    * @param *pVal[out]  The displayed name.
    *
    * @return void
    *
    */
   public String getDisplayName()
   {
      return m_strDispName;
   }

   /**
    *
    * Set the name displayed for this requirement source in the
    * Design Center tree.
    *
    * @param newVal[in]  The displayed name.
    *
    * @return void
    *
    */
   public void setDisplayName(String newVal)
   {
      m_strDispName = newVal;
   }

   /**
    *
    * Get the XML ID of this Requirement source (the ID used in
    * the RequirementSources.edt file).
    *
    * @param *pVal[out]  The ID of this requirement source.
    *
    * @return void
    *
    */
   public String getID()
   {
      return m_strId;
   }

   /**
    *
    * Set the XML ID for this Requirement source (the ID used in
    * the RequirementSources.edt file).
    *
    * @param newVal[in]  The ID of this requirement source.
    *
    * @return void
    *
    */
   public void setID(String newVal)
   {
      m_strId = newVal;
   }

   /**
    *
    * Returns true if this requirement source requires a login.
    *
    * @param *pVal[out]  Value of the RequiresLogin property.
    *
    * @return void
    *
    */
   public boolean getRequiresLogin()
   {
      return m_bRequiresLogin;
   }

   /**
    *
    * Sets whether this requirement source requires a login.
    *
    * @param newVal[in] true indicates a login is needed.
    *
    * @return void
    *
    */
   public void setRequiresLogin(boolean newVal)
   {
      m_bRequiresLogin = newVal;
   }

   public boolean isSame(IComparableTreeData otherData)
   {
      if (null == otherData)
         throw new IllegalArgumentException();

      boolean bIsSame = false;

      if (otherData instanceof IRequirementSource)
      {
         IRequirementSource requirementSource = (IRequirementSource)otherData;

         String strId = requirementSource.getID();
         String strProvider = requirementSource.getProvider();

         if (strId.equals(m_strId) && strProvider.equals(m_strProvider))
         {
            bIsSame = true;
         }
      }

      return bIsSame;
   }

}
