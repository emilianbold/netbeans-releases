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

import java.util.HashMap;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Branch;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IConfigManager;
import org.netbeans.modules.uml.core.requirementsframework.IRequirement;
import org.netbeans.modules.uml.core.requirementsframework.IRequirementSource;
import org.netbeans.modules.uml.core.requirementsframework.ISatisfier;
import org.netbeans.modules.uml.core.requirementsframework.Satisfier;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProduct;
import org.dom4j.IDResolver;

/**
 * @author josephg
 *
 */
public class ReqUtils
{
   private static HashMap<String, Document> m_ReqSourcesMap = new HashMap<String, Document>();
   private static String m_ReqProxyFile = "";
   private static Document m_Doc;
   
   public static String getConfigDir()
   {
      String dir = "";
      
      IProduct product = ProductHelper.getProduct();
      if(product != null)
      {
         IConfigManager configMgr = product.getConfigManager();
         if(configMgr != null)
         {
            String loc = configMgr.getDefaultConfigLocation();
            if(loc.length()>0)
            {
                dir = loc;
            }
         }
      }
      return dir;
   }
   
   public static Document getXMLDoc(IRequirementSource requirementSource)
   {
      String location = requirementSource.getLocation();
      String id = requirementSource.getID();
      
      Document doc = m_ReqSourcesMap.get(id);
      
      if(doc == null)
      {
         if(location.length()>0)
         {
            IDResolver resolver = new IDResolver();
            resolver.setGlobalID("id");
            doc = XMLManip.getDOMDocument(location, resolver);
            
            if(doc != null)
            {
               m_ReqSourcesMap.put(id,doc);
            }
         }
      }
      
      return doc;
   }
   
   public static boolean loadXMLDoc(String file)
   {
      if(m_Doc == null)
      {
         if(file.length()>0)
            m_Doc = XMLManip.getDOMDocument(file);
            
         if(m_Doc == null)
            return false;
      }
      return true;
   }
   
   public static void addSatisfierChildElements(IRequirement requirement, IRequirementSource requirementSource)
   {
      String file = null;
      
      if(m_ReqProxyFile.length()>0)
      {
         file = m_ReqProxyFile;
      }
      else
      {
         String configDir = getConfigDir();
         
         if(configDir.length() > 0)
         {
            file = configDir + "RequirementSources.etrp";
            m_ReqProxyFile = file;
         }
      }
      
      Document reqProxyDoc = XMLManip.getDOMDocument(file);
      
      if(reqProxyDoc != null)
      {
         String requirementSourceId = requirementSource.getID();
         
         StringBuffer pattern = new StringBuffer("RequirementProxies/RequirementProxy[@source='");
         pattern.append(requirementSourceId);
         pattern.append("']");
         
         Node requirementProxyNode = reqProxyDoc.selectSingleNode(pattern.toString());
         
         if(requirementProxyNode != null)
         {
            String id = requirement.getID();
            
            pattern = new StringBuffer("Requirement[@id='");
            pattern.append(id);
            pattern.append("']");
            
            Node requirementNode = requirementProxyNode.selectSingleNode(pattern.toString());
            
            if(requirementNode != null)
            {
               Node satisfiersNode = requirementNode.selectSingleNode("Satisfiers");
               
               if(satisfiersNode != null)
               {
                  List satisfierNodeList = null;
                  if(satisfiersNode instanceof Branch)
                  {
                     satisfierNodeList = ((Branch)satisfiersNode).content();
                  }
                  
                  if(satisfierNodeList!=null)
                  {
                     ETList<ISatisfier> satisfiers = new ETArrayList<ISatisfier>();
                     
                     int count = satisfierNodeList.size();
                     
                     for(int index = 0; index < count; index++)
                     {
                        Node satisfierNode = (Node)satisfierNodeList.get(index);
                        
                        if(satisfierNode instanceof Element)
                        {
                           Element satisfierElement = (Element)satisfierNode;
                           Attribute nameAttribute = satisfierElement.attribute("name");
                           Attribute xmiidAttribute = satisfierElement.attribute("xmiid");
                           Attribute projectIdAttribute = satisfierElement.attribute("projectid");
                           Attribute projectNameAttribute = satisfierElement.attribute("projectname");
                           
                           String name = "";
                           if(nameAttribute != null)
                              name = nameAttribute.getValue();
                              
                           String xmiid = "";
                           if(xmiidAttribute != null)
                              xmiid = xmiidAttribute.getValue();
                              
                           String projectId = "";
                           if(projectIdAttribute != null)
                              projectId = projectIdAttribute.getValue();
                              
                           String projectName = "";
                           if(projectNameAttribute != null)
                              projectName = projectNameAttribute.getValue();
                              
                           ISatisfier satisfier = new Satisfier();
                           
                           satisfier.setName(name);
                           satisfier.setXMIID(xmiid);
                           satisfier.setProjectID(projectId);
                           satisfier.setProjectName(projectName);
                           
                           satisfiers.add(satisfier);
                              
                        }
                     }
                     
                     requirement.setSatisfiers(satisfiers);
                  }                  
               }
            }
         }
      }
   }
}
