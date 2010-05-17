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

import java.util.Iterator;
import java.util.List;

import org.dom4j.Element;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author brettb
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class RequirementUtility
{

   /**
    *
    * Recursively process child XML elements from the requirements file
    * looking for requirements to display in the requirements tree.
    *
    * @param node[in] Current XML node.
    * @param class[in] ClassID of the component to be created.
    * @param ppRequirements[out,retval] Requirements Collection
    *
    * @return void
    *
    */
   public static ETList < IRequirement > processChildElements(Node node, Class c, ETList < IRequirement > requirements)
   
   {
      if (null == node)
         throw new IllegalArgumentException();
         
      String strProjectName = "";
      if (node instanceof Element)
      {
         Element projectElement = (Element)node;

         strProjectName = projectElement.attributeValue("name");
      }

      List children = node.selectNodes("Requirement");

      // Get all Requirement Nodes from XMLDOM representation of file and add to 
      // an IRequirements collection.
      for (Iterator iter = children.iterator(); iter.hasNext();)
      {
         Node childNode = (Node)iter.next();

         if (childNode instanceof Element)
         {
            Element childElement = (Element)childNode;

            String strNodeName = childElement.getName();
            String strName = childElement.attributeValue("name");
            String strModName = childElement.attributeValue("modname");
            String strType = childElement.attributeValue("type");

            if (strType.equals("Requirement") || strType.equals("Category"))
            {
               String strID = childElement.attributeValue("id");

               // Create a new Requirement instance.
               IRequirement requirement = null;
               try
               {
                  requirement = (IRequirement)c.newInstance();
               }
               catch( Exception e )
               {
                  e.printStackTrace();
               }

               // Get the Node's description for display in treeItem later.
               Node descriptionNode = childElement.selectSingleNode("Description");

               if ((descriptionNode != null) && (requirement != null))
               
                  {
                  // Populate the Requirement's id and name properties.
                  requirement.setType(strType);
                  requirement.setID(strID);
                  requirement.setName(strName);
                  requirement.setModName(strModName);
                  requirement.setProjectName(strProjectName);

                  // Populate the Requirement's description prop.
                  String strDescription = descriptionNode.getText();
                  requirement.setDescription(strDescription);

                  // Create or add to the return collection parameter.
                  if (requirements != null)
                  {
                     requirements.add(requirement);
                  }
                  else
                  {
                     requirements = new ETArrayList < IRequirement >();

                     requirements.add(requirement);
                  }
               }
            }
            else
            {
               requirements = processChildElements( childElement, c, requirements );
            }
         }
      }

      return requirements;
   }
}
